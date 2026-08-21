package arsenide.relix.menu.data;

import arsenide.relix.blocks.entity.TabletTableBlockEntity;
import net.minecraft.world.inventory.ContainerData;

public class TabletTableData implements ContainerData {

    private final TabletTableBlockEntity blockEntity;

    public TabletTableData(TabletTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int get(int dataId) {
        return switch (dataId) {
            case 0 -> (int) blockEntity.getRandomSeed();
            case 1 -> (int) (blockEntity.getRandomSeed() >>> 32);
            default -> {
                if (dataId < 0 || dataId - 1 > blockEntity.getClueCount()) {
                    throw new IndexOutOfBoundsException("Data ID out of bounds. Tried to access data ID " + dataId + "but only " + (blockEntity.getClueCount() + 2) + " data IDs are available");
                }
                yield blockEntity.isClueUncovered(dataId - 2) ? 1 : 0;
            }
        };
    }

    @Override
    public void set(int dataId, int value) {}

    @Override
    public int getCount() {
        return blockEntity.getClueCount() + 2;
    }
    
}
