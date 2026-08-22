package arsenide.relix.world;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import arsenide.relix.Relix;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
public class RelixWorldData extends SavedData {

    public static final String FILE_ID = ResourceLocation.fromNamespaceAndPath(
        Relix.MODID, 
        "world_data"
    ).toString();

    public static final Codec<RelixWorldData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            SummonRequirement.CODEC.listOf()
                .fieldOf("pharaoh_conditions")
                .forGetter(RelixWorldData::pharaohConditions)
        ).apply(instance, RelixWorldData::new)
    );

    public static SavedData.Factory<RelixWorldData> factory() {
        return new SavedData.Factory<>(
            () -> { throw new UnsupportedOperationException("Create via level"); },
            RelixWorldData::load,
            null
        );
    }

    private List<SummonRequirement> pharaohConditions;

    public RelixWorldData(ServerLevel level) {
        this(rollPharaohConditions(RandomSource.create(level.getSeed())));
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

    public void reroll(ServerLevel level) {
        this.pharaohConditions = rollPharaohConditions(level.getRandom());
        this.setDirty();
    }

    private static List<SummonRequirement> rollPharaohConditions(RandomSource random) {
        return List.of(
            Util.getRandom(PharaohRequirementOptions.TIME_OF_DAY, random),
            Util.getRandom(PharaohRequirementOptions.BIOME, random),
            Util.getRandom(PharaohRequirementOptions.PEDESTAL, random),
            Util.getRandom(PharaohRequirementOptions.OFFERING, random),
            Util.getRandom(PharaohRequirementOptions.EQUIPMENT, random)
        );
    }

    public static RelixWorldData load(CompoundTag tag, Provider provider) {
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return CODEC.parse(ops, tag).getOrThrow();
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider provider) {
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return (CompoundTag) CODEC.encodeStart(ops, this).getOrThrow();
    }
    
}