package net.george.milk.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.george.milk.MilkLib;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@WrapOperation(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;", ordinal = 0))
	public FluidState milkLib$clearEffectsInMilk(World instance, BlockPos pos, Operation<FluidState> original) {
		FluidState originalState = original.call(instance, pos);
		if ((Object) this instanceof LivingEntity entity) {
			if (MilkLib.isMilk(originalState)) {
				if (!entity.getStatusEffects().isEmpty()) {
					entity.clearStatusEffects();
				}
			}
		}
		return originalState;
	}
}
