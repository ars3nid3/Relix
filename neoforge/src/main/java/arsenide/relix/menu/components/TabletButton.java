package arsenide.relix.menu.components;

import arsenide.relix.Relix;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TabletButton extends Button {

    private static final ResourceLocation BUTTON_DISABLED =
        ResourceLocation.fromNamespaceAndPath(
            Relix.MODID, "textures/gui/sprites/button_disabled.png"
    );
    private static final ResourceLocation BUTTON_ENABLED =
        ResourceLocation.fromNamespaceAndPath(
            Relix.MODID, "textures/gui/sprites/button.png"
    );
    private static final ResourceLocation BUTTON_HOVERED =
        ResourceLocation.fromNamespaceAndPath(
            Relix.MODID, "textures/gui/sprites/button_highlight.png"
    );

    public TabletButton(
        int x, 
        int y, 
        int width, 
        int height, 
        Component message, 
        Button.OnPress onPress,
        Button.CreateNarration createNarration
    ) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    private ResourceLocation getTexture() {
        if (!active) {
            return BUTTON_DISABLED;
        }
        if (isHovered()) {
            return BUTTON_HOVERED;
        }
        return BUTTON_ENABLED;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(
            getTexture(),
            getX(),
            getY(),
            width,
            height
        );
    }
    
    
}
