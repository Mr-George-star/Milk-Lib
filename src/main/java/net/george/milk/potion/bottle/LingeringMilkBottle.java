package net.george.milk.potion.bottle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class LingeringMilkBottle extends LingeringPotionItem {
	public LingeringMilkBottle(Item.Properties settings) {
		super(settings);
	}

	@NotNull
	@Override
	public InteractionResult use(Level world, Player user, 	@NotNull InteractionHand hand) {
		world.playSound(null, user.getX(), user.getY(), user.getZ(),
				SoundEvents.LINGERING_POTION_THROW, SoundSource.NEUTRAL,
				0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

		ItemStack itemStack = user.getItemInHand(hand);
		if (!world.isClientSide()) {
			AbstractThrownPotion potionEntity = new ThrownLingeringPotion(world, user, itemStack);
			potionEntity.setItem(itemStack);
			potionEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), -20.0F, 0.5F, 1.0F);
			((PotionItemEntityExtensions) potionEntity).setMilk(true);
			world.addFreshEntity(potionEntity);
		}

		user.awardStat(Stats.ITEM_USED.get(this));
		if (!user.getAbilities().instabuild) {
			itemStack.shrink(1);
		}

		return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
	}

	@NotNull
	@Override
	public ItemStack getDefaultInstance() {
		ItemStack stack = super.getDefaultInstance();
		stack.remove(DataComponents.POTION_CONTENTS);
		return stack;
	}

	@Override
	public ItemStackTemplate getCraftingRemainder(@NotNull ItemStack stack) {
		return new ItemStackTemplate(Items.GLASS_BOTTLE);
	}
}
