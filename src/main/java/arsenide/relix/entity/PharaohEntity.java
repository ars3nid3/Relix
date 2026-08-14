package arsenide.relix.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;

import arsenide.relix.Relix;
import arsenide.relix.effects.RelixEffects;
import arsenide.relix.entity.ai.PharaohChooseStateGoal;
import arsenide.relix.entity.ai.PharaohDoNothingGoal;
import arsenide.relix.entity.ai.PharaohState;
import arsenide.relix.items.RelixItems;
import arsenide.relix.networking.PharaohVisualPayload;
import arsenide.relix.sounds.RelixSounds;
import arsenide.relix.util.SpawnUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

public class PharaohEntity extends Monster implements RangedAttackMob {

    private final int SPAWN_DURATION = 200;

    private final int SUMMONING_UNDEAD_COOLDOWN = 300;
    // Time until he hits the stick on the ground
    private final int SUMMONING_UNDEAD_PREAMBLE = 57;
    // Total animation time
    private final int SUMMONING_UNDEAD_DURATION = 70;
    
    private final int SHOCKWAVE_COOLDOWN = 80;
    // Time until he actually swings the stick
    private final int SHOCKWAVE_PREAMBLE_DURATION = 20;
    // Total animation time
    private final int SHOCKWAVE_DURATION = 33;
    private final double SHOCKWAVE_EXPANSION_RATE = 0.5; // Blocks per tick
    private final double SHOCKWAVE_ROTATION_RATE = 0.35; // Rad per tick
    private final double SHOCKWAVE_MAX_RADIUS = 8.0; // Blocks

    private final int SPELLCAST_COOLDOWN = 400;
    // Time of the full animation
    private final int SPELLCAST_DURATION = 100;

    private int specialAttackCooldown = 200;

    private BlockPos jukeboxPos = null;

    private static final EntityDataAccessor<Integer> STATE =
        SynchedEntityData.defineId(PharaohEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE_TICK =
        SynchedEntityData.defineId(PharaohEntity.class, EntityDataSerializers.INT);

    private Set<Monster> summonedEntities = new HashSet<>();
    private List<UUID> summonedEntitiesUUIDs = new ArrayList<>();
    private Set<UUID> knockedbackEntitiesUUIDs = new HashSet<>();
    private Set<UUID> cursedEntitiesUUIDs = new HashSet<>();
    private float summonedEntitiesMaxHealth = 1.0F;

    private final List<EntityType<? extends Monster>> summonableEntities = List.of(
        EntityTypes.HUSK,
        EntityTypes.PARCHED
    );

    private final ServerBossEvent bossBar = new ServerBossEvent(
        Mth.createInsecureUUID(random),
        Component.translatable("entity.relix.pharaoh"),
        BossEvent.BossBarColor.PURPLE,
        BossEvent.BossBarOverlay.PROGRESS
    );

    private final ServerBossEvent summonedEntitiesBar = new ServerBossEvent(
        Mth.createInsecureUUID(random),
        getSummonedEntitiesBarComponent(),
        BossEvent.BossBarColor.RED,
        BossEvent.BossBarOverlay.PROGRESS
    );

    protected PharaohEntity(EntityType<? extends PharaohEntity> type, Level level) {
        super(RelixEntities.PHARAOH_ENTITY.get(), level);
    }

    @Override
    protected void defineSynchedData(Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(STATE, PharaohState.SPAWNING.ordinal());
        entityData.define(STATE_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        // When spawning, always try to do nothing
        this.goalSelector.addGoal(0, new PharaohDoNothingGoal(this));
        this.goalSelector.addGoal(1, new PharaohChooseStateGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 0.4F, 40, 8F) {
            @Override
            public boolean canUse() {
                if (getState() != PharaohState.DEFAULT) return false;
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.3F) {
            @Override
            public boolean canUse() {
                if (getState() != PharaohState.DEFAULT) return false;
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public boolean addEffect(MobEffectInstance newEffect, @Nullable Entity source) {
        if (newEffect.getEffect().equals(RelixEffects.TIME_DILATION)) {
            return false;
        } else {
            MobEffectCategory category = newEffect.getEffect().value().getCategory();
            float multiplier = 0.0F;
            if (category == MobEffectCategory.BENEFICIAL) {
                multiplier = 0.75F;
            } else if (category == MobEffectCategory.HARMFUL) {
                multiplier = 0.25F;
            }
            if (multiplier > 0.0F) {
                MobEffectInstance reducedEffect = newEffect.withScaledDuration(
                    multiplier
                );
                return super.addEffect(reducedEffect, source);
            }
        }
        return super.addEffect(newEffect, source);
    }

    public static AttributeSupplier getDefaultAttributes() {
        return createMonsterAttributes()
        .add(Attributes.MAX_HEALTH, 250.0D)
        .add(Attributes.ARMOR, 10.0D)
        .build();
    }

    public void setState(PharaohState state) {
        entityData.set(STATE, state.ordinal());
        entityData.set(STATE_TICK, 0);
        if (state != PharaohState.DEFAULT) {
            getNavigation().stop();
            if (state != PharaohState.SPAWNING) {
                setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    public PharaohState getState() {
        return PharaohState.values()[entityData.get(STATE)];
    }

    public int getStateTick() {
        return entityData.get(STATE_TICK);
    }

    private void setStateTick(int tick) {
        if (tick < 0) return;
        entityData.set(STATE_TICK, tick);
    }

    public int getSpecialAttackCooldown() {
        return specialAttackCooldown;
    }

    public void setSpecialAttackCooldown(int specialAttackCooldown) {
        if (this.specialAttackCooldown < 0) return;
        this.specialAttackCooldown = specialAttackCooldown;
    }

    private void resolveSummonedEntities() {
        if (summonedEntitiesUUIDs.isEmpty()) return;

        Iterator<UUID> iterator = summonedEntitiesUUIDs.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = level().getEntity(uuid);
            if (
                entity instanceof Monster summonedEntity
            ) {
                if (!summonedEntity.isDeadOrDying()) {
                    summonedEntities.add(summonedEntity);
                    summonedEntitiesBar.setVisible(true);
                    setInvulnerable(true);
                    setSummonEntitiesBarProgress();
                }
                iterator.remove();
            }
        }
    }

    @Override
    protected boolean isImmobile() {
        return getState() != PharaohState.DEFAULT || super.isImmobile();
    }

    @Override
    public boolean isPushable() {
        return getState() == PharaohState.DEFAULT && super.isPushable();
    }

    @Override
    public void push(double xa, double ya, double za) {
        if (getState() != PharaohState.DEFAULT) {
            return;
        }
        super.push(xa, ya, za);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (getState() != PharaohState.DEFAULT && getState() != PharaohState.SPAWNING) {
                setDeltaMovement(Vec3.ZERO);
            }
            resolveSummonedEntities();
            setStateTick(getStateTick() + 1);

            switch (getState()) {
                case SPAWNING -> tickSpawning();
                case DEFAULT -> tickDefault();
                case SUMMONING_UNDEAD -> tickSummoningUndead();
                case SHOCKWAVE -> tickShockwave();
                case SPELLCASTING -> tickSpellCasting();
                default -> {}
            }

            if (summonedEntitiesBar.isVisible()) {
                setSummonEntitiesBarProgress();
                if (summonedEntities.isEmpty()) {
                    summonedEntitiesBar.setVisible(false);
                    setInvulnerable(false);
                }
            }
        } else {
            switch (getState()) {
                case SHOCKWAVE -> tickShockwaveClient();
                default -> {}
            }
        }
    }

    private Component getSummonedEntitiesBarComponent() {
        Component countComponent = summonedEntities.size() > 0 &&
            summonedEntities.size() < 4 ?
            Component.literal(" - ").append(
            Component.literal(summonedEntities.size() + " ")).append(
            Component.translatable(
                "entity.relix.pharaoh.summoned_entities.remaining"
            )) :
            Component.literal("");
        return Component.translatable(
            "entity.relix.pharaoh.summoned_entities"
        ).append(countComponent);
    }

    private void setSummonEntitiesBarProgress() {
        float totalHealth = 0.0F;
        ArrayList<Monster> toRemove = new ArrayList<>();
        for (Monster summonedEntity : summonedEntities) {
            totalHealth += summonedEntity.getHealth();
            if (summonedEntity.isDeadOrDying()) {
                toRemove.add(summonedEntity);
            }
        }        summonedEntitiesBar.setProgress(
            Mth.clamp(
                totalHealth / summonedEntitiesMaxHealth,
                0.0F,
                1.0F
            )
        );
        for (Monster summonedEntity : toRemove) {
            summonedEntities.remove(summonedEntity);
        }
        summonedEntitiesBar.setName(getSummonedEntitiesBarComponent());
    }

    public void identifyLivingDead() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Vec3 pharaohStart = position().add(0, getBbHeight() / 2, 0);
        for (Monster summonedEntity : summonedEntities) {
            // Spawn particle line between pharaoh and summoned entity
            Vec3 summonedEntityEnd = summonedEntity.position().add(
                0, 
                summonedEntity.getBbHeight() / 2, 
                0
            );
            Vec3 delta = summonedEntityEnd.subtract(pharaohStart);
            double distance = delta.length();
            double spacing = 0.15;
            Vec3 step = delta.normalize().scale(spacing);
            Vec3 particlePos = pharaohStart;
            for (double travelled = 0; travelled < distance; travelled += spacing) {
                serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0,
                    0,
                    0,
                    0
                );
                particlePos = particlePos.add(step);
            }
        }
    }

    public boolean hasLivingDead() {
        return !summonedEntities.isEmpty() || !summonedEntitiesUUIDs.isEmpty();
    }

    private void tickSpawning() {
        setInvulnerable(true);

        if (!onGround()) {
            Vec3 velocity = getDeltaMovement();
            if (velocity.y < 0) velocity = velocity.multiply(1, 0.45, 1);
            setDeltaMovement(velocity);
        }

        bossBar.setProgress(Mth.clamp(getStateTick() / (float) SPAWN_DURATION, 0.0F, 1.0F));

        if (getStateTick() >= SPAWN_DURATION) {
            setInvulnerable(false);
            setState(PharaohState.DEFAULT);
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    private void lifecycleTick() {
        bossBar.setProgress(getHealth() / getMaxHealth());
        setSpecialAttackCooldown(Math.max(0, getSpecialAttackCooldown() - 1));
    }

    private void tickDefault() {
        lifecycleTick();
    }

    private void tickShockwaveClient() {
        if (getStateTick() < SHOCKWAVE_PREAMBLE_DURATION) {
            return;
        }
        double currentRadius = Math.min(
            (getStateTick() - SHOCKWAVE_PREAMBLE_DURATION) * SHOCKWAVE_EXPANSION_RATE, 
            SHOCKWAVE_MAX_RADIUS
        );
        for (int arm = 0; arm < 4; arm++) {
            double angle = (getStateTick() - SHOCKWAVE_PREAMBLE_DURATION) * SHOCKWAVE_ROTATION_RATE
                + arm * Math.PI / 2
                + currentRadius * 3;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            level().addParticle(
                ParticleTypes.SOUL,
                getX() + dx * currentRadius,
                getY() + getBbHeight() / 2,
                getZ() + dz * currentRadius,
                0,
                0,
                0
            );
        }
    }

    private void tickShockwave() {
        lifecycleTick();
        if (getStateTick() < SHOCKWAVE_PREAMBLE_DURATION) {
            return;
        } else if (getStateTick() == SHOCKWAVE_PREAMBLE_DURATION) {
            level().playSound(
                null, 
                new BlockPos(
                    (int) Math.floor(position().x), 
                    (int) Math.floor(position().y), 
                    (int) Math.floor(position().z)
                ), 
                RelixSounds.PHARAOH_SHOCKWAVE.get(), 
                SoundSource.HOSTILE, 
                1.0F, 1.0F
            );
        }

        double currentRadius = Math.min(
            (getStateTick() - SHOCKWAVE_PREAMBLE_DURATION) * SHOCKWAVE_EXPANSION_RATE, 
            SHOCKWAVE_MAX_RADIUS
        );
        AABB shockwaveAABB = new AABB(
            getX() - currentRadius,
            getY() - 2.0,
            getZ() - currentRadius,
            getX() + currentRadius,
            getY() + getBbHeight() + 2.0,
            getZ() + currentRadius
        );
        List<LivingEntity> entitiesInShockwave = level().getEntitiesOfClass(
            LivingEntity.class, 
            shockwaveAABB,
            entity -> 
                entity != this && 
                !summonedEntities.contains(entity) &&
                Math.abs(entity.position().distanceTo(position()) - currentRadius) < 0.4 &&
                !knockedbackEntitiesUUIDs.contains(entity.getUUID())
        );
        for (LivingEntity entity : entitiesInShockwave) {
            Vec3 knockbackVector = entity.position().subtract(position());
            Vec3 knockback = knockbackVector.normalize().scale(4);
            double knockbackMultiplier =  2.0 * (1.0 - Mth.clamp(
                knockbackVector.length() / SHOCKWAVE_MAX_RADIUS, 
                0.0, 
                1.0
            ));
            entity.push(
                knockback.x * knockbackMultiplier, 
                1.0 * knockbackMultiplier, 
                knockback.z * knockbackMultiplier
            );
            entity.hurtServer(
                (ServerLevel) level(), 
                damageSources().mobAttack(this), 
                5.0F
            );
            knockedbackEntitiesUUIDs.add(entity.getUUID());
        }
        if (
            currentRadius >= SHOCKWAVE_MAX_RADIUS && 
            getStateTick() >= SHOCKWAVE_DURATION
        ) {
            setState(PharaohState.DEFAULT);
            float cooldownModifier = random.nextFloat() * 0.2F + 1.0F;
            setSpecialAttackCooldown((int) (SHOCKWAVE_COOLDOWN * cooldownModifier));
            knockedbackEntitiesUUIDs.clear();
        }
    }

    public boolean isCastingSpell() {
        return getState() == PharaohState.SPELLCASTING &&
        getStateTick() < SPELLCAST_DURATION;
    }

    private void tickSpellCasting() {
        lifecycleTick();

        if (getStateTick() < SPELLCAST_DURATION) {
            return;
        } else {
            List<Player> entitiesInSphere = level().getEntitiesOfClass(
                Player.class,
                new AABB(
                    position().x - 16,
                    position().y - 8,
                    position().z - 16,
                    position().x + 16,
                    position().y + 8,
                    position().z + 16
                ),
                entity -> !cursedEntitiesUUIDs.contains(entity.getUUID()) &&
                entity.position().distanceTo(position()) <= 16
            );
            for (Player entity : entitiesInSphere) {
                entity.addEffect(new MobEffectInstance(
                    RelixEffects.TIME_DILATION,
                    200,
                    0
                ));
                cursedEntitiesUUIDs.add(entity.getUUID());
            }
            setState(PharaohState.DEFAULT);
            float cooldownModifier = random.nextFloat() * 0.2F + 1.0F;
            setSpecialAttackCooldown((int) (SPELLCAST_COOLDOWN * cooldownModifier));
            cursedEntitiesUUIDs.clear();
            level().playSound(
                null,
                new BlockPos(
                    (int) Math.floor(position().x), 
                    (int) Math.floor(position().y), 
                    (int) Math.floor(position().z)
                ),
                RelixSounds.DILATION.get(),
                SoundSource.HOSTILE,
                1.0F, 1.0F
            );
        }
    }

    private void tickSummoningUndead() {
        lifecycleTick();
        // There are already summoned entities, do nothing
        if (getStateTick() < SUMMONING_UNDEAD_PREAMBLE && summonedEntities.size() > 0) {
            float cooldownModifier = random.nextFloat() * 0.2F + 1.0F;
            setSpecialAttackCooldown((int) (SUMMONING_UNDEAD_COOLDOWN * cooldownModifier));
            setState(PharaohState.DEFAULT);
            return;
        }
        if (
            getStateTick() < SUMMONING_UNDEAD_PREAMBLE ||
            (getStateTick() > SUMMONING_UNDEAD_PREAMBLE && 
            getStateTick() < SUMMONING_UNDEAD_DURATION)
        ) {
            // If we are in the preamble or in the duration, do nothing
            return;
        } else if (getStateTick() == SUMMONING_UNDEAD_PREAMBLE) {
            level().playSound(
                null,
                blockPosition(),
                RelixSounds.PHARAOH_SUMMON.get(),
                SoundSource.HOSTILE
            );
            // Spawn new undead entities, set bar to visible, make boss invulnerable
            boolean spawnedAtLeastOne = spawnUndeadEntities(
                random.nextInt(2) + 3
            );
            if (spawnedAtLeastOne) {
                summonedEntitiesBar.setVisible(true);
                setInvulnerable(true);
            }
            return;
        }
        float cooldownModifier = random.nextFloat() * 0.2F + 1.0F;
        setSpecialAttackCooldown((int) (SUMMONING_UNDEAD_COOLDOWN * cooldownModifier));
        setState(PharaohState.DEFAULT);
    }

    private void enchantAndEquipRandomItem(
        Monster summonedEntity,
        EquipmentSlot slot,
        List<Item> itemChoices
    ) {
        ItemStack item = new ItemStack(
            itemChoices.get(
                random.nextInt(itemChoices.size())
            )
        );
        enchantAndEquipItem(
            summonedEntity,
            slot,
            item
        );
    }

    private void enchantAndEquipItem(
        Monster summonedEntity,
        EquipmentSlot slot,
        ItemStack item
    ) {
        EnchantmentHelper.enchantItem(
            random, 
            item, 
            30,
            level().registryAccess(),
            Optional.empty()
        );
        summonedEntity.setItemSlot(slot, item);
    }

    private void equipRandomArmor(Monster summonedEntity, boolean isRanged) {
        List<Item> headItems = List.of(
            RelixItems.PHARAOH_HEADDRESS.get()
        );
        enchantAndEquipRandomItem(
            summonedEntity, 
            EquipmentSlot.HEAD, 
            headItems
        );
        List<Item> chestItems = List.of(
            Items.GOLDEN_CHESTPLATE,
            Items.CHAINMAIL_CHESTPLATE
        );
        if (random.nextBoolean()) {
            enchantAndEquipRandomItem(
                summonedEntity, 
                EquipmentSlot.CHEST, 
                chestItems
            );
        }
        List<Item> legItems = List.of(
            Items.GOLDEN_LEGGINGS,
            Items.CHAINMAIL_LEGGINGS
        );
        if (random.nextBoolean()) {
            enchantAndEquipRandomItem(
                summonedEntity, 
                EquipmentSlot.LEGS, 
                legItems
            );
        }
        List<Item> footItems = List.of(
            Items.GOLDEN_BOOTS,
            Items.CHAINMAIL_BOOTS
        );
        if (random.nextBoolean()) {
            enchantAndEquipRandomItem(
                summonedEntity, 
                EquipmentSlot.FEET, 
                footItems
            );
        }
        List<Item> weaponItems = List.of(
            Items.GOLDEN_SWORD,
            Items.GOLDEN_AXE,
            Items.GOLDEN_HOE,
            Items.IRON_SWORD,
            Items.IRON_AXE,
            Items.IRON_HOE
        );
        List<Holder<Potion>> arrowPotionEffects = List.of(
            Potions.LONG_WEAKNESS,
            Potions.SLOWNESS
        );
        if (random.nextBoolean() && !isRanged) {
            enchantAndEquipRandomItem(
                summonedEntity, 
                EquipmentSlot.MAINHAND, 
                weaponItems
            );
        } else if (isRanged) {
            enchantAndEquipItem(
                summonedEntity,
                EquipmentSlot.MAINHAND,
                new ItemStack(Items.BOW)
            );
            ItemStack arrows = new ItemStack(Items.TIPPED_ARROW);
            arrows.set(
                DataComponents.POTION_CONTENTS,
                new PotionContents(
                    arrowPotionEffects.get(
                        random.nextInt(arrowPotionEffects.size())
                    )
                )
            );
            summonedEntity.setItemSlot(EquipmentSlot.OFFHAND, arrows);
        }
    }

    private boolean spawnUndeadEntities(int count) {
        int randomAngleDeg = random.nextInt(360);
        int angleDelta = 360 / count;
        int spawnCount = 0;
        summonedEntities.clear();
        summonedEntitiesMaxHealth = 0.0F;
        for (int i = 0; i < count; i++) {
            int angleDeg = randomAngleDeg + i * angleDelta;
            EntityType<? extends Monster> chosenEntity = summonableEntities.get(
                random.nextInt(summonableEntities.size())
            );
            Vec3 spawnPos = SpawnUtil.getSpawnPosInCircle(
                level(), 
                position(),
                5.0D,
                angleDeg,
                4,
                8,
                2,
                1
            );
            if (spawnPos == null) {
                continue;
            }
            Monster summonedEntity = chosenEntity.create(
                level(),
                EntitySpawnReason.MOB_SUMMONED
            );
            summonedEntity.setCustomName(
                Component.translatable("entity.relix.pharaoh.summoned_entity")
                .withStyle(ChatFormatting.GOLD)
            );
            summonedEntity.setPos(spawnPos);
            equipRandomArmor(
                summonedEntity, 
                summonedEntity instanceof RangedAttackMob
            );
            // Don't despawn summoned entities
            summonedEntity.setPersistenceRequired();
            SpawnUtil.summonPoof(spawnPos, level());
            level().addFreshEntity(summonedEntity);

            summonedEntity.setCanPickUpLoot(false);
            summonedEntity.setDropChance(EquipmentSlot.HEAD, 0.0F);
            summonedEntity.setDropChance(EquipmentSlot.CHEST, 0.0F);
            summonedEntity.setDropChance(EquipmentSlot.LEGS, 0.0F);
            summonedEntity.setDropChance(EquipmentSlot.FEET, 0.0F);
            summonedEntity.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            summonedEntity.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
            summonedEntities.add(summonedEntity);
            summonedEntitiesMaxHealth += summonedEntity.getMaxHealth();
            spawnCount++;
        }
        return spawnCount > 0;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        if (getState() != PharaohState.DEFAULT) return;
        PharaohBoltEntity bolt = new PharaohBoltEntity(serverLevel, this, target);
        Vec3 staffOffset = new Vec3(0.45F, 3F, -2F);
        Vec3 origin = position().add(staffOffset.yRot(
            (180.0F - yBodyRot) * Mth.DEG_TO_RAD
        ));
        bolt.setPos(origin);
        bolt.setDeltaMovement(
            target.getEyePosition().subtract(origin).normalize().scale(
                PharaohBoltEntity.SPEED
            )
        );
        serverLevel.addFreshEntity(bolt);
        serverLevel.playSound(
            null,
            blockPosition(),
            RelixSounds.PHARAOH_BOLT_SHOOT.get(),
            SoundSource.HOSTILE,
            1.0F,
            0.9F + random.nextFloat() * 0.2F
        );
    }

    @Override
    public void checkDespawn() {
        if (EventHooks.checkMobDespawn(this)) return;
        if (level().getDifficulty() == Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
            discard();
        } else {
            noActionTime = 0;
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
        summonedEntitiesBar.addPlayer(player);
        PacketDistributor.sendToPlayer(player, new PharaohVisualPayload(getId(), true));
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
        summonedEntitiesBar.removePlayer(player);
        PacketDistributor.sendToPlayer(player, new PharaohVisualPayload(getId(), false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("CurrentState", getState().ordinal());
        output.putInt("StateTick", getStateTick());
        output.putInt("SpecialAttackCooldown", getSpecialAttackCooldown());
        TypedOutputList<String> summonedOutput = output.list(
            "SummonedEntities",
            Codec.STRING
        );
        Set<UUID> uniqueSummonedEntityUUIDs = new HashSet<>();
        for (Monster summonedEntity : summonedEntities) {
            uniqueSummonedEntityUUIDs.add(summonedEntity.getUUID());
        }
        for (UUID summonedEntityUUID : summonedEntitiesUUIDs) {
            uniqueSummonedEntityUUIDs.add(summonedEntityUUID);
        }
        for (UUID uniqueSummonedEntityUUID : uniqueSummonedEntityUUIDs) {
            summonedOutput.add(uniqueSummonedEntityUUID.toString());
        }
            
        output.putFloat(
            "SummonedEntitiesMaxHealth", 
            summonedEntitiesMaxHealth
        );
        TypedOutputList<String> knockedbackOutput = output.list(
            "KnockedbackEntities",
            Codec.STRING
        );
        for (UUID knockedbackEntityUUID : knockedbackEntitiesUUIDs) {
            knockedbackOutput.add(knockedbackEntityUUID.toString());
        }
        TypedOutputList<String> cursedOutput = output.list(
            "CursedEntities",
            Codec.STRING
        );
        for (UUID cursedEntityUUID : cursedEntitiesUUIDs) {
            cursedOutput.add(cursedEntityUUID.toString());
        }
        if (jukeboxPos != null) {
            output.putIntArray(
                "JukeboxPos",
                new int[] {
                    jukeboxPos.getX(),
                    jukeboxPos.getY(),
                    jukeboxPos.getZ()
                }
            );
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int stateValue = input.getIntOr("CurrentState", PharaohState.SPAWNING.ordinal());
        setState(PharaohState.values()[stateValue]);
        setStateTick(input.getIntOr("StateTick", 0));
        setSpecialAttackCooldown(input.getIntOr("SpecialAttackCooldown", 0));
        bossBar.setProgress(getHealth() / getMaxHealth());

        summonedEntities.clear();
        summonedEntitiesUUIDs.clear();
        ValueInput.TypedInputList<String> summonedInput = input.listOrEmpty(
            "SummonedEntities",
            Codec.STRING
        );
        for (String summonedEntityUUID : summonedInput) {
            try {
                summonedEntitiesUUIDs.add(UUID.fromString(summonedEntityUUID));
            } catch (IllegalArgumentException e) {
                Relix.LOGGER.error(
                    "Invalid UUID for summoned entity: {}", 
                    summonedEntityUUID
                );
                continue;
            }
        }
        summonedEntitiesMaxHealth = input.getFloatOr(
            "SummonedEntitiesMaxHealth", 
            1.0F
        );
        knockedbackEntitiesUUIDs.clear();
        ValueInput.TypedInputList<String> knockedbackInput = input.listOrEmpty(
            "KnockedbackEntities",
            Codec.STRING
        );
        for (String knockedbackEntityUUID : knockedbackInput) {
            try {
                knockedbackEntitiesUUIDs.add(UUID.fromString(knockedbackEntityUUID));
            } catch (IllegalArgumentException e) {
                Relix.LOGGER.error(
                    "Invalid UUID for knockedback entity: {}", 
                    knockedbackEntityUUID
                );
                continue;
            }
        }
        cursedEntitiesUUIDs.clear();
        ValueInput.TypedInputList<String> cursedInput = input.listOrEmpty(
            "CursedEntities",
            Codec.STRING
        );
        for (String cursedEntityUUID : cursedInput) {
            try {
                cursedEntitiesUUIDs.add(UUID.fromString(cursedEntityUUID));
            } catch (IllegalArgumentException e) {
                Relix.LOGGER.error(
                    "Invalid UUID for cursed entity: {}", 
                    cursedEntityUUID
                );
                continue;
            }
        }
        int[] jukeboxCoords = input.getIntArray("JukeboxPos").orElse(new int[] {});
        if (jukeboxCoords.length == 3) {
            jukeboxPos = new BlockPos(
                jukeboxCoords[0],
                jukeboxCoords[1],
                jukeboxCoords[2]
            );
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide() && jukeboxPos != null) {
            BlockState blockState = level().getBlockState(jukeboxPos);
            if (blockState.is(Blocks.JUKEBOX)) {
                level().destroyBlock(
                    jukeboxPos,
                    false
                );
            }
        }
        super.die(source);
    }

    public void informJukebox(BlockPos jukeboxPos) {
        this.jukeboxPos = jukeboxPos;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return RelixSounds.PHARAOH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return RelixSounds.PHARAOH_DEATH.get();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return RelixSounds.PHARAOH_IDLE.get();
    }

}