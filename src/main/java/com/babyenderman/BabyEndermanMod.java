package com.babyenderman;

import com.babyenderman.registry.ModEntities;
import com.babyenderman.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Main entry point for the Baby Enderman mod.
 *
 * <p>This adds an adorable, tamable baby Enderman companion that walks around,
 * does cute little jumps, carries blocks, can be tamed with ender pearls, and
 * rides on the player's shoulder handing out presents.</p>
 */
@Mod(BabyEndermanMod.MOD_ID)
public class BabyEndermanMod {
    public static final String MOD_ID = "babyenderman";

    public BabyEndermanMod(IEventBus modEventBus) {
        // Register all of our content with the game.
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
    }
}
