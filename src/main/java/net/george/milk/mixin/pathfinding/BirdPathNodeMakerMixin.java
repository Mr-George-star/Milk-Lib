package net.george.milk.mixin.pathfinding;

import net.george.milk.MilkLib;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FlyNodeEvaluator.class)
public class BirdPathNodeMakerMixin {
	@ModifyVariable(
			method = "getStart",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"
			),
			name = "state")
	private BlockState treatMilkAsWater(BlockState state) {
		if (MilkLib.isMilk(state)) {
			return Blocks.WATER.defaultBlockState();
		}
		return state;
	}
}
