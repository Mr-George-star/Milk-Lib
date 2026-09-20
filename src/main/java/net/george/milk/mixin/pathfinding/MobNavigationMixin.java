package net.george.milk.mixin.pathfinding;

import net.george.milk.MilkLib;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GroundPathNavigation.class)
public class MobNavigationMixin {
	@ModifyVariable(
			method = "getSurfaceY",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"
			),
			name = "state")
	private BlockState treatMilkAsWater(BlockState state) {
		if (MilkLib.isMilk(state)) {
			return Blocks.WATER.defaultBlockState();
		}
		return state;
	}
}
