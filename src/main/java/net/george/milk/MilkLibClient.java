package net.george.milk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;

public class MilkLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupFluidRendering(MilkLib.STILL_MILK, MilkLib.FLOWING_MILK, MilkLib.id("milk"));
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), MilkLib.STILL_MILK, MilkLib.FLOWING_MILK);
    }

    public static void setupFluidRendering(final Fluid still, final Fluid flowing, final Identifier textureBase) {
        final Identifier stillTexture = Identifier.of(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_still");
        final Identifier flowingTexture = Identifier.of(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_flow");

        FluidRenderHandler handler = new SimpleFluidRenderHandler(stillTexture, flowingTexture);
        FluidRenderHandlerRegistry.INSTANCE.register(still, handler);
        FluidRenderHandlerRegistry.INSTANCE.register(flowing, handler);
    }
}
