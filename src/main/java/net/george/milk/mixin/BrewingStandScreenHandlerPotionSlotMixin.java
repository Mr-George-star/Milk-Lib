package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandMenu.PotionSlot.class)
public abstract class BrewingStandScreenHandlerPotionSlotMixin {
//	@Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
//	private static void milkLib$matches(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
//		if (itemStack.is(MilkLib.SPLASH_MILK_BOTTLE)  || itemStack.is(MilkLib.LINGERING_MILK_BOTTLE) || itemStack.is(MilkLib.MILK_BOTTLE)) {
//			cir.setReturnValue(true);
//		}
//	}
}
