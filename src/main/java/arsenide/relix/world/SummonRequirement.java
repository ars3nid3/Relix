package arsenide.relix.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import arsenide.relix.Relix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;

public record SummonRequirement (
    RequirementType requirementType,
    String value,
    String clueText
) {
    public static SummonRequirement parse(String raw) {
        // Expect format: <type>:<value>(<clue_text>)
        // Clue text is optional
        String[] split = raw.split(":", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException(
                "Invalid requirement format: " + raw
            );
        }
        String[] valueSplit = split[1].split("\\(", 2);
        if (valueSplit.length == 1) {
            return new SummonRequirement(
                RequirementType.fromPrefix(split[0]),
                valueSplit[0],
                ""
            );
        }
        return new SummonRequirement(
            RequirementType.fromPrefix(split[0]),
            valueSplit[0],
            // Remove trailing parenthesis
            valueSplit[1].substring(0, valueSplit[1].length() - 1)
        );
    }

    public static final Codec<SummonRequirement> CODEC = 
        RecordCodecBuilder.create(instance  -> 
            instance.group(
                RequirementType.CODEC.fieldOf("type")
                    .forGetter(SummonRequirement::requirementType),
                Codec.STRING.fieldOf("value")
                    .forGetter(SummonRequirement::value),
                Codec.STRING.fieldOf("clue_text")
                    .forGetter(SummonRequirement::clueText)
            )
            .apply(instance, SummonRequirement::new)
        );

    public Component getClueText() {
        String clueText = switch (requirementType) {
            case DAY_PHASE -> "clue.relix.day_phase." + value;
            case BIOME -> "clue.relix.biome." + value;
            case BLOCK_INTERACT -> "clue.relix.block_interact." + value;
            case ITEM_INTERACT -> "clue.relix.item_interact." + value;
            case HELD_ITEM_OFFHAND -> "clue.relix.held_item_offhand." + value;
            case HELD_ITEM_HEAD -> "clue.relix.held_item_head." + value;
            case HELD_ITEM_CHEST -> "clue.relix.held_item_chest." + value;
            case HELD_ITEM_PANTS -> "clue.relix.held_item_pants." + value;
            case HELD_ITEM_BOOTS -> "clue.relix.held_item_boots." + value;
            default -> "clue.relix.unknown";
        };
        return Component.translatable(clueText);
    }

    public boolean isMet(ServerLevel level, BlockPos pos, Player player) {
        return switch (requirementType) {
            case DAY_PHASE -> checkDayPhase(level, value);
            case BIOME -> checkBiome(level, pos, value);
            case BLOCK_INTERACT -> checkInteractedBlock(level, pos, value);
            case ITEM_INTERACT -> checkInteractedItem(player, value);
            case HELD_ITEM_OFFHAND -> checkHeldItem(player, value, EquipmentSlot.OFFHAND);
            case HELD_ITEM_HEAD -> checkHeldItem(player, value, EquipmentSlot.HEAD);
            case HELD_ITEM_CHEST -> checkHeldItem(player, value, EquipmentSlot.CHEST);
            case HELD_ITEM_PANTS -> checkHeldItem(player, value, EquipmentSlot.LEGS);
            case HELD_ITEM_BOOTS -> checkHeldItem(player, value, EquipmentSlot.FEET);
            default -> {
                Relix.LOGGER.warn("Unknown requirement type: " + requirementType);
                yield false;
            }
        };
    }

    private boolean checkDayPhase(ServerLevel level, String value) {
        // TODO: Fix this
        // Don't like this magic number, but cannot figure out how to get the duration rn
        long dayTime = level.getOverworldClockTime() % 24000;
        return switch (value) {
            // Officially only between 23000 and 0 but 1000 for extra leniency
            case "dawn" -> dayTime < 500 || dayTime >= 22500;
            // Officially only between 12000 and 13000 but 1000 for extra leniency
            case "dusk" -> dayTime >= 11500 && dayTime < 13000;
            case "day" -> dayTime >= 500 && dayTime < 11500;
            case "night" -> dayTime >= 13000 && dayTime < 22500; 
            default -> false;
        };
    }

    private boolean checkBiome(ServerLevel level, BlockPos pos, String value) {
        Holder<Biome> biome = level.getBiome(pos);
        return switch (value) {
            // Ewww hardcoded biome for desert but whatever
            case "desert" -> biome.is(Biomes.DESERT);
            case "badlands" -> biome.is(BiomeTags.IS_BADLANDS);
            case "savanna" -> biome.is(BiomeTags.IS_SAVANNA);
            default -> false;
        };
    }

    private boolean checkInteractedBlock(ServerLevel level, BlockPos pos, String value) {
        BlockState state = level.getBlockState(pos);
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String namespaceValue = String.join(":", value.split("\\."));
        String nameSpace = "minecraft";
        String path = namespaceValue;
        if (namespaceValue.contains(":")) {
            String[] split = namespaceValue.split("\\:", 2);
            nameSpace = split[0];
            path = split[1];
        }
        return id.getNamespace().equals(nameSpace) && id.getPath().equals(path);
    }

    private boolean checkInteractedItem(Player player, String value) {
        Identifier id = BuiltInRegistries.ITEM.getKey(
            player.getMainHandItem().getItem()
        );
        String namespaceValue = String.join(":", value.split("\\."));
        String nameSpace = "minecraft";
        String path = namespaceValue;
        if (namespaceValue.contains(":")) {
            String[] split = namespaceValue.split("\\:", 2);
            nameSpace = split[0];
            path = split[1];
        }
        return id.getNamespace().equals(nameSpace) && id.getPath().equals(path);
    }

    private boolean checkHeldItem(Player player, String value, EquipmentSlot slot) {
        Identifier id = BuiltInRegistries.ITEM.getKey(
            player.getItemBySlot(slot).getItem()
        );
        String namespaceValue = String.join(":", value.split("\\."));
        String nameSpace = "minecraft";
        String path = namespaceValue;
        if (namespaceValue.contains(":")) {
            String[] split = namespaceValue.split("\\:", 2);
            nameSpace = split[0];
            path = split[1];
        }
        return id.getNamespace().equals(nameSpace) && id.getPath().equals(path);
    }
}
