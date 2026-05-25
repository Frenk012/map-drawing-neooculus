package wawa.mapwright.neoforge;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.neoforge.item.MapSnapshotItem;

public class MapwrightItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MapwrightClient.MOD_ID);

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MapwrightClient.MOD_ID);

    public static final DeferredHolder<Item, MapSnapshotItem> MAP_SNAPSHOT =
            ITEMS.register("map_snapshot", () -> new MapSnapshotItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SNAPSHOT_ID =
            DATA_COMPONENT_TYPES.register("snapshot_id", () ->
                    DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build()
            );
}
