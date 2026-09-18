package net.george.milk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.george.milk.api.DrippableFluidManager;
import net.george.milk.potion.MilkArrowEntityRenderer;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;

public class MilkLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DrippableFluidManager.registerClient(MilkLib.STILL_MILK_PARTICLES, MilkLib.STILL_MILK);
        DrippableFluidManager.registerClient(MilkLib.FLOWING_MILK_PARTICLES, MilkLib.FLOWING_MILK);

        setupFluidRendering(MilkLib.STILL_MILK, MilkLib.FLOWING_MILK, MilkLib.id("milk"));
        BlockRenderLayerMap.putFluids(BlockRenderLayer.TRANSLUCENT, MilkLib.STILL_MILK, MilkLib.FLOWING_MILK);

        EntityRendererFactories.register(MilkLib.MILK_ARROW_ENTITY_TYPE, MilkArrowEntityRenderer::new);
    }

    public static void setupFluidRendering(final Fluid still, final Fluid flowing, final Identifier textureBase) {
        final Identifier stillTexture = Identifier.of(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_still");
        final Identifier flowingTexture = Identifier.of(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_flow");

        FluidRenderHandler handler = new SimpleFluidRenderHandler(stillTexture, flowingTexture);
        FluidRenderHandlerRegistry.INSTANCE.register(still, handler);
        FluidRenderHandlerRegistry.INSTANCE.register(flowing, handler);
    }
}
