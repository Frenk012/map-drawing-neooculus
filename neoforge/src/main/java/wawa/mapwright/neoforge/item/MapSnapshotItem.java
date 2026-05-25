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
import wawa.mapwright.data.SnapshotIO;
import wawa.mapwright.map.MapSnapshotScreen;
import wawa.mapwright.neoforge.MapwrightItems;

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
        if (snapshotId == null) return;
        final Path snapshotDir = SnapshotIO.getSnapshotDir(snapshotId);
        if (snapshotDir == null) return;
        Minecraft.getInstance().setScreen(new MapSnapshotScreen(snapshotId, snapshotDir));
    }
}
