package arsenide.relix.world;

import java.util.List;

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
    String[] values,
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
        String[] valueOptions = valueSplit[0].split("\\|");
        if (valueSplit.length == 1) {
            return new SummonRequirement(
                RequirementType.fromPrefix(split[0]),
                valueOptions,
                ""
            );
        }
        return new SummonRequirement(
            RequirementType.fromPrefix(split[0]),
            valueOptions,
            // Remove trailing parenthesis
            valueSplit[1].substring(0, valueSplit[1].length() - 1)
        );
    }

    private static final Codec<SummonRequirement> CURRENT_CODEC =
        RecordCodecBuilder.create(instance ->
            instance.group(
                RequirementType.CODEC.fieldOf("type")
                    .forGetter(SummonRequirement::requirementType),
                Codec.STRING.listOf().fieldOf("values")
                    .forGetter(r -> List.of(r.values)),
                Codec.STRING.fieldOf("clue_text")
                    .forGetter(SummonRequirement::clueText)
            )
            .apply(instance, (type, values, clueText) ->
                new SummonRequirement(type, values.toArray(String[]::new), clueText)
            )
        );

    private static final Codec<SummonRequirement> LEGACY_CODEC =
        RecordCodecBuilder.create(instance ->
            instance.group(
                RequirementType.CODEC.fieldOf("type")
                    .forGetter(SummonRequirement::requirementType),
                Codec.STRING.fieldOf("value")
                    .forGetter(r -> r.values.length > 0 ? r.values[0] : ""),
                Codec.STRING.fieldOf("clue_text")
                    .forGetter(SummonRequirement::clueText)
            )
            .apply(instance, (type, value, clueText) ->
                new SummonRequirement(type, value.split("\\|"), clueText)
            )
        );

    public static final Codec<SummonRequirement> CODEC =
        CURRENT_CODEC.withAlternative(LEGACY_CODEC);

    public Component getClueText() {
        String valuesString = String.join("|", values);
        String clueText = switch (requirementType) {
            case DAY_PHASE -> "clue.relix.day_phase." + valuesString;
            case BIOME -> "clue.relix.biome." + valuesString;
            case BLOCK_INTERACT -> "clue.relix.block_interact." + valuesString;
            case ITEM_INTERACT -> "clue.relix.item_interact." + valuesString;
            case HELD_ITEM_OFFHAND -> "clue.relix.held_item_offhand." + valuesString;
            case HELD_ITEM_HEAD -> "clue.relix.held_item_head." + valuesString;
            case HELD_ITEM_CHEST -> "clue.relix.held_item_chest." + valuesString;
            case HELD_ITEM_PANTS -> "clue.relix.held_item_pants." + valuesString;
            case HELD_ITEM_BOOTS -> "clue.relix.held_item_boots." + valuesString;
            default -> "clue.relix.unknown";
        };
        return Component.translatable(clueText);
    }

    public boolean isMet(ServerLevel level, BlockPos pos, Player player) {
        return switch (requirementType) {
            case DAY_PHASE -> checkDayPhase(level, values);
            case BIOME -> checkBiome(level, pos, values);
            case BLOCK_INTERACT -> checkInteractedBlock(level, pos, values);
            case ITEM_INTERACT -> checkInteractedItem(player, values);
            case HELD_ITEM_OFFHAND -> checkHeldItem(player, values, EquipmentSlot.OFFHAND);
            case HELD_ITEM_HEAD -> checkHeldItem(player, values, EquipmentSlot.HEAD);
            case HELD_ITEM_CHEST -> checkHeldItem(player, values, EquipmentSlot.CHEST);
            case HELD_ITEM_PANTS -> checkHeldItem(player, values, EquipmentSlot.LEGS);
            case HELD_ITEM_BOOTS -> checkHeldItem(player, values, EquipmentSlot.FEET);
            default -> {
                Relix.LOGGER.warn("Unknown requirement type: " + requirementType);
                yield false;
            }
        };
    }

    private boolean checkDayPhase(ServerLevel level, String[] values) {
        // Don't like this magic number, but cannot figure out how to get the duration rn
        long dayTime = level.getOverworldClockTime() % 24000;
        boolean isMet = false;
        for (String value : values) {
            isMet |= switch (value) {
                case "dawn" -> dayTime < 500 || dayTime >= 22500;
                case "dusk" -> dayTime >= 11500 && dayTime < 13000;
                case "day" -> dayTime >= 500 && dayTime < 11500;
                case "night" -> dayTime >= 13000 && dayTime < 22500;
                default -> false;
            };
        }
        return isMet;
    }

    private boolean checkBiome(ServerLevel level, BlockPos pos, String[] values) {
        Holder<Biome> biome = level.getBiome(pos);
        boolean isMet = false;
        for (String value : values) {
            isMet |= switch (value) {
                case "desert" -> biome.is(Biomes.DESERT);
                case "badlands" -> biome.is(BiomeTags.IS_BADLANDS);
                case "savanna" -> biome.is(BiomeTags.IS_SAVANNA);
                default -> false;
            };
        }
        return isMet;
    }

    private boolean checkInteractedBlock(ServerLevel level, BlockPos pos, String[] values) {
        BlockState state = level.getBlockState(pos);
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        boolean isMet = false;
        for (String value : values) {
            String namespaceValue = String.join(":", value.split("\\."));
            String nameSpace = "minecraft";
            String path = namespaceValue;
            if (namespaceValue.contains(":")) {
                String[] split = namespaceValue.split("\\:", 2);
                nameSpace = split[0];
                path = split[1];
            }
            isMet |= id.getNamespace().equals(nameSpace) && id.getPath().equals(path);
        }
        return isMet;
    }

    private boolean checkInteractedItem(Player player, String[] values) {
        Identifier id = BuiltInRegistries.ITEM.getKey(
            player.getMainHandItem().getItem()
        );
        boolean isMet = false;
        for (String value : values) {
            String namespaceValue = String.join(":", value.split("\\."));
            String nameSpace = "minecraft";
            String path = namespaceValue;
            if (namespaceValue.contains(":")) {
                String[] split = namespaceValue.split("\\:", 2);
                nameSpace = split[0];
                path = split[1];
            }
            isMet |= id.getNamespace().equals(nameSpace) && id.getPath().equals(path);
        }
        return isMet;
    }

    private boolean checkHeldItem(Player player, String[] values, EquipmentSlot slot) {
        Identifier id = BuiltInRegistries.ITEM.getKey(
            player.getItemBySlot(slot).getItem()
        );
        boolean isMet = false;
        for (String value : values) {
            String namespaceValue = String.join(":", value.split("\\."));
            String nameSpace = "minecraft";
            String path = namespaceValue;
            if (namespaceValue.contains(":")) {
                String[] split = namespaceValue.split("\\:", 2);
                nameSpace = split[0];
                path = split[1];
            }
            isMet |= id.getNamespace().equals(nameSpace) && id.getPath().equals(path);
        }
        return isMet;
    }
}
