package net.george.milk.mixin.pathfinding;

import net.george.milk.MilkLib;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.BirdPathNodeMaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BirdPathNodeMaker.class)
public class BirdPathNodeMakerMixin {
	@ModifyVariable(
			method = "getStart()Lnet/minecraft/entity/ai/pathing/PathNode;",
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
