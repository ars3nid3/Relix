package arsenide.relix.menu;

import java.util.function.Supplier;

import arsenide.relix.Relix;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelixMenus {
    
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Relix.MODID);
    public static final Supplier<MenuType<TabletTableMenu>> TABLET_TABLE_MENU = MENUS.register(
        "tablet_table_menu",
        () -> IMenuTypeExtension.create(TabletTableMenu::new)
    );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
