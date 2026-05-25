package wawa.mapwright.neoforge.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.neoforge.MapwrightItems;

public record RequestSnapshotItemPacket(String snapshotId) implements CustomPacketPayload {

    public static final Type<RequestSnapshotItemPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MapwrightClient.MOD_ID, "snapshot_item_request"));

    public static final StreamCodec<ByteBuf, RequestSnapshotItemPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    RequestSnapshotItemPacket::snapshotId,
                    RequestSnapshotItemPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof final ServerPlayer player)) return;
            final ItemStack stack = new ItemStack(MapwrightItems.MAP_SNAPSHOT.get());
            stack.set(MapwrightItems.SNAPSHOT_ID.get(), this.snapshotId);
            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }
        });
    }
}
