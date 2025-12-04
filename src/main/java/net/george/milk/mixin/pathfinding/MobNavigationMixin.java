package net.george.milk.mixin.pathfinding;

import net.george.milk.MilkLib;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.MobNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MobNavigation.class)
public class MobNavigationMixin {
	@ModifyVariable(
			method = "getPathfindingY",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
			)
	)
	private BlockState treatMilkAsWater(BlockState state) {
		if (MilkLib.isMilk(state)) {
			return Blocks.WATER.getDefaultState();
		}
		return state;
	}
}
