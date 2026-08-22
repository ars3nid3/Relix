package arsenide.relix.menu;

import java.util.ArrayList;
import arsenide.relix.Relix;
import arsenide.relix.blocks.entity.TabletTableBlockEntity;
import arsenide.relix.menu.components.TabletButton;
import arsenide.relix.menu.components.TabletDisplayWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TabletTableScreen extends AbstractContainerScreen<TabletTableMenu> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Relix.MODID, "textures/gui/container/tablet_table.png"
    );

    private TabletButton tabletButton;
    private ArrayList<TabletDisplayWidget> tabletDisplayWidgets = new ArrayList<>();

    public TabletTableScreen(TabletTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.width = 176;
        this.height = 178;
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
    public void renderBg(
        GuiGraphics graphics,
        float partialTick, 
        int mouseX, 
        int mouseY
    ) {
        graphics.blitSprite(
            TEXTURE,
            leftPos,
            topPos,
            imageWidth,
            imageHeight
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
