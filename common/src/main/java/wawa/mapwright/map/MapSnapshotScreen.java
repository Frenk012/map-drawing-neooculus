package wawa.mapwright.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.*;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.data.PageIO;
import wawa.mapwright.data.PageManager;
import wawa.mapwright.map.background.MapBackground;

import java.lang.Math;
import java.nio.file.Path;

public class MapSnapshotScreen extends Screen {

    private static final int OUTER_PADDING  = 30;
    private static final int TOP_MARGIN     = 40;
    private static final int LEFT_MARGIN    = 96;
    private static final int RIGHT_MARGIN   = 96;
    private static final int BOTTOM_MARGIN  = 40;
    private static final int TOP_SCISSOR    = 20;
    private static final int LEFT_SCISSOR   = 50;
    private static final int RIGHT_SCISSOR  = 20;
    private static final int BOTTOM_SCISSOR = 20;

    private final PageManager snapshotManager;
    private MapBackground background;

    private int widgetX, widgetY, widgetW, widgetH;

    private final Vector2d panning = new Vector2d();
    private final Vector2d backgroundPanning = new Vector2d();
    private int zoomNum = 0;
    private float zoom = 1f;

    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    public MapSnapshotScreen(final String snapshotId, final Path snapshotDir) {
        super(Component.translatable("mapwright.snapshot.view"));
        this.snapshotManager = new PageManager();
        this.snapshotManager.pageTexturePrefix = "snap_" + snapshotId.replace("-", "");
        this.snapshotManager.pageIO = new PageIO(snapshotDir);
    }

    @Override
    protected void init() {
        super.init();
        final int outerW = this.width  - OUTER_PADDING * 2;
        final int outerH = this.height - OUTER_PADDING * 2;
        this.background = new MapBackground(outerW, outerH, TOP_MARGIN, LEFT_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN);
        this.widgetW = this.background.getTrueWidth()  + LEFT_MARGIN + RIGHT_MARGIN;
        this.widgetH = this.background.getTrueHeight() + TOP_MARGIN  + BOTTOM_MARGIN;
        this.widgetX = (this.width  - this.widgetW) / 2;
        this.widgetY = (this.height - this.widgetH) / 2;
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.background.render(guiGraphics, this.widgetX, this.widgetY,
                this.widgetW, this.widgetH, this.backgroundPanning, -1);

        final double hw = this.widgetW / 2d;
        final double hh = this.widgetH / 2d;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.widgetX + hw, this.widgetY + hh, 0);
        guiGraphics.pose().scale(this.zoom, this.zoom, 1f);

        final double xOff = -this.panning.x;
        final double yOff = -this.panning.y;

        guiGraphics.enableScissor(
                this.widgetX + LEFT_SCISSOR,
                this.widgetY + TOP_SCISSOR,
                this.widgetX + this.widgetW - RIGHT_SCISSOR,
                this.widgetY + this.widgetH - BOTTOM_SCISSOR
        );

        final int tlX = Mth.floor((this.panning.x - hw / this.zoom) / MapwrightClient.CHUNK_SIZE);
        final int tlY = Mth.floor((this.panning.y - hh / this.zoom) / MapwrightClient.CHUNK_SIZE);
        final int brX = Mth.ceil( (this.panning.x + hw / this.zoom) / MapwrightClient.CHUNK_SIZE);
        final int brY = Mth.ceil( (this.panning.y + hh / this.zoom) / MapwrightClient.CHUNK_SIZE);

        for (int x = tlX; x <= brX; x++) {
            for (int y = tlY; y <= brY; y++) {
                this.snapshotManager.getOrCreatePage(x, y).render(guiGraphics, xOff, yOff);
            }
        }

        guiGraphics.disableScissor();
        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double deltaX, final double deltaY) {
        this.zoomNum = Mth.clamp(this.zoomNum + (int) deltaY, -2, 3);
        this.zoom = (float) Math.pow(2, this.zoomNum);
        return true;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0 || button == 2) {
            this.dragging = true;
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.dragging) {
            final double dx = (mouseX - this.lastMouseX) / this.zoom;
            final double dy = (mouseY - this.lastMouseY) / this.zoom;
            this.panning.sub(dx, dy);
            this.backgroundPanning.sub(deltaX, deltaY);
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.snapshotManager.disposeReadOnly();
    }

    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
