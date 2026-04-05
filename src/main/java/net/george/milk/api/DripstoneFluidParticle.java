package net.george.milk.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public class DripstoneFluidParticle extends WaterSplashParticle {
    public DripstoneFluidParticle(ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        if (this.maxAge-- <= 0) {
            this.markDead();
        } else {
            this.velocityY -= this.gravityStrength;
            this.move(this.velocityX, this.velocityY, this.velocityZ);
            this.velocityX *= 0.98F;
            this.velocityY *= 0.98F;
            this.velocityZ *= 0.98F;
            if (this.onGround) {
                if (Math.random() < 0.5) {
                    this.markDead();
                }
                this.velocityX *= 0.7F;
                this.velocityZ *= 0.7F;
            }
        }
    }
}
