package wawa.mapwright.data;

import wawa.mapwright.MapwrightClient;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

public class SnapshotIO {

    public static void createSnapshot(final Consumer<String> onSaved) {
        final PageManager pm = MapwrightClient.PAGE_MANAGER;
        if (pm.pageIO == null) return;

        final String uuid = UUID.randomUUID().toString();
        final Path snapshotDir = getSnapshotDir(pm, uuid);
        pm.saveSnapshot(snapshotDir, () -> onSaved.accept(uuid));
    }

    public static Path getSnapshotDir(final String uuid) {
        return getSnapshotDir(MapwrightClient.PAGE_MANAGER, uuid);
    }

    private static Path getSnapshotDir(final PageManager pm, final String uuid) {
        if (pm.pageIO == null) return null;
        return pm.pageIO.getPagePath().resolve("snapshots").resolve(uuid);
    }
}
