package wawa.mapwright;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4dc;

import java.io.IOException;
import java.io.InputStream;

public class Rendering {

	public static class RenderTypes {
		public static final ResourceLocation PALETTE_SWAP = MapwrightClient.id("palette_swap");
		public static final ResourceLocation UV_REMAP = MapwrightClient.id("uv_remap");
		public static final ResourceLocation BACKGROUND = MapwrightClient.id("background");
	}

	public static class Textures {
		public static final ResourceLocation PALETTE = MapwrightClient.id("textures/gui/palette.png");
		public static final ResourceLocation HEAD_ICON = MapwrightClient.id("textures/gui/head_icon.png");
		public static final ResourceLocation BACKGROUND = MapwrightClient.id("background");
		public static final ResourceLocation BACKGROUND_FULL = MapwrightClient.id("textures/gui/sprites/background.png");
	}

	public static class Shaders {
		public static final ResourceLocation PALETTE_SWAP = MapwrightClient.id("palette_swap");
		public static final ResourceLocation UV_REMAP = MapwrightClient.id("uv_remap");
		public static final ResourceLocation BACKGROUND = MapwrightClient.id("background");
	}

	public static void renderHead(final GuiGraphics guiGraphics, final Vector2dc playerPosition, final Vector2dc mouseScreen, final double xOff, final double yOff, final float scale, final Vector4dc worldBounds) {
		final Vector2d pos = new Vector2d(playerPosition).add(xOff, yOff).mul(scale); // world position to screen position
		Helper.clampWithin(pos, worldBounds);
		final float alpha = Helper.getMouseProximityFade(mouseScreen, pos, 35);
		Rendering.renderPlayerIcon(guiGraphics, pos.x - 8, pos.y - 8, Minecraft.getInstance().player, alpha);
	}

	public static void simpleTypeBlit(final GuiGraphics guiGraphics, final ResourceLocation texture, final double x, final double y, final int blitOffset, final float uOffset, final float vOffset, final int uWidth, final int vHeight, final int textureWidth, final int textureHeight, final float alpha) {
		final float minU = uOffset / textureWidth;
		final float maxU = (uOffset + uWidth) / (float) textureWidth;
		final float minV = vOffset / textureHeight;
		final float maxV = (vOffset + vHeight) / (float) textureHeight;
		simpleTypeBlit(guiGraphics, texture, x, x + uWidth, y, y + vHeight, blitOffset, minU, maxU, minV, maxV, alpha);
	}

	public static void simpleTypeBlit(final GuiGraphics guiGraphics, final ResourceLocation texture, final double x1, final double x2, final double y1, final double y2, final int blitOffset, final float minU, final float maxU, final float minV, final float maxV, final float alpha) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		buf.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV).setColor(1, 1, 1, alpha);
		buf.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV).setColor(1, 1, 1, alpha);
		buf.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV).setColor(1, 1, 1, alpha);
		buf.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV).setColor(1, 1, 1, alpha);
		BufferUploader.drawWithShader(buf.buildOrThrow());
		RenderSystem.disableBlend();
	}

	public static void simpleTypeBlitUV1(final GuiGraphics guiGraphics, final ResourceLocation texture, final int x, final int y, final int width, final int height, final int textureWidth, final int textureHeight, final int blitOffset, final float u, final float v) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		buf.addVertex(matrix4f, (float)x, (float)y, (float)blitOffset).setUv(u / textureWidth, v / textureHeight).setColor(1, 1, 1, 1);
		buf.addVertex(matrix4f, (float)x, (float)(y + height), (float)blitOffset).setUv(u / textureWidth, (v + height) / textureHeight).setColor(1, 1, 1, 1);
		buf.addVertex(matrix4f, (float)(x + width), (float)(y + height), (float)blitOffset).setUv((u + width) / textureWidth, (v + height) / textureHeight).setColor(1, 1, 1, 1);
		buf.addVertex(matrix4f, (float)(x + width), (float)y, (float)blitOffset).setUv((u + width) / textureWidth, v / textureHeight).setColor(1, 1, 1, 1);
		BufferUploader.drawWithShader(buf.buildOrThrow());
		RenderSystem.disableBlend();
	}

	public static void backgroundBlit(final GuiGraphics guiGraphics, final ResourceLocation texture,
									   final int x, final int y, final int width, final int height,
									   final int textureWidth, final int textureHeight, final int blitOffset,
									   final float u, final float v,
									   final ShaderInstance shader, final float transX, final float transY) {
		if (shader == null) {
			simpleTypeBlitUV1(guiGraphics, texture, x, y, width, height, textureWidth, textureHeight, blitOffset, u, v);
			return;
		}
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShaderTexture(1, Textures.BACKGROUND_FULL);
		RenderSystem.setShader(() -> shader);
		final var uScreenCenter = shader.getUniform("ScreenCenter");
		if (uScreenCenter != null) uScreenCenter.set(0f, 0f);
		final var uTranslation = shader.getUniform("Translation");
		if (uTranslation != null) uTranslation.set(transX, transY);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
		buf.addVertex(matrix4f, (float) x, (float) y, (float) blitOffset)
				.setColor(1f, 1f, 1f, 1f).setUv(u / textureWidth, v / textureHeight).setUv2(x, y);
		buf.addVertex(matrix4f, (float) x, (float) (y + height), (float) blitOffset)
				.setColor(1f, 1f, 1f, 1f).setUv(u / textureWidth, (v + height) / (float) textureHeight).setUv2(x, y + height);
		buf.addVertex(matrix4f, (float) (x + width), (float) (y + height), (float) blitOffset)
				.setColor(1f, 1f, 1f, 1f).setUv((u + width) / (float) textureWidth, (v + height) / (float) textureHeight).setUv2(x + width, y + height);
		buf.addVertex(matrix4f, (float) (x + width), (float) y, (float) blitOffset)
				.setColor(1f, 1f, 1f, 1f).setUv((u + width) / (float) textureWidth, v / (float) textureHeight).setUv2(x + width, y);
		BufferUploader.drawWithShader(buf.buildOrThrow());
		RenderSystem.disableBlend();
	}

	public static void renderPlayerIcon(final GuiGraphics graphics, final double x, final double y, final LocalPlayer player, final float alpha) {
		// cancelled by MapwrightRenderingMixin; no Veil shader available
	}

	public static NativeImage getPaletteTexture() {
		final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		NativeImage image = null;

		try {
			final Resource resource = resourceManager.getResourceOrThrow(Textures.PALETTE);

			try(final InputStream stream = resource.open()) {
				image = NativeImage.read(stream);
			}

		} catch (final IOException ignored) {}

		return image;
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x, final double y, final int blitOffset, final float uOffset, final float vOffset, final int uWidth, final int vHeight, final int textureWidth, final int textureHeight, final float alpha) {
		renderTypeBlit(guiGraphics, renderType, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, alpha);
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x1, final double x2, final double y1, final double y2, final int blitOffset, final int uWidth, final int vHeight, final float uOffset, final float vOffset, final int textureWidth, final int textureHeight, final float alpha) {
		renderTypeBlit(guiGraphics, renderType, x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight, alpha);
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x1, final double x2, final double y1, final double y2, final int blitOffset, final float minU, final float maxU, final float minV, final float maxV, final float alpha) {
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV).setColor(1, 1, 1, alpha);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV).setColor(1, 1, 1, alpha);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV).setColor(1, 1, 1, alpha);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV).setColor(1, 1, 1, alpha);
		renderType.draw(bufferBuilder.buildOrThrow());
	}

	public static void renderTypeBlitUV1(final GuiGraphics guiGraphics, final RenderType renderType,
										 final int x, final int y, final int width, final int height,
										 final int textureWidth, final int textureHeight, final int blitOffset,
										 final float u, final float v) {
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.addVertex(matrix4f, (float)x, (float)y, (float)blitOffset)
				.setUv(u / textureWidth, v / textureHeight)
				.setColor(1, 1, 1, 1);

		bufferBuilder.addVertex(matrix4f, (float)x, (float)y + height, (float)blitOffset)
				.setUv(u / textureWidth, (v + height) / textureHeight)
				.setColor(1, 1, 1, 1);

		bufferBuilder.addVertex(matrix4f, (float)x + width, (float)y + height, (float)blitOffset)
				.setUv((u + width) / textureWidth, (v + height) / textureHeight)
				.setColor(1, 1, 1, 1);

		bufferBuilder.addVertex(matrix4f, (float)x + width, (float)y, (float)blitOffset)
				.setUv((u + width) / textureWidth, v / textureHeight)
				.setColor(1, 1, 1, 1);

		renderType.draw(bufferBuilder.buildOrThrow());
	}

	public static void croppedBlit(final GuiGraphics graphics, final ResourceLocation atlasLocation, final int x1, final int x2, final int y1, final int y2, final int blitOffset, final float minU, final float maxU, final float minV, final float maxV, final float red, final float green, final float blue, final float alpha) {
		RenderSystem.setShaderTexture(0, atlasLocation);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();
		final Matrix4f matrix4f = graphics.pose().last().pose();
		final BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferbuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV).setColor(red, green, blue, alpha);
		bufferbuilder.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV).setColor(red, green, blue, alpha);
		bufferbuilder.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV).setColor(red, green, blue, alpha);
		bufferbuilder.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV).setColor(red, green, blue, alpha);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
		RenderSystem.disableBlend();
	}
}
