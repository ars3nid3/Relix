package arsenide.relix.entity.ai;

import arsenide.relix.entity.AttendantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;

public class PerformAttendantGoal extends Goal {

    private final AttendantEntity attendant;
    private final BlockPos altarPos;
    private final Level level;
    private final boolean isClientLevel;
    private int timer = 0;

    public PerformAttendantGoal(
        AttendantEntity attendant, 
        BlockPos altarPos
    ) {
        this.attendant = attendant;
        this.altarPos = altarPos;
        this.level = attendant.level();
        this.isClientLevel = level.isClientSide();
    }

    @Override
    public boolean canUse() {
        if (attendant.distanceToSqr(Vec3.atCenterOf(altarPos)) >= 7) {
            return false;
        }
        if (attendant.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            if (!attendant.isJukeboxAttendant()) {
            }
            return false;
        }
        if (!attendant.isJukeboxAttendant()) {
            return level.getBlockState(
                altarPos
            ).getBlock() == Blocks.JUKEBOX;
        }
        return true;
    }

    @Override
    public void start() {
        if (isClientLevel) {
            return;
        }
        BlockPos jukeboxPos = altarPos;
        if (attendant.isJukeboxAttendant()) {
            if (level.getBlockState(jukeboxPos).getBlock() != Blocks.JUKEBOX) {
                if (level.getBlockState(jukeboxPos).getBlock() != Blocks.AIR) {
                    attendant.swing(InteractionHand.MAIN_HAND);
                }
                level.destroyBlock(jukeboxPos, true);
                timer++;
            }
        } else {
            timer++;
        }
    }

    @Override
    public void tick() {
        timer++;
        if (isClientLevel || timer != 10) {
            return;
        }
        if (attendant.isJukeboxAttendant()) {
            level.setBlockAndUpdate(
                altarPos,
                Blocks.JUKEBOX.defaultBlockState()
            );
            level.playSound(
                null,
                altarPos,
                SoundEvents.WOOD_PLACE,
                SoundSource.BLOCKS
            );
        } else {
            BlockEntity blockEntity = level.getBlockEntity(altarPos);
            if (!(blockEntity instanceof JukeboxBlockEntity jukeboxBlockEntity)) {
                return;
            }
            jukeboxBlockEntity.setTheItem(attendant.getItemInHand(
                InteractionHand.MAIN_HAND
            ).copy());
        }
        attendant.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        attendant.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public boolean canContinueToUse() {
        return timer > 0 && timer <= 10;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
