package net.george.milk.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class ParticleFactories {
    public record DrippingDripstoneFluidFactory(DripstoneInteractingFluid fluid, SpriteProvider spriteProvider)
            implements ParticleFactory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld clientWorld,
                                                double x, double y, double z,
                                                double velocityX, double velocityY, double velocityZ,
                                                Random random) {
            BlockLeakParticle particle = new BlockLeakParticle.Dripping(
                    clientWorld, x, y, z, (Fluid) this.fluid,
                    DrippableFluidManager.getInstance().getSet(this.fluid).fall(),
                    this.spriteProvider.getSprite(random)
            );
            int color = this.fluid.getParticleColor(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
            float r = (color >> 16 & 255) / 255f;
            float g = (color >> 8 & 255) / 255f;
            float b = (color & 255) / 255f;
            particle.setColor(r, g, b);
            return particle;
        }
    }

    public record FallingDripstoneFluidFactory(DripstoneInteractingFluid fluid, SpriteProvider spriteProvider)
            implements ParticleFactory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld clientWorld,
                                                double x, double y, double z,
                                                double velocityX, double velocityY, double velocityZ,
                                                Random random) {
            BlockLeakParticle particle = new BlockLeakParticle.DripstoneLavaDrip(
                    clientWorld, x, y, z, (Fluid) this.fluid,
                    DrippableFluidManager.getInstance().getSet(this.fluid).splash(),
                    this.spriteProvider.getSprite(random)
            );
            int color = this.fluid.getParticleColor(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
            float r = (color >> 16 & 255) / 255f;
            float g = (color >> 8 & 255) / 255f;
            float b = (color & 255) / 255f;
            particle.setColor(r, g, b);
            return particle;
        }
    }

    public static class DripstoneFluidSplashFactory extends WaterSplashParticle.SplashFactory {
        private final DripstoneInteractingFluid fluid;

        public DripstoneFluidSplashFactory(SpriteProvider spriteProvider, DripstoneInteractingFluid fluid) {
            super(spriteProvider);
            this.fluid = fluid;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z, double velocityX,
                                       double velocityY, double velocityZ,
                                       Random random) {
            WaterSplashParticle particle = new DripstoneFluidParticle(world,
                    x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider.getSprite(random));
            int color = this.fluid.getParticleColor(world, x, y, z, velocityX, velocityY, velocityZ);
            float r = (color >> 16 & 255) / 255f;
            float g = (color >> 8 & 255) / 255f;
            float b = (color & 255) / 255f;
            particle.setColor(r, g, b);
            return particle;
        }
    }
}
