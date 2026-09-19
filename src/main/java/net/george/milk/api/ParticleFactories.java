package net.george.milk.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ParticleFactories {
    public record DrippingDripstoneFluidFactory(DrippableFluid fluid, SpriteSet spriteProvider)
            implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel clientWorld,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       @NotNull RandomSource random) {
            DripParticle particle = new DripParticle.DripHangParticle(
                    clientWorld, x, y, z, (Fluid) this.fluid,
                    DrippableFluidManager.getInstance().getSet(this.fluid).fall(),
                    this.spriteProvider.get(random)
            );
            int color = this.fluid.getParticleColor(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
            float r = (color >> 16 & 255) / 255f;
            float g = (color >> 8 & 255) / 255f;
            float b = (color & 255) / 255f;
            particle.setColor(r, g, b);
            return particle;
        }
    }

    public record FallingDripstoneFluidFactory(DrippableFluid fluid, SpriteSet spriteProvider)
            implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel clientWorld,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       @NotNull RandomSource random) {
            DripParticle particle = new DripParticle.DripstoneFallAndLandParticle(
                    clientWorld, x, y, z, (Fluid) this.fluid,
                    DrippableFluidManager.getInstance().getSet(this.fluid).splash(),
                    this.spriteProvider.get(random)
            );
            int color = this.fluid.getParticleColor(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
            float r = (color >> 16 & 255) / 255f;
            float g = (color >> 8 & 255) / 255f;
            float b = (color & 255) / 255f;
            particle.setColor(r, g, b);
            return particle;
        }
    }

    public static class DripstoneFluidSplashFactory extends SplashParticle.Provider {
        private final DrippableFluid fluid;

        public DripstoneFluidSplashFactory(SpriteSet spriteProvider, DrippableFluid fluid) {
            super(spriteProvider);
            this.fluid = fluid;
        }

        @NotNull
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world,
                                       double x, double y, double z, double velocityX,
                                       double velocityY, double velocityZ,
                                       @NotNull RandomSource random) {
            SplashParticle particle = new DripstoneFluidParticle(world,
                    x, y, z, velocityX, velocityY, velocityZ, this.sprite.get(random));
            int color = this.fluid.getParticleColor(world, x, y, z, velocityX, velocityY, velocityZ);
            float r = (color >> 16 & 255) / 255f;
            float g = (color >> 8 & 255) / 255f;
            float b = (color & 255) / 255f;
            particle.setColor(r, g, b);
            return particle;
        }
    }
}
