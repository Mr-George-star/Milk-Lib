package net.george.milk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MilkCauldronBlock extends LayeredCauldronBlock {
	static final CauldronInteraction.Dispatcher DISPATCHER = CauldronInteractions.newDispatcher("milk");
	static final CauldronInteraction FILL_FROM_BUCKET = (ignored, world, pos, player, hand, stack) ->
			CauldronInteractions.emptyBucket(world, pos, player, hand, stack, MilkLib.MILK_CAULDRON.defaultBlockState().setValue(LEVEL, 3), SoundEvents.BUCKET_EMPTY);
	static final CauldronInteraction EMPTY_TO_BUCKET = (state, world, pos, player, hand, stack) ->
			CauldronInteractions.fillBucket(state, world, pos, player, hand, stack, new ItemStack(Items.MILK_BUCKET), blockState -> blockState.getValue(LEVEL) == 3, SoundEvents.BUCKET_FILL);
	static final CauldronInteraction MILKIFY_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
		if (!world.isClientSide()) {
			player.setItemInHand(hand, DyedItemColor.applyDyes(stack, List.of(DyeColor.WHITE)));
			player.awardStat(Stats.CLEAN_ARMOR);
			LayeredCauldronBlock.lowerFillLevel(state, world, pos);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	};
	static final CauldronInteraction MILKIFY_SHULKER_BOX = (state, world, pos, player, hand, stack) -> {
		Block block = Block.byItem(stack.getItem());
		if ((block instanceof ShulkerBoxBlock)) {
			if (!world.isClientSide()) {
				ItemStack itemStack = stack.transmuteCopy(Blocks.DYED_SHULKER_BOX.white(), 1);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, itemStack, false));
				player.awardStat(Stats.CLEAN_SHULKER_BOX);
				LayeredCauldronBlock.lowerFillLevel(state, world, pos);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	};
	static final CauldronInteraction MILKIFY_BANNER = (state, world, pos, player, hand, stack) -> {
		if (!world.isClientSide()) {
			ItemStack itemStack = new ItemStack(Items.BANNER.white());
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}

			if (stack.isEmpty()) {
				player.setItemInHand(hand, itemStack);
			} else if (player.getInventory().add(itemStack)) {
				player.inventoryMenu.sendAllDataToRemote();
			} else {
				player.drop(itemStack, false);
			}

			player.awardStat(Stats.CLEAN_BANNER);
			LayeredCauldronBlock.lowerFillLevel(state, world, pos);
		}
		return InteractionResult.SUCCESS;
	};

	public MilkCauldronBlock(Properties settings) {
		super(Biome.Precipitation.NONE, DISPATCHER, settings);
	}

	@Override
	protected boolean canReceiveStalactiteDrip(@NotNull Fluid fluid) {
		return fluid instanceof MilkFluid;
	}

	@Override
	protected void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Entity entity, @NotNull InsideBlockEffectApplier handler, boolean bl) {
		if (world.isClientSide()) {
			return;
		}
		if (!this.isEntityTouchingFluid(state, pos, entity) || !entity.mayInteract((ServerLevel) world, pos)) {
			return;
		}
		if (entity instanceof LivingEntity livingEntity && MilkLib.tryRemoveRandomEffect(livingEntity)) {
			lowerFillLevel(state, world, pos);
		}
		handler.apply(InsideBlockEffectType.EXTINGUISH);
	}

	protected boolean isEntityTouchingFluid(BlockState state, BlockPos pos, Entity entity) {
		return entity.getY() < (double) pos.getY() + this.getContentHeight(state) && entity.getBoundingBox().maxY > (double) pos.getY() + 0.25;
	}

	@NotNull
	@Override
	protected ItemStack getCloneItemStack(@NotNull LevelReader world, @NotNull BlockPos pos, @NotNull BlockState state, boolean includeData) {
		return Items.CAULDRON.getDefaultInstance();
	}

	public static CauldronInteraction addBehavior(CauldronInteraction behavior, Item item) {
		DISPATCHER.put(item, behavior);
		return behavior;
	}

	public static void addBehavior(CauldronInteraction behavior, TagKey<Item> tag) {
		DISPATCHER.put(tag, behavior);
	}

	public static CauldronInteraction addInputToCauldronExchange(Item toEmpty, Item emptied) {
		CauldronInteraction behavior = addBehavior(new InputToCauldronCauldronBehavior(toEmpty, emptied), toEmpty);
		CauldronInteractions.EMPTY.put(toEmpty, behavior);
		return behavior;
	}

	public static CauldronInteraction addOutputToItemExchange(Item toFill, Item filled) {
		return addBehavior(new OutputToItemCauldronBehavior(toFill, filled), toFill);
	}

	public record OutputToItemCauldronBehavior(Item toFill, Item filled) implements CauldronInteraction {
		@NotNull
		@Override
		public InteractionResult interact(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack held) {
			if (!world.isClientSide() && held.is(this.toFill)) {
				Item item = held.getItem();
				player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, this.filled.getDefaultInstance()));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(item));
				LayeredCauldronBlock.lowerFillLevel(state, world, pos);
				world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
			}
			return InteractionResult.SUCCESS;
		}
	}

	public record InputToCauldronCauldronBehavior(Item toEmpty, Item emptied) implements CauldronInteraction {
		@NotNull
		@Override
		public InteractionResult interact(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack stack) {
			Block block = state.getBlock();
			if ((block == Blocks.CAULDRON || block == MilkLib.MILK_CAULDRON) && (!state.hasProperty(LEVEL) || state.getValue(LEVEL) != 3) && stack.is(this.toEmpty)) {
				if (!world.isClientSide()) {
					player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, this.emptied.getDefaultInstance()));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
					if (block == Blocks.CAULDRON) {
						world.setBlockAndUpdate(pos, MilkLib.MILK_CAULDRON.defaultBlockState().setValue(LEVEL, 1));
					} else {
						world.setBlockAndUpdate(pos, state.setValue(LEVEL, state.getValue(LEVEL) + 1));
					}
					world.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
	}
}
