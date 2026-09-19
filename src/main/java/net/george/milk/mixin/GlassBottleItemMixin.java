package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public abstract class GlassBottleItemMixin extends Item {
	private GlassBottleItemMixin(Properties settings) {
		super(settings);
	}

	@Shadow
	protected abstract ItemStack turnBottleIntoItem(ItemStack itemStack, Player player, ItemStack itemStackToTurnInto);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
			ordinal = 1, shift = At.Shift.AFTER), method = "use", cancellable = true)
	public void milkLib$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (MilkLib.MILK_BOTTLE != null && MilkLib.STILL_MILK != null) {
			BlockHitResult hitResult = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
			BlockPos blockPos = hitResult.getBlockPos();
			FluidState state = level.getFluidState(blockPos);
			if (MilkLib.isMilk(state)) {
				cir.setReturnValue(InteractionResult.SUCCESS.heldItemTransformedTo(this.turnBottleIntoItem(player.getItemInHand(hand), player, new ItemStack(MilkLib.MILK_BOTTLE))));
			}
		}
	}
}
