package net.george.milk;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

public class MilkCauldronBlock extends LeveledCauldronBlock {
	static final CauldronBehavior.CauldronBehaviorMap MILK_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("milk");
	static final CauldronBehavior FILL_FROM_BUCKET = (state, world, pos, player, hand, stack) ->
			CauldronBehavior.fillCauldron(world, pos, player, hand, stack, MilkLib.MILK_CAULDRON.getDefaultState().with(LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY);
	static final CauldronBehavior EMPTY_TO_BUCKET = (state, world, pos, player, hand, stack) ->
			CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.MILK_BUCKET), blockState -> blockState.get(LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL);
	static final CauldronBehavior MILKIFY_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
		if (!world.isClient) {
			player.setStackInHand(hand, DyedColorComponent.setColor(stack, Lists.newArrayList(DyeItem.byColor(DyeColor.WHITE))));
			player.incrementStat(Stats.CLEAN_ARMOR);
			LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
	};
	static final CauldronBehavior MILKIFY_SHULKER_BOX = (state, world, pos, player, hand, stack) -> {
		Block block = Block.getBlockFromItem(stack.getItem());
		if ((block instanceof ShulkerBoxBlock)) {
			if (!world.isClient) {
				ItemStack itemStack = stack.copyComponentsToNewStack(Blocks.WHITE_SHULKER_BOX, 1);
                player.setStackInHand(hand, itemStack);
				player.incrementStat(Stats.CLEAN_SHULKER_BOX);
				LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
	};
	static final CauldronBehavior MILKIFY_BANNER = (state, world, pos, player, hand, stack) -> {
		if (!world.isClient()) {
			ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
			if (!player.getAbilities().creativeMode) {
				stack.decrement(1);
			}

			if (stack.isEmpty()) {
				player.setStackInHand(hand, itemStack);
			} else if (player.getInventory().insertStack(itemStack)) {
				player.playerScreenHandler.syncState();
			} else {
				player.dropItem(itemStack, false);
			}

			player.incrementStat(Stats.CLEAN_BANNER);
			LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
		}
		return ActionResult.SUCCESS;
	};

	public MilkCauldronBlock(Settings settings) {
		super(Biome.Precipitation.NONE, MILK_CAULDRON_BEHAVIOR, settings);

	}

	@Override
	protected boolean canBeFilledByDripstone(Fluid fluid) {
		return fluid instanceof MilkFluid;
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
		if (!world.isClient && isEntityTouchingFluid(state, pos, entity) && entity.canModifyAt((ServerWorld) world, pos)) {
			boolean shouldDrain = false;
			if (entity.isOnFire()) {
				entity.extinguish();
				shouldDrain = true;
			}

			if (entity instanceof LivingEntity livingEntity) {
				shouldDrain = MilkLib.tryRemoveRandomEffect(livingEntity);
			}

			if (shouldDrain) {
				decrementFluidLevel(state, world, pos);
			}
		}
	}

	@Override
	protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return Items.CAULDRON.getDefaultStack();
	}

	public static CauldronBehavior addBehavior(CauldronBehavior behavior, Item items) {
		MILK_CAULDRON_BEHAVIOR.map().put(items, behavior);
		return behavior;
	}

	public static CauldronBehavior addInputToCauldronExchange(ItemStack toEmpty, ItemStack emptied, boolean ignoreComponent) {
		Item emptyItem = toEmpty.getItem();
		CauldronBehavior behavior = addBehavior(new InputToCauldronCauldronBehavior(toEmpty, emptied, ignoreComponent), emptyItem);
		CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.map().put(emptyItem, behavior);
		return behavior;
	}

	public static CauldronBehavior addOutputToItemExchange(ItemStack toFill, ItemStack filled, boolean ignoreComponent) {
		return addBehavior(new OutputToItemCauldronBehavior(toFill, filled, ignoreComponent), toFill.getItem());
	}

	private static boolean typeAndDataEqual(ItemStack stack1, ItemStack stack2, boolean ignoreComponent) {
		if (ignoreComponent) {
			return ItemStack.areItemsEqual(stack1, stack2);
		} else {
			return ItemStack.areItemsAndComponentsEqual(stack1, stack2);
		}
	}

	public record OutputToItemCauldronBehavior(ItemStack toFill, ItemStack filled, boolean ignoreComponent) implements CauldronBehavior {
		@Override
		public ActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack held) {
			if (!world.isClient && typeAndDataEqual(held, this.toFill, this.ignoreComponent)) {
				Item item = held.getItem();
				player.setStackInHand(hand, ItemUsage.exchangeStack(held, player, this.filled.copy()));
				player.incrementStat(Stats.USE_CAULDRON);
				player.incrementStat(Stats.USED.getOrCreateStat(item));
				LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
				world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
				world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
			}
			return ActionResult.SUCCESS;
		}
	}

	public record InputToCauldronCauldronBehavior(ItemStack toEmpty, ItemStack emptied, boolean ignoreComponent) implements CauldronBehavior {
		@Override
		public ActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
			Block block = state.getBlock();
			if ((block == Blocks.CAULDRON || block == MilkLib.MILK_CAULDRON) && (!state.contains(LEVEL) || state.get(LEVEL) != 3) && typeAndDataEqual(stack, this.toEmpty, this.ignoreComponent)) {
				if (!world.isClient) {
					player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, this.emptied.copy()));
					player.incrementStat(Stats.USE_CAULDRON);
					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
					if (block == Blocks.CAULDRON) {
						world.setBlockState(pos, MilkLib.MILK_CAULDRON.getDefaultState().with(LEVEL, 1));
					} else {
						world.setBlockState(pos, state.with(LEVEL, state.get(LEVEL) + 1));
					}
					world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
					world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
				}
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
		}
	}
}
