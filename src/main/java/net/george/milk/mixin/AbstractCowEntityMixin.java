package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractCowEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCowEntity.class)
public abstract class AbstractCowEntityMixin extends AnimalEntity {
	protected AbstractCowEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("HEAD"), method = "interactMob", cancellable = true)
	public void milkLib$interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (itemStack.isOf(Items.GLASS_BOTTLE) && !this.isBaby() && MilkLib.MILK_BOTTLE != null) {
			player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
			ItemStack exchanged = ItemUsage.exchangeStack(itemStack, player, MilkLib.MILK_BOTTLE.getDefaultStack());
			player.setStackInHand(hand, exchanged);
			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}
}
