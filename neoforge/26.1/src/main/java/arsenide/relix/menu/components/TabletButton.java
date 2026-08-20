package arsenide.relix.menu.components;

import arsenide.relix.Relix;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TabletButton extends Button {

    private static final Identifier BUTTON_DISABLED =
        Identifier.fromNamespaceAndPath(
            Relix.MODID, "textures/gui/sprites/button_disabled.png"
    );
    private static final Identifier BUTTON_ENABLED =
        Identifier.fromNamespaceAndPath(
            Relix.MODID, "textures/gui/sprites/button.png"
    );
    private static final Identifier BUTTON_HOVERED =
        Identifier.fromNamespaceAndPath(
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

    private Identifier getTexture() {
        if (!active) {
            return BUTTON_DISABLED;
        }
        if (isHovered()) {
            return BUTTON_HOVERED;
        }
        return BUTTON_ENABLED;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(
            getTexture(),
            getX(),
            getY(),
            getX() + width,
            getY() + height,
            0f,
            1f,
            0f,
            1f
        );
    }
    
    
}
