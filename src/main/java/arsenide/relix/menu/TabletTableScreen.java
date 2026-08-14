package arsenide.relix.menu;

import java.util.ArrayList;
import arsenide.relix.Relix;
import arsenide.relix.blocks.entity.TabletTableBlockEntity;
import arsenide.relix.menu.components.TabletButton;
import arsenide.relix.menu.components.TabletDisplayWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class TabletTableScreen extends AbstractContainerScreen<TabletTableMenu> {
    
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        Relix.MODID, "textures/gui/container/tablet_table.png"
    );

    private TabletButton tabletButton;
    private ArrayList<TabletDisplayWidget> tabletDisplayWidgets = new ArrayList<>();

    public TabletTableScreen(TabletTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 178);
    }

    @Override
    protected void init() {
        super.init();
        tabletButton = new TabletButton(
            leftPos + 14,
            topPos + 51,
            18,
            18,
            Component.empty(),
            button -> {
                minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    0
                );
            },
            defaultNarration -> Component.translatable(
                "gui.relix.tablet_table.analyze_tablet"
            )
        );
        addRenderableWidget(tabletButton);

        int widgetWidth = 113;
        int widgetHeight = 10;

        tabletDisplayWidgets.clear();
        for (int i = 0; i < TabletTableBlockEntity.CLUE_COUNT; i++) {
            TabletDisplayWidget widget = new TabletDisplayWidget(
                leftPos + 47,
                topPos + 22 + i * (widgetHeight + 1),
                widgetWidth,
                widgetHeight,
                menu.getClueText(i),
                menu.isClueUncovered(i),
                menu.getRandomSeed() + (long)i
            );
            tabletDisplayWidgets.add(widget);
            addRenderableOnly(widget);
        }
    }

    @Override
    public void extractBackground(
        GuiGraphicsExtractor graphics, 
        int mouseX, 
        int mouseY, 
        float partialTick
    ) {
        graphics.blit(
            TEXTURE,
            leftPos,
            topPos,
            leftPos + imageWidth,
            topPos + imageHeight,
            0.0F,
            imageWidth / 256.0F,
            0.0F,
            imageHeight / 256.0F
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tabletButton.active = menu.hasTablet();
        for (int i = 0; i < tabletDisplayWidgets.size(); i++) {
            tabletDisplayWidgets.get(i).setUncovered(menu.isClueUncovered(i));
        }
    }
}
