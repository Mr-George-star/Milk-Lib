package net.george.milk.potion;

import net.george.milk.MilkLib;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class MilkAreaEffectCloudEntity extends AreaEffectCloudEntity {
	public MilkAreaEffectCloudEntity(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setPotionContents(new PotionContentsComponent(Optional.ofNullable(Potions.WATER), Optional.of(0xFFFFFF), List.of()));
	}

	@Override
	public void tick() {
		boolean waiting = this.isWaiting();
		float radius = this.getRadius();
		if (this.getWorld().isClient) {
			if (waiting && this.random.nextBoolean()) {
				return;
			}

			ParticleEffect particleEffect = this.getParticleType();
			int area;
			float actualRadius;
			if (waiting) {
				area = 2;
				actualRadius = 0.2F;
			} else {
				area = MathHelper.ceil((float) Math.PI * radius * radius);
				actualRadius = radius;
			}

			for (int k = 0; k < area; ++k) {
				float l = this.random.nextFloat() * (float) (Math.PI * 2);
				float m = MathHelper.sqrt(this.random.nextFloat()) * actualRadius;
				double d = this.getX() + (double)(MathHelper.cos(l) * m);
				double e = this.getY();
				double n = this.getZ() + (double)(MathHelper.sin(l) * m);
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

				this.getWorld().addImportantParticle(particleEffect, d, e, n, s, t, u);
			}
		} else {
			if (this.age >= getWaitTime() + getDuration()) {
				this.discard();
				return;
			}

			boolean bl2 = this.age < getWaitTime();
			if (waiting != bl2) {
				this.setWaiting(bl2);
			}

			if (bl2) {
				return;
			}

			if (getRadiusGrowth() != 0.0F) {
				radius += getRadiusGrowth();
				if (radius < 0.5F) {
					this.discard();
					return;
				}

				this.setRadius(radius);
			}

			if (this.age % 5 == 0) {
				getWorld().getOtherEntities(this, getBoundingBox().expand(2)).forEach(entity -> {
					if (entity instanceof LivingEntity livingEntity) {
						MilkLib.tryRemoveRandomEffect(livingEntity);
					}
				});
			}
		}
	}
}
