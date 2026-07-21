package rats.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import rats.Main;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ModItems {
    public static final DataComponentType<List<RatData>> RATS_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Main.id("rats"),
            DataComponentType.<List<RatData>>builder().persistent(RatData.CODEC.listOf()).build()
    );

    public static Item RAT_BUNDLE = register("rat_bundle", RatBundleItem::new, new Item.Properties().stacksTo(1).component(RATS_COMPONENT, List.of(new RatData(8F, 0, Optional.empty(), Optional.empty(),Optional.empty(), List.of(), List.of()))));
    public static Item CREATIVE_RAT_BUNDLE = register("creative_rat_bundle", CreativeRatBundleItem::new, new Item.Properties().stacksTo(1));

    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Main.id(name));
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("spawn_eggs")))
                .register((output) -> {
                    output.accept(RAT_BUNDLE);
                    output.accept(CREATIVE_RAT_BUNDLE);
                });
    }
}
