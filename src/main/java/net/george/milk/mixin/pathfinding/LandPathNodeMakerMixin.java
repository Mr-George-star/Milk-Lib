package net.george.milk.mixin.pathfinding;

import net.george.milk.MilkLib;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WalkNodeEvaluator.class)
public class LandPathNodeMakerMixin {
	@ModifyVariable(
			method = "getStart",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"
			),
			name = "blockState")
	private BlockState treatMilkAsWater(BlockState blockState) {
		if (MilkLib.isMilk(blockState)) {
			return Blocks.WATER.defaultBlockState();
		}
		return blockState;
	}
}
