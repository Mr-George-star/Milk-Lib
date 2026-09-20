package net.george.milk.potion.bottle;

import net.george.milk.MilkLib;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class MilkBottle extends PotionItem {
	public MilkBottle(Properties settings) {
		super(settings);
	}

	@NotNull
	@Override
	public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
		Player playerEntity = user instanceof Player ? (Player) user : null;
		if (playerEntity instanceof ServerPlayer player) {
			CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
		}

		if (!world.isClientSide() && !user.hasEffect(MilkLib.RANDOM_PURGE)) {
			user.addEffect(MilkLib.createRandomPurgeEffect());
		}

		if (playerEntity != null) {
			playerEntity.awardStat(Stats.ITEM_USED.get(this));
			if (!playerEntity.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}

		if (playerEntity == null || !playerEntity.getAbilities().instabuild) {
			if (stack.isEmpty()) {
				return new ItemStack(Items.GLASS_BOTTLE);
			}

			if (playerEntity != null) {
				playerEntity.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
			}
		}

		user.gameEvent(GameEvent.DRINK);
		return stack;
	}

	@NotNull
	@Override
	public InteractionResult use(@NotNull Level world, @NotNull Player user, @NotNull InteractionHand hand) {
		return ItemUtils.startUsingInstantly(world, user, hand);
	}

	@NotNull
	@Override
	public ItemStack getDefaultInstance() {
		ItemStack stack = super.getDefaultInstance();
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(MilkLib.RANDOM_PURGE_POTION));
		return stack;
	}

	@Override
	public ItemStackTemplate getCraftingRemainder(@NotNull ItemStack stack) {
		return new ItemStackTemplate(Items.GLASS_BOTTLE);
	}
}
