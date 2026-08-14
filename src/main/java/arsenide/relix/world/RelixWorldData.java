package arsenide.relix.world;

import java.util.List;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import arsenide.relix.Relix;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class RelixWorldData extends SavedData {

    public static final SavedDataType<RelixWorldData> KEY = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(Relix.MODID, "world_data"),
        RelixWorldData::new,
        level -> RecordCodecBuilder.create(instance -> instance.group(
            SummonRequirement.CODEC.listOf()
                .fieldOf("pharaoh_conditions")
                .forGetter(RelixWorldData::pharaohConditions)
        ).apply(instance, RelixWorldData::new))
    );

    private final List<SummonRequirement> pharaohConditions;

    public RelixWorldData(ServerLevel level) {
        RandomSource random = RandomSource.create(level.getSeed());

        List<SummonRequirement> pharaohConditions = List.of(
            Util.getRandom(PharaohRequirementOptions.TIME_OF_DAY, random),
            Util.getRandom(PharaohRequirementOptions.BIOME, random),
            Util.getRandom(PharaohRequirementOptions.PEDESTAL, random),
            Util.getRandom(PharaohRequirementOptions.OFFERING, random),
            Util.getRandom(PharaohRequirementOptions.EQUIPMENT, random)
        );
        this.pharaohConditions = pharaohConditions;

        this.setDirty();
    }

    public RelixWorldData(
        List<SummonRequirement> pharaohConditions
    ) {
        this.pharaohConditions = pharaohConditions;
        this.setDirty();
    }

    public List<SummonRequirement> pharaohConditions() {
        return pharaohConditions;
    }
    
}