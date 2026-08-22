package arsenide.relix.blocks;

import arsenide.relix.Relix;
import arsenide.relix.blocks.entity.TabletTableBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RelixBlocks {
    // Create a Deferred Register to hold Blocks which will all be registered under the "relix" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Relix.MODID);
    public static final DeferredBlock<TabletTableBlock> TABLET_TABLE = BLOCKS.registerBlock(
        "tablet_table", 
        TabletTableBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)
            .noOcclusion()
    );

    // Register Block Entities
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Relix.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TabletTableBlockEntity>> TABLET_TABLE_ENTITY = BLOCK_ENTITIES.register(
        "tablet_table_entity",
        () -> BlockEntityType.Builder.of(
            TabletTableBlockEntity::new,
            TABLET_TABLE.get()
        ).build(null)
    );

    // Register Block Items
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Relix.MODID);
    public static final DeferredItem<BlockItem> TABLET_TABLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(TABLET_TABLE);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }

}
