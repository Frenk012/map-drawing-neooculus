package wawa.mapwright.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.config.MapwrightClientConfig;
import wawa.mapwright.map.widgets.SnapshotButtonWidget;
import wawa.mapwright.neoforge.network.RequestSnapshotItemPacket;

@Mod(value = MapwrightClient.MOD_ID)
public final class MapwrightNeoforgeClient {

    public MapwrightNeoforgeClient(final ModContainer container, final IEventBus modBus) {
        MapwrightItems.ITEMS.register(modBus);
        MapwrightItems.DATA_COMPONENT_TYPES.register(modBus);
        modBus.addListener(this::registerPayloads);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MapwrightClient.init();
            modBus.register(ClientEventsStartup.class);
            NeoForge.EVENT_BUS.register(ClientEventsRuntime.class);
            container.registerConfig(ModConfig.Type.CLIENT, MapwrightClientConfig.CONFIG_SPEC);

            SnapshotButtonWidget.SNAPSHOT_HANDLER = uuid ->
                    PacketDistributor.sendToServer(new RequestSnapshotItemPacket(uuid));
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MapwrightClient.MOD_ID);
        registrar.playToServer(
                RequestSnapshotItemPacket.TYPE,
                RequestSnapshotItemPacket.STREAM_CODEC,
                RequestSnapshotItemPacket::handle
        );
    }
}
