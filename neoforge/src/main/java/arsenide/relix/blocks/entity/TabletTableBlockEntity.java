package arsenide.relix.blocks.entity;

import java.util.List;

import javax.annotation.Nullable;

import arsenide.relix.blocks.RelixBlocks;
import arsenide.relix.blocks.TabletTableBlock;
import arsenide.relix.items.RelixItems;
import arsenide.relix.menu.TabletTableMenu;
import arsenide.relix.menu.data.TabletTableData;
import arsenide.relix.world.RelixWorldData;
import arsenide.relix.world.SummonRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
public class TabletTableBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INVENTORY_SIZE = 1;
    public static final int CLUE_COUNT = 5;

    // Stupid ugly guard flag implementation, signals that we should not
    // interact with the world yet
    private boolean hasLoaded = false;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(RelixItems.HIEROGLYPH_TABLET.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
            if (!hasLoaded) return;
            Level level = getLevel();
            if (level != null && !level.isClientSide()) {
                BlockState blockState = level.getBlockState(worldPosition);
                boolean hasTablet = !getStackInSlot(slot).isEmpty();
                if (blockState.getValue(TabletTableBlock.HAS_TABLET) != hasTablet) {
                    level.setBlock(
                        worldPosition,
                        blockState.setValue(TabletTableBlock.HAS_TABLET, hasTablet),
                        Block.UPDATE_ALL
                    );
                }
            }
        };
    };

    private long randomSeed = RandomSource.create().nextLong();
    private final boolean[] uncoveredClues = new boolean[CLUE_COUNT]; 


    public TabletTableBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(RelixBlocks.TABLET_TABLE_ENTITY.get(), worldPosition, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.relix.tablet_table");
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public boolean isClueUncovered(int index) {
        return uncoveredClues[index];
    }

    public int getClueCount() {
        return uncoveredClues.length;
    }

    public void setClueUncovered(int index, boolean uncovered) {
        if (index < 0 || index >= uncoveredClues.length) {
            return;
        }
        uncoveredClues[index] = uncovered;
        setChanged();
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new TabletTableMenu(
            containerId, 
            playerInv, 
            ContainerLevelAccess.create(getLevel(), getBlockPos()),
            inventory,
            new TabletTableData(this),
            this
        );
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Inventory", inventory.serializeNBT(provider));
        CompoundTag hieroglyphs = new CompoundTag();
        hieroglyphs.putLong("Seed", randomSeed);
        for (int i = 0; i < uncoveredClues.length; i++) {
            hieroglyphs.putBoolean(
                "UncoveredClue" + i, 
                uncoveredClues[i]
            );
        }
        tag.put("Hieroglyphs", hieroglyphs);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        hasLoaded = false;
        if (tag.contains("Inventory", Tag.TAG_COMPOUND)) {
            inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
        }
        CompoundTag hieroglyphs = tag.getCompound("Hieroglyphs");
        long loadedSeed = hieroglyphs.getLong("Seed"); 
        if (loadedSeed != 0) {
            randomSeed = loadedSeed;
        }
        for (int i = 0; i < uncoveredClues.length; i++) {
            uncoveredClues[i] = hieroglyphs.getBoolean("UncoveredClue" + i);
        }
        hasLoaded = true;
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            Containers.dropItemStack(
                level, 
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                inventory.getStackInSlot(0) 
            );
        }
        super.setRemoved();
    }

    public List<Component> getClueTexts() {
        if (level instanceof ServerLevel serverLevel) {
            RelixWorldData worldData = serverLevel.getDataStorage().computeIfAbsent(
                RelixWorldData.factory(),
                RelixWorldData.FILE_ID
            );
            if (worldData == null) {
                worldData = new RelixWorldData(serverLevel);
                serverLevel.getDataStorage().set(RelixWorldData.FILE_ID, worldData);
            }
            List<SummonRequirement> pharaohConditions = 
                worldData.pharaohConditions();
                return pharaohConditions.stream()
                .map(SummonRequirement::getClueText)
                .toList();
        }
        return List.of();
    }
    
}
