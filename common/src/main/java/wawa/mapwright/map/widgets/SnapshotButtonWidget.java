package wawa.mapwright.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import wawa.mapwright.MapwrightClient;

import java.util.function.Consumer;

public class SnapshotButtonWidget extends AbstractWidget {

    private static final ResourceLocation ICON =
            MapwrightClient.id("textures/gui/sprites/snapshot_button.png");

    /**
     * Set by the platform module (NeoForge) to send the snapshot request packet.
     * Receives the snapshot UUID string.
     */
    public static Consumer<String> SNAPSHOT_HANDLER = uuid -> {};

    public SnapshotButtonWidget(final int x, final int y) {
        super(x, y, 16, 16, Component.translatable("mapwright.snapshot.create"));
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        guiGraphics.blit(ICON, this.getX(), this.getY(), 0, 0, 16, 16, 16, 16);
        if (this.isHovered) {
            guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + 17, this.getY() + 17, 0x44FFFFFF);
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.translatable("mapwright.snapshot.create"),
                    mouseX, mouseY
            );
        }
    }

    @Override
    public void onClick(final double mouseX, final double mouseY) {
        wawa.mapwright.data.SnapshotIO.createSnapshot(SNAPSHOT_HANDLER);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output) {}
}
