package com.babyenderman.registry;

import com.babyenderman.BabyEndermanMod;
import com.babyenderman.entity.BabyEnderman;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers our entity types. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BabyEndermanMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BabyEnderman>> BABY_ENDERMAN =
            ENTITY_TYPES.register("baby_enderman", name -> {
                ResourceKey<EntityType<?>> key = ResourceKey.create(
                        Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BabyEndermanMod.MOD_ID, "baby_enderman"));
                // Half-size baby: 6x6x8 px outer dimensions => 0.375 x 0.5 blocks.
                return EntityType.Builder.of(BabyEnderman::new, MobCategory.CREATURE)
                        .sized(0.375F, 0.5F)
                        .eyeHeight(0.35F)
                        .clientTrackingRange(10)
                        .build(key);
            });

    private ModEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
