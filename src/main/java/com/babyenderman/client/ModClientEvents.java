package com.babyenderman.client;

import com.babyenderman.BabyEndermanMod;
import com.babyenderman.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only mod-bus events: registers our entity renderer. */
@EventBusSubscriber(modid = BabyEndermanMod.MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BABY_ENDERMAN.get(), BabyEndermanRenderer::new);
    }
}
