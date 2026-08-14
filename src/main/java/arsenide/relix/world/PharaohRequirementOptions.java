package arsenide.relix.world;

import java.util.List;

// TODO: at some point we make this data-driven.
public final class PharaohRequirementOptions {
    private PharaohRequirementOptions() {}

    public static final List<SummonRequirement> TIME_OF_DAY = List.of(
        SummonRequirement.parse("phase:dawn"),
        SummonRequirement.parse("phase:day"),
        SummonRequirement.parse("phase:dusk"),
        SummonRequirement.parse("phase:night")
    );

    public static final List<SummonRequirement> BIOME = List.of(
        SummonRequirement.parse("biome:desert"),
        SummonRequirement.parse("biome:badlands"),
        SummonRequirement.parse("biome:savanna")
    );

    public static final List<SummonRequirement> PEDESTAL = List.of(
        SummonRequirement.parse("block:chiseled_sandstone"),
        SummonRequirement.parse("block:chiseled_red_sandstone"),
        SummonRequirement.parse("block:chiseled_stone_bricks")
    );

    public static final List<SummonRequirement> OFFERING = List.of(
        SummonRequirement.parse("item:golden_apple"),
        SummonRequirement.parse("item:rabbit_foot"),
        SummonRequirement.parse("item:trial_key"),
        SummonRequirement.parse("item:totem_of_undying")
    );
    
    public static final List<SummonRequirement> EQUIPMENT = List.of(
        SummonRequirement.parse("head:skeleton_skull"),
        SummonRequirement.parse("head:carved_pumpkin"),
        SummonRequirement.parse("offhand:golden_hoe"),
        SummonRequirement.parse("offhand:relix.hieroglyph_tablet")
    );   
}
