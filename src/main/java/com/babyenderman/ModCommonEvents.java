package com.babyenderman;

import com.babyenderman.entity.BabyEnderman;
import com.babyenderman.registry.ModEntities;
import com.babyenderman.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/** Mod-bus events that run on both client and server. */
@EventBusSubscriber(modid = BabyEndermanMod.MOD_ID)
public final class ModCommonEvents {
    private ModCommonEvents() {}

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BABY_ENDERMAN.get(), BabyEnderman.createAttributes().build());
    }

    @SubscribeEvent
    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.BABY_ENDERMAN_SPAWN_EGG.get());
        }
    }
}
