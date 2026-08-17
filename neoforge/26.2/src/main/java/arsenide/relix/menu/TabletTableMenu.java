package arsenide.relix.menu;

import java.util.ArrayList;
import java.util.List;

import arsenide.relix.blocks.RelixBlocks;
import arsenide.relix.blocks.entity.TabletTableBlockEntity;
import arsenide.relix.items.RelixItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class TabletTableMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final TabletTableBlockEntity blockEntity;
    private final List<Component> clueTexts;

    // Client constructor
    public TabletTableMenu(
        int containerId, 
        Inventory playerInv,
        RegistryFriendlyByteBuf buffer
    ) {
        this(
            containerId, 
            playerInv, 
            ContainerLevelAccess.NULL,
            new ItemStacksResourceHandler(1),
            new SimpleContainerData(TabletTableBlockEntity.CLUE_COUNT + 2),
            null
        );
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            clueTexts.add(
                ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer)
            );
        }
    }

    // Server constructor
    public TabletTableMenu(
        int containerId, 
        Inventory playerInv, 
        ContainerLevelAccess access,
        ItemStacksResourceHandler inventory,
        ContainerData data,
        TabletTableBlockEntity blockEntity
    ) {
        super(RelixMenus.TABLET_TABLE_MENU.get(), containerId);
        this.access = access;
        // Tablet slot
        this.addSlot(new ResourceHandlerSlot(inventory, inventory::set, 0, 15, 30));
        // Player inventory slots
        this.addStandardInventorySlots(playerInv, 8, 96);

        // Add data slots
        this.data = data;
        this.addDataSlots(data);

        if (blockEntity != null) {
            // Store clue texts
            this.clueTexts = blockEntity.getClueTexts();
        } else {
            this.clueTexts = new ArrayList<>();
        }

        // Store block entity
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(slotIndex);
        // Check if the slot exists and has an item
        if (quickMovedSlot == null || !quickMovedSlot.hasItem()) {
            return quickMovedStack;
        }
        // Create a copy of the item stack
        ItemStack rawStack = quickMovedSlot.getItem();
        quickMovedStack = rawStack.copy();
        // Check if we are shift clicking the tablet slot
        if (slotIndex == 0) {
            // Try to move to the player inventory (1-36)
            if (!this.moveItemStackTo(rawStack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        // Check if we are clicking a player inventory slot
        } else if (slotIndex >= 1 && slotIndex < 37) {
            // Try to move to the tablet slot (0)
            if (!this.moveItemStackTo(rawStack, 0, 1, false)) {
                // Otherwise, if we are clicking not in the hotbar (1-28), try to move to hotbar
                if (slotIndex < 29) {
                    if (!this.moveItemStackTo(rawStack, 29, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                // Otherwise, if we are clicking in the hotbar (29-36), try to move to player inventory
                } else {
                    if (!this.moveItemStackTo(rawStack, 1, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        // Update the slot that the itemstack was moved from
        if (rawStack.isEmpty()) {
            quickMovedSlot.set(ItemStack.EMPTY);
        } else {
            quickMovedSlot.setChanged();
        }
        quickMovedSlot.onTake(player, rawStack);
        return quickMovedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, RelixBlocks.TABLET_TABLE.get());
    }

    public boolean hasTablet() {
        ItemStack stack = this.getSlot(0).getItem();
        return !stack.isEmpty() && stack.getItem() == RelixItems.HIEROGLYPH_TABLET.get();
    }

    private boolean hasCluesToUncover() {
        for (int i = 0; i < data.getCount() - 2; i++) {
            if (!isClueUncovered(i)) {
                return true;
            }
        }
        return false;
    }

    private void uncoverNextClue() {
        if (blockEntity == null) {
            return;
        }
        for (int i = 0; i < data.getCount() - 2; i++) {
            if (!isClueUncovered(i)) {
                blockEntity.setClueUncovered(i, true);
                return;
            }
        }
    }

    public boolean consumeTablet() {
        Slot tabletSlot = this.getSlot(0);
        ItemStack stack = tabletSlot.getItem();
        if (stack.isEmpty() || stack.getItem() != RelixItems.HIEROGLYPH_TABLET.get()) {
            return false;
        }
        if (!hasCluesToUncover()) {
            return false;
        }
        stack.shrink(1);
        uncoverNextClue();
        tabletSlot.set(stack);
        tabletSlot.setChanged();

        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0) {
            return consumeTablet();
        }
        return false;
    }

    public long getRandomSeed() {
        return ((long) data.get(1) << 32) | (data.get(0) & 0xFFFFFFFFL);
    }

    public boolean isClueUncovered(int index) {
        return data.get(index + 2) == 1;
    }

    public Component getClueText(int index) {
        return clueTexts.get(index);
    }
}
