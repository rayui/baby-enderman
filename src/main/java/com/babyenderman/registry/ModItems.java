package com.babyenderman.registry;

import com.babyenderman.BabyEndermanMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers our items, including the spawn egg. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BabyEndermanMod.MOD_ID);

    // In 1.21.11 the spawn egg's entity is a data component (set via Properties.spawnEgg)
    // and its colours come from the item texture (assets/.../textures/item/baby_enderman_spawn_egg.png).
    public static final DeferredItem<SpawnEggItem> BABY_ENDERMAN_SPAWN_EGG = ITEMS.registerItem(
            "baby_enderman_spawn_egg",
            SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(ModEntities.BABY_ENDERMAN.get()));

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
