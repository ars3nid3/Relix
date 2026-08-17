package arsenide.relix.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

@Mixin(StructurePiece.class)
public interface StructurePieceAccessor {
    @Invoker("getWorldPos")
    BlockPos.MutableBlockPos relix$getWorldPos(int x, int y, int z);
}
