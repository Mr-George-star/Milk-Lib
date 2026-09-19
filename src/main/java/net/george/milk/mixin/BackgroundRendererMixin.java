package net.george.milk.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.MilkLib;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public abstract class BackgroundRendererMixin {
	@Inject(method = "computeFogColor", at = @At(value = "HEAD"), cancellable = true)
	private void milkLib$getFogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f dest, CallbackInfo ci) {
		FluidState state = level.getFluidState(camera.blockPosition());
		if (MilkLib.isMilk(state)) {
			dest.set(new Vector4f(1, 1, 1, 1.0F));
			ci.cancel();
        }
	}

	@Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
	private void milkLib$setupFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir, @Local(name = "fog") FogData fog) {
		if (level == null) {
			cir.cancel();
		} else {
			FluidState state = level.getFluidState(camera.blockPosition());
			if (MilkLib.isMilk(state)) {
				fog.renderDistanceStart = -8;
				fog.renderDistanceEnd = 5;
			}
		}
	}
}
