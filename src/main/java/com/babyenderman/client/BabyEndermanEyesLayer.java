package com.babyenderman.client;

import com.babyenderman.BabyEndermanMod;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Glowing green eyes for the baby Enderman. */
public class BabyEndermanEyesLayer extends EyesLayer<EndermanRenderState, EndermanModel<EndermanRenderState>> {
    private static final RenderType GREEN_EYES = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(BabyEndermanMod.MOD_ID, "textures/entity/baby_enderman_eyes.png"));

    public BabyEndermanEyesLayer(RenderLayerParent<EndermanRenderState, EndermanModel<EndermanRenderState>> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GREEN_EYES;
    }
}
