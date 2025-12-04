package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandScreenHandler.PotionSlot.class)
public abstract class BrewingStandScreenHandlerPotionSlotMixin {
	@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
	private static void milkLib$matches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.isOf(MilkLib.SPLASH_MILK_BOTTLE)  || stack.isOf(MilkLib.LINGERING_MILK_BOTTLE) || stack.isOf(MilkLib.MILK_BOTTLE)) {
			cir.setReturnValue(true);
		}
	}
}
