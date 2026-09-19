package net.george.milk.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.george.milk.MilkLib;
import net.george.milk.MilkLibTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "collidedWithFluid", at = @At("HEAD"))
	private void milkLib$clearEffectsInMilks(FluidState fluidState, BlockPos blockPos, Vec3 from, Vec3 _to, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof LivingEntity entity) {
			if (MilkLib.isMilk(fluidState)) {
				if (!entity.getActiveEffects().isEmpty()) {
					entity.removeAllEffects();
				}
			}
		}
	}

	@WrapWithCondition(
			method = "updateFluidInteraction",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;doWaterSplashEffect()V"
			)
	)
	private boolean milkLib$preventMilkSplash(Entity instance) {
		return !instance.level().getFluidState(instance.blockPosition()).is(MilkLibTags.MILK);
	}
}
