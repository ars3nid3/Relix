package arsenide.relix.menu.components;

import java.util.Locale;

import arsenide.relix.Relix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class TabletDisplayWidget implements Renderable {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private boolean uncovered;
    private Component clueText;

    private static final Identifier HIEROGLYPH_ATLAS = 
        Identifier.fromNamespaceAndPath(
            Relix.MODID, 
            "textures/gui/atlas/hieroglyphs.png");
    private static final int hieroglyphWidth = 8;
    private static final int hieroglyphHeight = 8;
    // Number of hieroglyphs in the atlas, excluding the slot at 0,0
    private static final int numHieroglyphs = 43;
    private static final int atlasTextureWidth = 256;
    private static final int atlasTextureHeight = 256;
    private final long hieroglyphSeed;

    public TabletDisplayWidget(
        int x, 
        int y, 
        int width, 
        int height,
        Component clueText,
        boolean uncovered,
        long hieroglyphSeed
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.clueText = clueText;
        this.hieroglyphSeed = hieroglyphSeed;
        this.uncovered = uncovered;
    }

    private void renderUncovered(GuiGraphicsExtractor graphics) {
        graphics.fill(
            x,
            y,
            x + width,
            y + height,
            0xFFC6AE71
        );


        Component uppercaseClueText = Component.literal(
            clueText.getString().toUpperCase(Locale.ROOT)
        );
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(uppercaseClueText);
        int textHeight = font.lineHeight;

        graphics.text(
            font, 
            uppercaseClueText, 
            x + (width / 2) - (textWidth / 2), 
            y + (height / 2) - (textHeight / 2),
            0xFF373737, // Must specify ARGB!
            false // No drop shadow
        );
    }

    private void renderCovered(GuiGraphicsExtractor graphics) {
        int numTiles = this.width / (hieroglyphWidth + 1);
        int paddingX = (this.width % (numTiles * (hieroglyphWidth + 1))) / 2;
        int numRows = this.height / (hieroglyphHeight + 1);
        int paddingY = (this.height % (numRows * (hieroglyphHeight + 1))) / 2;

        RandomSource random = RandomSource.create(hieroglyphSeed);
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numTiles; col++) {
                // Get a random hieroglyph index from the seed
                int hieroglyphIndex = random.nextInt(numHieroglyphs) + 1;
                // Calculate the position of the hieroglyph in the atlas
                int atlasX = (hieroglyphIndex % (atlasTextureWidth / hieroglyphWidth)) * hieroglyphWidth;
                int atlasY = (hieroglyphIndex / (atlasTextureWidth / hieroglyphWidth)) * hieroglyphHeight;
                float relativeAtlasX = (float) atlasX / atlasTextureWidth;
                float relativeAtlasY = (float) atlasY / atlasTextureHeight;
                float relativeAtlasX2 = (float) (atlasX + hieroglyphWidth) / atlasTextureWidth;
                float relativeAtlasY2 = (float) (atlasY + hieroglyphHeight) / atlasTextureHeight;

                // Calculate the position of the hieroglyph in the widget
                int hieroglyphX = paddingX + x + (col * (hieroglyphWidth + 1)) + (this.width % hieroglyphWidth) / 2;
                int hieroglyphY = paddingY + y + (row * (hieroglyphHeight + 1)) + (this.height % hieroglyphHeight) / 2;
                graphics.blit(
                    HIEROGLYPH_ATLAS, 
                    hieroglyphX, 
                    hieroglyphY, 
                    hieroglyphX + hieroglyphWidth, 
                    hieroglyphY + hieroglyphHeight, 
                    relativeAtlasX,  
                    relativeAtlasX2,
                    relativeAtlasY, 
                    relativeAtlasY2
                );
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (uncovered) {
            renderUncovered(graphics);
        } else {
            renderCovered(graphics);
        }
    }

    public void setUncovered(boolean uncovered) {
        this.uncovered = uncovered;
    }
    
}
