package arsenide.relix.structures;

import java.util.ArrayList;

import arsenide.relix.Relix;
import arsenide.relix.mixins.StructurePieceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class DesertPyramidDecorator {

    private static final ResourceKey<LootTable> HIEROGLYPH_TABLET_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(Relix.MODID, "archaeology/hieroglyph_tablet")
    );

    public static void addPiles(
        WorldGenLevel level,
        RandomSource random,
        DesertPyramidPiece piece,
        BoundingBox box
    ) {
        // Get the center terracotta piece of the pyramid
        BlockPos centerPos = ((StructurePieceAccessor) piece).relix$getWorldPos(10, 0, 10);

        int pileCount = random.nextInt(3) + 5;
        int numberGuaranteedTablets = random.nextInt(2) + 2;
        int guaranteedTabletPlaced = 0;

        for (int pileNum = 0; pileNum < pileCount; pileNum++) {
            int tryCount = 0;
            int foundY = -1;
            int pileX = 0;
            int pileZ = 0;
            while (tryCount < 3 && foundY == -1) {
                pileX = random.nextInt(18) + centerPos.getX() - 9;
                pileZ = random.nextInt(18) + centerPos.getZ() - 9;
                foundY = findPileFloorY(
                    level,
                    pileX,
                    centerPos.getY(),
                    box.maxY(),
                    pileZ
                );
                tryCount++;
            }
            if (foundY == -1) {
                Relix.LOGGER.warn(
                    "Failed to find suitable floor for pile after {} tries",
                    tryCount
                );
                continue;
            }

            boolean guaranteeTablet = guaranteedTabletPlaced < numberGuaranteedTablets;
            int numBlocksPlaced = placePile(
                level,
                random,
                new BlockPos(pileX, foundY, pileZ),
                guaranteeTablet
            );
            if (guaranteeTablet && numBlocksPlaced > 0) {
                guaranteedTabletPlaced++;
            }
        }
    }

    private static int findPileFloorY(
        WorldGenLevel level,
        int pileX,
        int startY,
        int endY,
        int pileZ
    ) {
        int foundY = -1;
        for (int y = startY; y < endY; y++) {
            BlockPos evaluatedPos = new BlockPos(pileX, y, pileZ);
            if (foundY == -1 && level.getBlockState(evaluatedPos).is(Blocks.AIR)) {
                foundY = y;
            } else if (foundY != -1 && !level.getBlockState(evaluatedPos).is(Blocks.AIR)) {
                return foundY;
            }
        }
        return -1;
    }

    private static int placePile(
        WorldGenLevel level,
        RandomSource random,
        BlockPos pilePos,
        boolean guaranteeTablet
    ) {
        ArrayList<BlockPos> pileBlocks = new ArrayList<>();
        int maxPileSize = random.nextInt(4) + 3;
        int pileSizeCount = 1;
        BlockPos nextPos = pilePos;
        while (pileSizeCount < maxPileSize) {
            if (level.getBlockState(nextPos).is(Blocks.AIR)) {
                pileBlocks.add(nextPos);
                BlockPos nextPosGround = findFloorPos(level, nextPos);
                level.setBlock(nextPosGround, Blocks.SAND.defaultBlockState(), 3);
            }
            nextPos = getNextPilePos(pileSizeCount, nextPos, pileBlocks);
            pileSizeCount++;
        }

        if (pileBlocks.isEmpty()) {
            return 0;
        }

        BlockPos randomBlock = pileBlocks.get(random.nextInt(pileBlocks.size()));
        level.setBlock(randomBlock, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
        BlockEntity suspiciousEntity = level.getBlockEntity(randomBlock);
        ResourceKey<LootTable> lootKey = guaranteeTablet
            ? HIEROGLYPH_TABLET_LOOT
            : BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY;
        if (suspiciousEntity instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(lootKey, random.nextLong());
        }
        return pileBlocks.size();
    }

    private static BlockPos findFloorPos(
        WorldGenLevel level,
        BlockPos pos
    ) {
        for (int y = pos.getY() - 1; y >= level.getMinY(); y--) {
            BlockPos evaluatedPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!level.getBlockState(evaluatedPos).is(Blocks.AIR)) {
                return pos;
            }
            pos = pos.below();
        }
        return pos;
    }

    private static BlockPos getNextPilePos(
        int pileSizeCount,
        BlockPos currentPos,
        ArrayList<BlockPos> pileBlocks
    ) {
        if (pileSizeCount % 2 == 1) {
            return currentPos.above();
        } else {
            BlockPos centerPos = pileBlocks.get(0);
            BlockPos[] possiblePositions = {
                centerPos.north(),
                centerPos.south(),
                centerPos.east(),
                centerPos.west()
            };
            for (BlockPos possiblePos : possiblePositions) {
                if (!pileBlocks.contains(possiblePos)) {
                    return possiblePos;
                }
            }
            return currentPos;
        }
    }
}
