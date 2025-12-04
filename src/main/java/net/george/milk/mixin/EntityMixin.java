package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@ModifyVariable(
			method = "updateMovementInFluid",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/entity/Entity;getWorld()Lnet/minecraft/world/World;",
							ordinal = 0
					)
			),
			at = @At(value = "STORE"
			)
	)
	public FluidState milkLib$clearEffectsInMilk(FluidState state) {
		if ((Object) this instanceof LivingEntity entity) {
			if (MilkLib.isMilk(state)) {
				if (!entity.getStatusEffects().isEmpty()) {
					entity.clearStatusEffects();
				}
			}
		}
		return state;
	}
}
