package com.babyenderman.client;

import com.babyenderman.BabyEndermanMod;
import com.babyenderman.entity.BabyEnderman;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the baby Enderman. Reuses the vanilla Enderman model but scaled right down (a tiny
 * Enderman = a baby!), with our own dark texture, glowing green eyes, and the carried block.
 */
public class BabyEndermanRenderer extends MobRenderer<BabyEnderman, EndermanRenderState, EndermanModel<EndermanRenderState>> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(BabyEndermanMod.MOD_ID, "textures/entity/baby_enderman.png");

    // A full Enderman model is ~2.9 blocks tall; this shrinks it to a ~0.6-block baby.
    private static final float BABY_SCALE = 0.2F;

    public BabyEndermanRenderer(EntityRendererProvider.Context context) {
        // NOTE: the 3rd arg is the SHADOW radius, not the model scale. Model scaling is done in scale().
        super(context, new EndermanModel<>(context.bakeLayer(ModelLayers.ENDERMAN)), 0.18F);
        this.addLayer(new BabyEndermanEyesLayer(this));
        this.addLayer(new CarriedBlockLayer(this));
    }

    @Override
    protected void scale(EndermanRenderState state, PoseStack poseStack) {
        poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
    }

    @Override
    public EndermanRenderState createRenderState() {
        return new EndermanRenderState();
    }

    @Override
    public void extractRenderState(BabyEnderman entity, EndermanRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver);
        state.isCreepy = false;
        state.carriedBlock = entity.getCarriedBlock();
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState state) {
        return TEXTURE;
    }
}
