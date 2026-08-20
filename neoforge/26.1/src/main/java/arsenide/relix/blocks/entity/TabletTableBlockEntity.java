package arsenide.relix.blocks.entity;

import java.util.List;

import org.jspecify.annotations.Nullable;

import arsenide.relix.blocks.RelixBlocks;
import arsenide.relix.blocks.TabletTableBlock;
import arsenide.relix.items.RelixItems;
import arsenide.relix.menu.TabletTableMenu;
import arsenide.relix.menu.data.TabletTableData;
import arsenide.relix.world.RelixWorldData;
import arsenide.relix.world.SummonRequirement;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class TabletTableBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INVENTORY_SIZE = 1;
    public static final int CLUE_COUNT = 5;

    // Stupid ugly guard flag implementation, signals that we should not
    // interact with the world yet
    private boolean hasLoaded = false;

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(INVENTORY_SIZE) {
        // Override isValid here and not at slot level: otherwise the hoppers will be angry :(
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.getItem() == RelixItems.HIEROGLYPH_TABLET.get();
        };

        @Override
        public void onContentsChanged(int slot, ItemStack stack) {
            super.onContentsChanged(slot, stack);
            setChanged();
            if (!hasLoaded) return;
            Level level = getLevel();
            if (!level.isClientSide()) {
                BlockState blockState = level.getBlockState(worldPosition);
                boolean hasTablet = !inventory.copyToList().get(slot).isEmpty();
                if (blockState.getValue(TabletTableBlock.HAS_TABLET) != hasTablet) {
                    level.setBlock(
                        worldPosition, 
                        blockState.setValue(TabletTableBlock.HAS_TABLET, hasTablet),
                        Block.UPDATE_ALL
                    );
                }
            }
        }
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

    public ResourceHandler<ItemResource> getInventory() {
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
        ValueOutput hieroglyphs = output.child("Hieroglyphs");
        hieroglyphs.putLong("Seed", randomSeed);
        for (int i = 0; i < uncoveredClues.length; i++) {
            hieroglyphs.putBoolean(
                "UncoveredClue" + i, 
                uncoveredClues[i]
            );
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hasLoaded = false;
        inventory.deserialize(input.childOrEmpty("Inventory"));
        ValueInput hieroglyphs = input.childOrEmpty("Hieroglyphs");
        long loadedSeed = hieroglyphs.getLongOr(
            "Seed", 
            (long) 0
        );
        if (loadedSeed != 0) {
            randomSeed = loadedSeed;
        }
        for (int i = 0; i < uncoveredClues.length; i++) {
            uncoveredClues[i] = hieroglyphs.getBooleanOr(
                "UncoveredClue" + i,
                false
            );
        }
        hasLoaded = true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        Containers.dropContents(level, pos, inventory.copyToList());
    }

    public List<Component> getClueTexts() {
        if (level instanceof ServerLevel serverLevel) {
            List<SummonRequirement> pharaohConditions = 
                serverLevel.getDataStorage().get(RelixWorldData.KEY).pharaohConditions();
                return pharaohConditions.stream()
                .map(SummonRequirement::getClueText)
                .toList();
        }
        return List.of();
    }
    
}
