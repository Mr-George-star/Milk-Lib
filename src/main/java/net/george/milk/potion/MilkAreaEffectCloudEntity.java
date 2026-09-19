package net.george.milk.potion;

import net.george.milk.MilkLib;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Optional;

public class MilkAreaEffectCloudEntity extends AreaEffectCloud {
	public MilkAreaEffectCloudEntity(Level world, double x, double y, double z) {
		super(world, x, y, z);
		this.setPotionContents(new PotionContents(Optional.of(Potions.WATER), Optional.of(0xFFFFFF), List.of(), Optional.empty()));
	}

	@Override
	public void tick() {
		boolean waiting = this.isWaiting();
		float radius = this.getRadius();
		if (this.level().isClientSide()) {
			if (waiting && this.random.nextBoolean()) {
				return;
			}

			ParticleOptions particleEffect = this.getParticle();
			int area;
			float actualRadius;
			if (waiting) {
				area = 2;
				actualRadius = 0.2F;
			} else {
				area = Mth.ceil((float) Math.PI * radius * radius);
				actualRadius = radius;
			}

			for (int k = 0; k < area; ++k) {
				float l = this.random.nextFloat() * (float) (Math.PI * 2);
				float m = Mth.sqrt(this.random.nextFloat()) * actualRadius;
				double d = this.getX() + (double)(Mth.cos(l) * m);
				double e = this.getY();
				double n = this.getZ() + (double)(Mth.sin(l) * m);
				double s;
				double t;
				double u;
				if (particleEffect.getType() != ParticleTypes.ENTITY_EFFECT) {
					if (waiting) {
						s = 0.0;
						t = 0.0;
						u = 0.0;
					} else {
						s = (0.5 - this.random.nextDouble()) * 0.15;
						t = 0.01F;
						u = (0.5 - this.random.nextDouble()) * 0.15;
					}
				} else {
					int o = 0xFFFFFF;
					s = ((float)(o >> 16 & 0xFF) / 255.0F);
					t = ((float)(o >> 8 & 0xFF) / 255.0F);
					u = ((float)(o & 0xFF) / 255.0F);
				}

				this.level().addAlwaysVisibleParticle(particleEffect, d, e, n, s, t, u);
			}
		} else {
			if (this.tickCount >= getWaitTime() + getDuration()) {
				this.discard();
				return;
			}

			boolean bl2 = this.tickCount < getWaitTime();
			if (waiting != bl2) {
				this.setWaiting(bl2);
			}

			if (bl2) {
				return;
			}

			if (getRadiusPerTick() != 0.0F) {
				radius += getRadiusPerTick();
				if (radius < 0.5F) {
					this.discard();
					return;
				}

				this.setRadius(radius);
			}

			if (this.tickCount % 5 == 0) {
				this.level().getEntities(this, getBoundingBox().inflate(2)).forEach(entity -> {
					if (entity instanceof LivingEntity livingEntity) {
						livingEntity.addEffect(MilkLib.createRandomPurgeEffect());
					}
				});
			}
		}
	}
}
