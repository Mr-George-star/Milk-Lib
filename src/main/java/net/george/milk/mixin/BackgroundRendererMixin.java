package net.george.milk.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.MilkLib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
	@Inject(method = "getFogColor", at = @At(value = "HEAD"), cancellable = true)
	private static void milkLib$getFogColor(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
		FluidState state = world.getFluidState(camera.getBlockPos());
		if (MilkLib.isMilk(state)) {
			cir.setReturnValue(new Vector4f(1, 1, 1, 1.0F));
        }
	}

	@Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
	private static void milkLib$applyFog(Camera camera, FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
		ClientWorld world = MinecraftClient.getInstance().world;
		if (world == null) {
			cir.cancel();
		} else {
			FluidState state = world.getFluidState(camera.getBlockPos());
			if (MilkLib.isMilk(state)) {
				cir.setReturnValue(new Fog(-8, 5, FogShape.CYLINDER, color.x, color.y, color.z, color.w));
			}
		}
	}
}
