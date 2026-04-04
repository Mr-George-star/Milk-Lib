package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MilkBucketItem.class, priority = 921) // apply sooner, minimize conflicts
public abstract class MilkBucketItemMixin extends Item implements FluidModificationItem {
	public MilkBucketItemMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		ItemStack stack = user.getStackInHand(hand);
		BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);
		if (hitResult.getType() == HitResult.Type.MISS) {
			cir.setReturnValue(TypedActionResult.pass(stack));
			return;
		}
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			cir.setReturnValue(TypedActionResult.pass(stack));
			return;
		}

		BlockPos hitPos = hitResult.getBlockPos();
		Direction side = hitResult.getSide();
		BlockPos placePos = hitPos.offset(side);
		if (!world.canPlayerModifyAt(user, hitPos) || !user.canPlaceOn(placePos, side, stack)) {
			cir.setReturnValue(TypedActionResult.fail(stack));
			return;
		}
		if (world.getBlockState(hitPos).isIn(MilkLib.MILK_PLACEMENT_DISALLOWED)) {
			cir.setReturnValue(TypedActionResult.pass(stack));
			return;
		}

		if (placeMilk(user, world, placePos, hitResult)) {
			onEmptied(user, world, stack, placePos);
			if (user instanceof ServerPlayerEntity serverPlayer) {
				serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
			}
			ItemStack resultStack = user.isInCreativeMode() ? stack : new ItemStack(Items.BUCKET);
			cir.setReturnValue(TypedActionResult.success(resultStack, world.isClient()));
		} else {
			cir.setReturnValue(TypedActionResult.fail(stack));
		}
	}

	@Unique
	private boolean placeMilk(PlayerEntity player, World world, BlockPos pos, BlockHitResult hitResult) {
		Fluid fluid = MilkLib.STILL_MILK;
		if (!(fluid instanceof FlowableFluid flowableFluid)) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		BlockState targetState = fluid.getDefaultState().getBlockState();
		boolean canPlace = state.isAir() || state.canBucketPlace(fluid) || (state.getBlock() instanceof FluidFillable && ((FluidFillable) state.getBlock()).canFillWithFluid(player, world, pos, state, fluid));

		if (!canPlace) {
			return hitResult != null && placeMilk(player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
		}

		if (world.getDimension().ultrawarm()) {
			int x = pos.getX(), y = pos.getY(), z = pos.getZ();
			world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
			for (int i = 0; i < 8; i++) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(), z + Math.random(), 0.0, 0.0, 0.0);
			}
			return true;
		}

		if (state.getBlock() instanceof FluidFillable fillable && fluid == MilkLib.STILL_MILK) {
			fillable.tryFillWithFluid(world, pos, state, flowableFluid.getStill(false));
			playEmptyingSound(player, world, pos);
			return true;
		}

		// isLiquid
		if (!world.isClient && !state.isFullCube(world, pos)) {
			world.breakBlock(pos, true);
		}
		if (!world.setBlockState(pos, targetState, 11) && !state.getFluidState().isStill()) {
			return false;
		}
		playEmptyingSound(player, world, pos);
		return true;
	}

	@Unique
	private void playEmptyingSound(PlayerEntity player, WorldAccess world, BlockPos pos) {
		world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
	}
}
