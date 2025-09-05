package rats.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import rats.Main;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ModItems {
    public static final ComponentType<List<RatData>> RATS_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Main.id("rats"),
            ComponentType.<List<RatData>>builder().codec(RatData.CODEC.listOf()).build()
    );

    public static Item RAT_BUNDLE = register("rat_bundle", RatBundleItem::new, new Item.Settings().maxCount(1).component(RATS_COMPONENT, List.of(new RatData(8F, 0, Optional.empty(), Optional.empty(),Optional.empty(), List.of(), List.of()))));
    public static Item CREATIVE_RAT_BUNDLE = register("creative_rat_bundle", CreativeRatBundleItem::new, new Item.Settings().maxCount(1));

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Main.id(name));
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
                .register((itemGroup) -> {
                    itemGroup.add(RAT_BUNDLE);
                    itemGroup.add(CREATIVE_RAT_BUNDLE);
                });
    }
}
