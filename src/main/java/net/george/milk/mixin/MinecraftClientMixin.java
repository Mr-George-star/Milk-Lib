package net.george.milk.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.api.Constants;
import net.george.milk.api.ParticleFactories;
import net.george.milk.api.ParticleTypeSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DiscouragedShift")
@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Final public ParticleManager particleManager;

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.BY, by = 2, target = "Lnet/minecraft/client/particle/ParticleManager;<init>(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/texture/TextureManager;)V"), method = "<init>")
    private void milkLib$handleParticles(RunArgs args, CallbackInfo ci) {
        Constants.TO_REGISTER.forEach(fluid -> {
            ParticleTypeSet particles = Constants.FLUIDS_TO_PARTICLES.get(fluid);
            ParticleManagerAccessor access = (ParticleManagerAccessor) this.particleManager;
            access.callRegisterBlockLeakFactory(particles.hang(), new ParticleFactories.DrippingDripstoneFluidFactory(fluid));
            access.callRegisterBlockLeakFactory(particles.fall(), new ParticleFactories.FallingDripstoneFluidFactory(fluid));
            access.callRegisterFactory(particles.splash(), prov -> new ParticleFactories.DripstoneFluidSplashFactory(prov, fluid));
        });
    }
}
