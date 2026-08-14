package arsenide.relix.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import arsenide.relix.structures.DesertPyramidDecorator;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;

@Mixin(StructureStart.class)
public class DesertPyramidMixin {

    @Unique
    private int relix$chunksProcessed = 0;

    @Unique
    private boolean relix$decorated = false;

    @Inject(
        method = "placeInChunk",
        at = @At("TAIL")
    )
    private void relix$afterStructurePlaced(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator generator,
        RandomSource random,
        BoundingBox chunkBB,
        ChunkPos chunkPos,
        CallbackInfo ci
    ) {
        StructureStart self = (StructureStart)(Object)this;

        if (!self.getStructure().type().equals(StructureType.DESERT_PYRAMID)) {
            return;
        }

        if (this.relix$decorated) {
            return;
        }

        this.relix$chunksProcessed++;
        BoundingBox box = self.getBoundingBox();
        if (this.relix$chunksProcessed < relix$countIntersectingChunks(box)) {
            return;
        }

        DesertPyramidPiece piece = null;
        for (StructurePiece structurePiece : self.getPieces()) {
            if (structurePiece instanceof DesertPyramidPiece desertPyramidPiece) {
                piece = desertPyramidPiece;
                break;
            }
        }

        if (piece == null) {
            return;
        }

        this.relix$decorated = true;
        RandomSource decoRandom = RandomSource.create(level.getSeed())
            .forkPositional()
            .at(box.getCenter());
        DesertPyramidDecorator.addPiles(level, decoRandom, piece, box);
    }

    @Unique
    private static int relix$countIntersectingChunks(BoundingBox box) {
        int minChunkX = box.minX() >> 4;
        int maxChunkX = box.maxX() >> 4;
        int minChunkZ = box.minZ() >> 4;
        int maxChunkZ = box.maxZ() >> 4;
        return (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
    }
}
