package wawa.mapwright.map.background;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2dc;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.Rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

// this whole class is awful and hardcoded to hell and back
public class MapBackground {
    public static ShaderInstance BACKGROUND_SHADER = null;
    private static final int[] sizesHorizontal =  new int[]{8, 32, 96};
    private static final int[] sizesVertical =  new int[]{8, 16, 48};
    private static final int minSize = Arrays.stream(sizesHorizontal).min().getAsInt();

    public final int topMargin;
    public final int leftMargin;
    public final int rightMargin;
    public final int bottomMargin;

    private final FullEdge topEdge = new FullEdge(Edge.TOP);
    private final FullEdge leftEdge = new FullEdge(Edge.LEFT);
    private final FullEdge rightEdge = new FullEdge(Edge.RIGHT);
    private final FullEdge bottomEdge = new FullEdge(Edge.BOTTOM);

    public MapBackground(final int width, final int height,
                         final int topMargin, final int leftMargin, final int rightMargin, final int bottomMargin) {
        this.topMargin = topMargin;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.bottomMargin = bottomMargin;

        final Random rand = new Random((long) width * height);
        this.topEdge.buildEdge(width - leftMargin - rightMargin, rand);
        this.leftEdge.buildEdge(height - topMargin - bottomMargin, rand);
        this.rightEdge.buildEdge(height - topMargin - bottomMargin, rand);
        this.bottomEdge.buildEdge(width - leftMargin - rightMargin, rand);
    }

    public int innerWidth(final int width) {
        return width - this.leftMargin - this.rightMargin;
    }

    public int innerHeight(final int height) {
        return height - this.topMargin - this.bottomMargin;
    }

    public int getTrueWidth() {
        return this.topEdge.trueSize;
    }

    public int getTrueHeight() {
        return this.leftEdge.trueSize;
    }

    public void render(final GuiGraphics guiGraphics, final int x, final int y, final int width, final int height, final Vector2dc backgroundTranslation, final int blitOffset) {
        final float transX = (float) backgroundTranslation.x();
        final float transY = (float) backgroundTranslation.y();
        final ResourceLocation cornerTex = MapwrightClient.id("textures/gui/sprites/background/corner.png");
        Rendering.backgroundBlit(guiGraphics, cornerTex,
                x, y,
                this.leftMargin, this.topMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, 0, 0, BACKGROUND_SHADER, transX, transY
        );
        Rendering.backgroundBlit(guiGraphics, cornerTex,
                x + width - this.rightMargin, y,
                this.rightMargin, this.topMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, this.leftMargin, 0, BACKGROUND_SHADER, transX, transY
        );
        Rendering.backgroundBlit(guiGraphics, cornerTex,
                x, y + height - this.bottomMargin,
                this.leftMargin, this.bottomMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, 0, this.topMargin, BACKGROUND_SHADER, transX, transY
        );
        Rendering.backgroundBlit(guiGraphics, cornerTex,
                x + width - this.rightMargin, y + height - this.bottomMargin,
                this.rightMargin, this.bottomMargin,
                this.leftMargin + this.rightMargin, this.topMargin + this.bottomMargin,
                blitOffset, this.leftMargin, this.topMargin, BACKGROUND_SHADER, transX, transY
        );

        final int iw = this.innerWidth(width);
        final int ih = this.innerHeight(height);
        // UV must be 0..1 (not 0..iw) to avoid mipmap level overflow on the 1x1 magenta center.png
        Rendering.backgroundBlit(guiGraphics, MapwrightClient.id("textures/gui/sprites/background/center.png"),
                x + this.leftMargin, y + this.topMargin,
                iw, ih, iw, ih,
                blitOffset, 0, 0, BACKGROUND_SHADER, transX, transY
        );

        this.topEdge.render(guiGraphics, transX, transY,
                x + this.leftMargin, y,
                this.topMargin, blitOffset);
        this.leftEdge.render(guiGraphics, transX, transY,
                x, y + this.topMargin,
                this.leftMargin, blitOffset);
        this.rightEdge.render(guiGraphics, transX, transY,
                x + width - this.rightMargin, y + this.topMargin,
                this.rightMargin, blitOffset);
        this.bottomEdge.render(guiGraphics, transX, transY,
                x + this.rightMargin, y + height - this.bottomMargin,
                this.bottomMargin, blitOffset);
    }

    enum Edge {
        TOP("top", true, sizesHorizontal),
        LEFT("left", false, sizesVertical),
        RIGHT("right", false, sizesVertical),
        BOTTOM("bottom", true, sizesHorizontal);

        final boolean horizontal;
        final int[] textureSizes;
        final ResourceLocation[] textures;
        Edge(final String prefix, final boolean horizontal, final int[] textureSizes) {
            this.horizontal = horizontal;
            this.textureSizes = textureSizes;
            this.textures = Arrays.stream(textureSizes)
                    .mapToObj(i -> MapwrightClient.id("textures/gui/sprites/background/" + prefix + "/" + i + ".png"))
                    .toArray(ResourceLocation[]::new);
        }
    }

    private static class FullEdge extends ArrayList<EdgeTexture> {
        public final Edge edge;
        private int trueSize = 0;
        public FullEdge(final Edge edge) {
            this.edge = edge;
        }

        // this is. not a good algorithm. boowomp
        public void buildEdge(final int targetSize, final Random random) {
            this.clear();
            int currentSize = 0;
            final int[] sizeCount = new int[this.edge.textureSizes.length];
            while (currentSize + minSize <= targetSize) {
                final int candidateIndex = random.nextInt(sizeCount.length);
                if (this.edge.textureSizes[candidateIndex] + currentSize <= targetSize) {
                    currentSize += this.edge.textureSizes[candidateIndex];
                    sizeCount[candidateIndex]++;
                }
            }
            this.trueSize = currentSize;

            for (int i = 0; i < sizeCount.length; i++) {
                for (int j = 0; j < sizeCount[i]; j++) {
                    this.add(new EdgeTexture(i, random.nextInt(EdgeTexture.variations)));
                }
            }
            Collections.shuffle(this, random);
        }

        public void render(final GuiGraphics guiGraphics, final float transX, final float transY, int x, int y, final int size, final int blitOffset) {
            if (this.edge.horizontal) {
                for (final EdgeTexture edge : this) {
                    final int width = this.edge.textureSizes[edge.sizeIndex];
                    Rendering.backgroundBlit(guiGraphics, this.edge.textures[edge.sizeIndex],
                            x, y, width, size,
                            width, size * EdgeTexture.variations,
                            blitOffset, 0, edge.index * size,
                            MapBackground.BACKGROUND_SHADER, transX, transY
                    );
                    x += width;
                }
            } else {
                for (final EdgeTexture edge : this) {
                    final int height = this.edge.textureSizes[edge.sizeIndex];
                    Rendering.backgroundBlit(guiGraphics, this.edge.textures[edge.index],
                            x, y, size, height,
                            size * EdgeTexture.variations, height,
                            blitOffset, edge.index * size, 0,
                            MapBackground.BACKGROUND_SHADER, transX, transY
                    );
                    y += height;
                }
            }
        }
    }

    private record EdgeTexture(int sizeIndex, int index) {
        public static final int variations = 3;
    }
}
