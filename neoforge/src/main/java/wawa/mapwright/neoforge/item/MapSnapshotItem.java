package wawa.mapwright.neoforge.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.data.SnapshotIO;
import wawa.mapwright.map.MapSnapshotScreen;
import wawa.mapwright.neoforge.MapwrightItems;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Path;

public class MapSnapshotItem extends Item {

    public MapSnapshotItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            openSnapshot(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openSnapshot(final ItemStack stack) {
        final String snapshotId = stack.get(MapwrightItems.SNAPSHOT_ID.get());
        if (snapshotId == null) { MapwrightClient.LOGGER.warn("[Snapshot] open: snapshotId is null"); return; }
        final Path snapshotDir = SnapshotIO.getSnapshotDir(snapshotId);
        if (snapshotDir == null) { MapwrightClient.LOGGER.warn("[Snapshot] open: snapshotDir is null (pageIO null?)"); return; }
        try {
            long fileCount = -1L;
            if (Files.exists(snapshotDir)) {
                try (final java.util.stream.Stream<Path> s = Files.list(snapshotDir)) {
                    fileCount = s.filter(p -> p.toString().endsWith(".png")).count();
                }
            }
            MapwrightClient.LOGGER.info("[Snapshot] open: dir={} pngCount={}", snapshotDir, fileCount);
        } catch (final IOException e) {
            MapwrightClient.LOGGER.warn("[Snapshot] open: could not list dir", e);
        }
        Minecraft.getInstance().setScreen(new MapSnapshotScreen(snapshotId, snapshotDir));
    }
}
