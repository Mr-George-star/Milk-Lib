package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.george.milk.MilkLibTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Item.class, priority = 921) // apply sooner, minimize conflicts
public abstract class MilkBucketItemMixin implements DispensibleContainerItem {
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void onUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.MILK_BUCKET)) {
			cir.cancel();
			return;
		}

		BlockHitResult hitResult = milkLib$raycast(level, player);
		if (hitResult.getType() == HitResult.Type.MISS) {
			cir.setReturnValue(InteractionResult.PASS);
			return;
		}
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			cir.setReturnValue(InteractionResult.PASS);
			return;
		}

		BlockPos hitPos = hitResult.getBlockPos();
		Direction side = hitResult.getDirection();
		BlockPos placePos = hitPos.relative(side);
		if (!level.mayInteract(player, hitPos) || !player.mayUseItemAt(placePos, side, stack)) {
			cir.setReturnValue(InteractionResult.FAIL);
			return;
		}
		if (level.getBlockState(hitPos).is(MilkLibTags.MILK_PLACEMENT_DISALLOWED)) {
			cir.setReturnValue(InteractionResult.PASS);
			return;
		}

		if (placeMilk(player, level, placePos, hitResult)) {
			checkExtraContent(player, level, stack, placePos);
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.awardStat(Stats.ITEM_USED.get(Items.MILK_BUCKET));
			}
			ItemStack resultStack = player.hasInfiniteMaterials() ? stack : new ItemStack(Items.BUCKET);
			cir.setReturnValue(InteractionResult.SUCCESS.heldItemTransformedTo(resultStack));
		} else {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Unique
	private boolean placeMilk(Player player, Level world, BlockPos pos, BlockHitResult hitResult) {
		Fluid fluid = MilkLib.STILL_MILK;
		if (!(fluid instanceof FlowingFluid flowableFluid)) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		BlockState targetState = fluid.defaultFluidState().createLegacyBlock();
		boolean canPlace = state.isAir() || state.canBeReplaced(fluid) || (state.getBlock() instanceof LiquidBlockContainer && ((LiquidBlockContainer) state.getBlock()).canPlaceLiquid(player, world, pos, state, fluid));

		if (!canPlace) {
			return hitResult != null && placeMilk(player, world, hitResult.getBlockPos().relative(hitResult.getDirection()), null);
		}

		if (world.environmentAttributes().getDimensionValue(EnvironmentAttributes.FAST_LAVA)) {
			int x = pos.getX(), y = pos.getY(), z = pos.getZ();
			world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.8F);
			for (int i = 0; i < 8; i++) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(), z + Math.random(), 0.0, 0.0, 0.0);
			}
			return true;
		}

		if (state.getBlock() instanceof LiquidBlockContainer fillable && fluid == MilkLib.STILL_MILK) {
			fillable.placeLiquid(world, pos, state, flowableFluid.getSource(false));
			playEmptyingSound(player, world, pos);
			return true;
		}

		// isLiquid
		if (!world.isClientSide() && !state.isCollisionShapeFullBlock(world, pos)) {
			world.destroyBlock(pos, true);
		}
		if (!world.setBlock(pos, targetState, 11) && !state.getFluidState().isSource()) {
			return false;
		}
		playEmptyingSound(player, world, pos);
		return true;
	}

	@Unique
	private void playEmptyingSound(Player player, LevelAccessor world, BlockPos pos) {
		world.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
	}

	@Unique
	private static BlockHitResult milkLib$raycast(Level world, Player player) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 rotated = eyePos.add(Entity.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange()));
		return world.clip(new ClipContext(eyePos, rotated, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
	}
}
