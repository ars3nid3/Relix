package arsenide.relix;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import arsenide.relix.blocks.RelixBlocks;
import arsenide.relix.effects.RelixEffects;
import arsenide.relix.entity.PharaohEntity;
import arsenide.relix.entity.PharaohSpawnSceneEntity;
import arsenide.relix.entity.RelixEntities;
import arsenide.relix.items.RelixItems;
import arsenide.relix.items.trades.DesertPyramidMapTrade;
import arsenide.relix.menu.RelixMenus;
import arsenide.relix.networking.PharaohVisualPayload;
import arsenide.relix.sounds.RelixSounds;
import arsenide.relix.world.RelixAttachments;
import arsenide.relix.world.RelixMapDecorationTypes;
import arsenide.relix.world.RelixWorldData;
import arsenide.relix.world.SummonRequirement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// Mod entrypoint, name must match modid in TOML
@Mod(Relix.MODID)
public class Relix {
    // Use this when referring to Mod ID
    public static final String MODID = "relix";

    public static final Logger LOGGER = LogUtils.getLogger();

    // Runs on mod load
    public Relix(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        RelixBlocks.register(modEventBus);

        // Register the Deferred Register to the mod event bus so items get registered
        RelixItems.register(modEventBus);

        RelixEntities.register(modEventBus);

        RelixMenus.register(modEventBus);

        RelixSounds.register(modEventBus);

        RelixEffects.register(modEventBus);

        RelixAttachments.register(modEventBus);

        RelixMapDecorationTypes.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Relix) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (Config.LOG_INIT_MESSAGE.get()) {
            LOGGER.info("Hello from Relix! We put the cobwebs back in.");
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(RelixItems.HIEROGLYPH_TABLET);
            event.accept(RelixItems.MUSIC_DISC_PHARAOH);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(RelixItems.SACRED_GOLD_HELMET);
            event.accept(RelixItems.SACRED_GOLD_CHESTPLATE);
            event.accept(RelixItems.SACRED_GOLD_LEGGINGS);
            event.accept(RelixItems.SACRED_GOLD_BOOTS);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(RelixBlocks.TABLET_TABLE_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(RelixItems.SACRED_GEMSTONE);
            event.accept(RelixItems.SACRED_GOLD_UPGRADE_SMITHING_TEMPLATE);
            event.accept(RelixItems.PHARAOH_HEADDRESS);
        }
    }

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (
            event.getLevel() instanceof ServerLevel level &&
            level.dimension() == (Level.OVERWORLD)
        ) {
            RelixWorldData data = level.getDataStorage().computeIfAbsent(
                RelixWorldData.factory(),
                RelixWorldData.FILE_ID
            );
            if (data == null) {
                data = new RelixWorldData(level);
                level.getDataStorage().set(
                    RelixWorldData.FILE_ID,
                    data
                );
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractWithBlock(PlayerInteractEvent.RightClickBlock event) {
        // Don't respond to client-side events
        if (event.getLevel().isClientSide()) {
            return;
        }
        // Only respond to interaction using main hand
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        // Get the world data for the level, which contains summon conditions
        RelixWorldData data = level.getDataStorage().computeIfAbsent(
            RelixWorldData.factory(), 
            RelixWorldData.FILE_ID
        );
        List<SummonRequirement> pharaohRequirements = data.pharaohConditions();

        // Check if we meet all summon conditions
        for (SummonRequirement requirement : pharaohRequirements) {
            if (!requirement.isMet(level, pos, player)) {
                return;
            }
        }

        // If we meet all conditions, add the spawn scene entity
        PharaohSpawnSceneEntity spawnScene = new PharaohSpawnSceneEntity(
            level, 
            pos, 
            player
        );
        spawnScene.setPos(Vec3.atBottomCenterOf(pos.above()));
        level.addFreshEntity(spawnScene);

        CriteriaTriggers.SUMMONED_ENTITY.trigger(
            (ServerPlayer) player, 
            spawnScene
        );

        // Consume the item
        player.getItemInHand(event.getHand()).shrink(1);
        player.swing(event.getHand());
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            RelixBlocks.TABLET_TABLE_ENTITY.get(),
            (be, side) -> be.getInventory()
        );
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("pharaohvisual")
            .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("on")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        PacketDistributor.sendToPlayer(
                            player, 
                            new PharaohVisualPayload(0, true)
                        );
                        return 1;
                    })
                ).then(Commands.literal("off")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        PacketDistributor.sendToPlayer(
                            player, 
                            new PharaohVisualPayload(0, false)
                        );
                        return 1;
                    })
                )
        );
        event.getDispatcher().register(
            Commands.literal("reroll_pharaoh_summon")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getServer().overworld();
                    RelixWorldData data = level.getDataStorage()
                        .computeIfAbsent(
                            RelixWorldData.factory(),
                            RelixWorldData.FILE_ID
                        );
                    if (data == null) {
                        data = new RelixWorldData(level);
                        level.getDataStorage().set(
                            RelixWorldData.FILE_ID,
                            data
                        );
                    }
                    data.reroll(level);
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Rerolled pharaoh summon requirements"),
                        true
                    );
                    return 1;
                })
        );
    }

    @SubscribeEvent
    public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.hasEffect(RelixEffects.TIME_DILATION)) {
            if (RelixAttachments.hasTimeDilationSlowUse(livingEntity)) {
                RelixAttachments.setTimeDilationSlowUse(livingEntity, false);
            } else {
                event.setDuration(event.getDuration() + 1);
                RelixAttachments.setTimeDilationSlowUse(livingEntity, true);
            }
        } else if (RelixAttachments.hasTimeDilationSlowUse(livingEntity)) {
            RelixAttachments.setTimeDilationSlowUse(livingEntity, false);
        }
    }

    @SubscribeEvent
    public void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (
            player.getItemBySlot(EquipmentSlot.HEAD).getItem() == RelixItems.SACRED_GOLD_HELMET.get()
            || player.getItemBySlot(EquipmentSlot.CHEST).getItem() == RelixItems.SACRED_GOLD_CHESTPLATE.get()
            || player.getItemBySlot(EquipmentSlot.LEGS).getItem() == RelixItems.SACRED_GOLD_LEGGINGS.get()
            || player.getItemBySlot(EquipmentSlot.FEET).getItem() == RelixItems.SACRED_GOLD_BOOTS.get()
        ) {
            if (event.getEffectInstance().getEffect().value() == RelixEffects.TIME_DILATION.get()) {
                event.setResult(Result.DO_NOT_APPLY);
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof PharaohEntity pharaoh)) return;
        if (!pharaoh.hasLivingDead()) return;

        pharaoh.identifyLivingDead();
    }

    @SubscribeEvent
    public void onHeaddressUse(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Entity target = event.getTarget();
        if (!(
            (
                target instanceof Cat cat &&
                cat.isTame()
            ) | target instanceof Ocelot
        )) return;
        if (!(target instanceof LivingEntity livingEntity)) return;

        ItemStack stack = event.getItemStack();
        if (stack.is(RelixItems.PHARAOH_HEADDRESS.get())) {
            if (RelixAttachments.hasHeaddress(livingEntity)) return;

            RelixAttachments.setHasHeaddress(livingEntity, true);
            if (!event.getEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }
            event.getEntity().swing(event.getHand());
            event.getLevel().playSound(
                null,
                event.getEntity().blockPosition(),
                SoundEvents.ARMOR_EQUIP_GOLD.value(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else if (stack.canPerformAction(ItemAbilities.SHEARS_HARVEST)) {
            if (!RelixAttachments.hasHeaddress(livingEntity)) return;
            RelixAttachments.setHasHeaddress(livingEntity, false);
            livingEntity.spawnAtLocation(new ItemStack(
                RelixItems.PHARAOH_HEADDRESS.get(),
                1
            ));
            event.getEntity().swing(event.getHand());
            event.getLevel().playSound(
                null,
                event.getEntity().blockPosition(),
                SoundEvents.SHEEP_SHEAR,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public void registerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.CARTOGRAPHER) return;
        event.getTrades().get(2).add(new DesertPyramidMapTrade());
    }
}
