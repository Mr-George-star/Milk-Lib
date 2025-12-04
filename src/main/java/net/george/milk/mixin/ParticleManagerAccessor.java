package net.george.milk.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public interface ParticleManagerAccessor {
    @Invoker
    <T extends ParticleEffect> void callRegisterFactory(
            ParticleType<T> type, ParticleManager.SpriteAwareFactory<T> factory
    );

    @Invoker
    <T extends ParticleEffect> void callRegisterBlockLeakFactory(
            ParticleType<T> type, ParticleFactory.BlockLeakParticleFactory<T> factory
    );
}
