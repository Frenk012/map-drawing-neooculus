package wawa.mapwright.neoforge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.map.background.MapBackground;
import wawa.mapwright.neoforge.data.LangGen;
import wawa.mapwright.neoforge.input.NeoKeyMappings;

import java.io.IOException;

@EventBusSubscriber(modid = MapwrightClient.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEventsStartup {
    @SubscribeEvent
    public static void registerBindings(final RegisterKeyMappingsEvent event) {
        event.register(NeoKeyMappings.OPEN_MAP.get());
        event.register(NeoKeyMappings.UNDO.get());
        event.register(NeoKeyMappings.REDO.get());
        event.register(NeoKeyMappings.HAND.get());
        event.register(NeoKeyMappings.PEN.get());
        event.register(NeoKeyMappings.BRUSH.get());
        event.register(NeoKeyMappings.ERASER.get());
        event.register(NeoKeyMappings.SWAP.get());
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent event) throws IOException {
        event.registerShader(
            new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(MapwrightClient.MOD_ID, "background"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
            shader -> MapBackground.BACKGROUND_SHADER = shader
        );
    }

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        MapwrightClient.LOGGER.info("Generating data!!! :3");

        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new LangGen(output));
    }
}
