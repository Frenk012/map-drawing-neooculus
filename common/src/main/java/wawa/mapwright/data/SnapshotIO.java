package wawa.mapwright.data;

import wawa.mapwright.MapwrightClient;

import java.nio.file.Path;
import java.util.UUID;

public class SnapshotIO {

    public static String createSnapshot() {
        final PageManager pm = MapwrightClient.PAGE_MANAGER;
        if (pm.pageIO == null) return null;

        final String uuid = UUID.randomUUID().toString();
        final Path snapshotDir = getSnapshotDir(pm, uuid);
        pm.saveSnapshot(snapshotDir);
        return uuid;
    }

    public static Path getSnapshotDir(final String uuid) {
        return getSnapshotDir(MapwrightClient.PAGE_MANAGER, uuid);
    }

    private static Path getSnapshotDir(final PageManager pm, final String uuid) {
        if (pm.pageIO == null) return null;
        return pm.pageIO.getPagePath().resolve("snapshots").resolve(uuid);
    }
}
