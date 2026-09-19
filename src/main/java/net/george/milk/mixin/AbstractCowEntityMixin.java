package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCow.class)
public abstract class AbstractCowEntityMixin extends Animal {
	protected AbstractCowEntityMixin(EntityType<? extends Animal> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(at = @At("HEAD"), method = "mobInteract", cancellable = true)
	public void milkLib$interactMob(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack itemStack = player.getItemInHand(hand);
		if (itemStack.is(Items.GLASS_BOTTLE) && !this.isBaby() && MilkLib.MILK_BOTTLE != null) {
			player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
			ItemStack exchanged = ItemUtils.createFilledResult(itemStack, player, MilkLib.MILK_BOTTLE.getDefaultInstance());
			player.setItemInHand(hand, exchanged);
			cir.setReturnValue(InteractionResult.SUCCESS);
		}
	}
}
