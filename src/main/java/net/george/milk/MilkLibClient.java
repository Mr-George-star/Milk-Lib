package net.george.milk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.george.milk.api.DrippableFluidManager;
import net.george.milk.potion.MilkArrowEntityRenderer;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public class MilkLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DrippableFluidManager.registerClient(MilkLib.STILL_MILK_PARTICLES, MilkLib.STILL_MILK);
        DrippableFluidManager.registerClient(MilkLib.FLOWING_MILK_PARTICLES, MilkLib.FLOWING_MILK);

        setupFluidRendering(MilkLib.STILL_MILK, MilkLib.FLOWING_MILK, MilkLib.id("milk"));

        EntityRenderers.register(MilkLib.MILK_ARROW_ENTITY_TYPE, MilkArrowEntityRenderer::new);
    }

    public static void setupFluidRendering(final Fluid still, final Fluid flowing, final Identifier textureBase) {
        final Identifier stillTexture = Identifier.fromNamespaceAndPath(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_still");
        final Identifier flowingTexture = Identifier.fromNamespaceAndPath(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_flow");

        FluidRenderingRegistry.register(still, flowing, new FluidModel.Unbaked(
                new Material(stillTexture),
                new Material(flowingTexture),
                null,
                BlockTintSources.constant(0XFFFFFF)
        ));
    }
}
