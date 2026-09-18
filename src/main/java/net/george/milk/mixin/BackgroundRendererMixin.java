package net.george.milk.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.MilkLib;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public abstract class BackgroundRendererMixin {
	@Inject(method = "getFogColor", at = @At(value = "HEAD"), cancellable = true)
	private void milkLib$getFogColor(Camera camera, float tickProgress, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
		FluidState state = world.getFluidState(camera.getBlockPos());
		if (MilkLib.isMilk(state)) {
			cir.setReturnValue(new Vector4f(1, 1, 1, 1.0F));
        }
	}

	@Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"), cancellable = true)
	private void milkLib$applyFog(Camera camera, int viewDistance, RenderTickCounter renderTickCounter, float f, ClientWorld clientWorld, CallbackInfoReturnable<Vector4f> cir, @Local FogData fogData) {
		if (clientWorld == null) {
			cir.cancel();
		} else {
			FluidState state = clientWorld.getFluidState(camera.getBlockPos());
			if (MilkLib.isMilk(state)) {
				fogData.renderDistanceStart = -8;
				fogData.renderDistanceEnd = 5;
			}
		}
	}
}
