package net.george.milk.mixin;

import com.google.common.annotations.VisibleForTesting;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.george.milk.api.DrippableFluid;
import net.george.milk.api.DrippableFluidManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = PointedDripstoneBlock.class, priority = 429) // random number to apply overwriting early, let other mods inject
public abstract class PointedDripstoneBlockMixin extends SpeleothemBlock {
    public PointedDripstoneBlockMixin(BlockState blockToGrowOn, Properties properties) {
        super(blockToGrowOn, properties);
    }

    @Shadow
    @Nullable
    private static BlockPos findFillableCauldronBelowStalactiteTip(Level level, BlockPos stalactiteTipPos, Fluid fluid) {
        throw new RuntimeException("Mixin application failed!");
    }

    @Shadow
    private static Optional<PointedDripstoneBlock.FluidInfo> getFluidAboveStalactite(Level level, BlockPos stalactitePos, BlockState stalactiteState) {
        throw new RuntimeException("Mixin application failed!");
    }

    /**
     * @author Tropheus Jay
     * @reason to properly handle custom fluid drip chances, requires access to multiple variables
     */
    @VisibleForTesting
    @Overwrite
    public static void maybeTransferFluid(BlockState state, ServerLevel world, BlockPos pos, float dripChance) {
        // removed outside if statement to handle custom fluid chances
        if (isStalactiteStartPos(state, world, pos)) {
            Optional<PointedDripstoneBlock.FluidInfo> optional = getFluidAboveStalactite(world, pos, state);
            if (optional.isPresent()) {
                Fluid fluid = optional.get().fluid();
                float f;
                if (fluid == Fluids.WATER) {
                    f = 0.17578125F;
                } else if (fluid == Fluids.LAVA) {
                    f = 0.05859375F;
                } else {
                    if (fluid instanceof DrippableFluid customFluid) {
                        f = customFluid.getFluidDripChance(world, optional.get());
                    } else {
                        return;
                    }
                }

                if (!(dripChance >= f)) {
                    BlockPos blockPos = findTip(state, world, pos, 11, false);
                    if (blockPos != null) {
                        if (optional.get().sourceState().is(Blocks.MUD) && fluid == Fluids.WATER) {
                            BlockState blockState = Blocks.CLAY.defaultBlockState();
                            world.setBlockAndUpdate(optional.get().pos(), blockState);
                            Block.pushEntitiesUp(
                                    optional.get().sourceState(), blockState, world, optional.get().pos()
                            );
                            world.gameEvent(GameEvent.BLOCK_CHANGE, optional.get().pos(), GameEvent.Context.of(blockState));
                            world.levelEvent(LevelEvent.DRIPSTONE_DRIP, blockPos, 0);
                        } else {
                            BlockPos blockPos2 = findFillableCauldronBelowStalactiteTip(world, blockPos, fluid);
                            if (blockPos2 != null) {
                                world.levelEvent(LevelEvent.DRIPSTONE_DRIP, blockPos, 0);
                                int i = blockPos.getY() - blockPos2.getY();
                                int j = 50 + i;
                                BlockState blockState2 = world.getBlockState(blockPos2);
                                world.scheduleTick(blockPos2, blockState2.getBlock(), j);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get particle effect for other fluids
     * @author Tropheus Jay
     */
    @WrapOperation(
            method = "spawnDripParticle(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;getDripParticle(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/particles/ParticleOptions;")
    )
    private static ParticleOptions milkLib$spawnDripParticle(Level level, Fluid fluidAbove, BlockPos posAbove, Operation<ParticleOptions> original) {
        if (fluidAbove instanceof DrippableFluid drippableFluid) {
            return DrippableFluidManager.getInstance().getSet(drippableFluid).hang();
        }
        return original.call(level, fluidAbove, posAbove);
    }

    /**
     * Allow fluids other than water to grow dripstone
     * @author Tropheus Jay
     */
    @ModifyReturnValue(method = "canGrow", at = @At(value = "RETURN"))
    private boolean milkLib$canGrow(boolean original, @Local(name = "fluidState") FluidState fluidState) {
        if (fluidState.getType() instanceof DrippableFluid drippableFluid) {
            return original && drippableFluid.growsDripstone(fluidState);
        }
        return original;
    }

    @Inject(method = "canFillCauldron", at = @At("HEAD"), cancellable = true)
    private static void milkLib$makeCustomFluidsValid(Fluid fluidAbove, CallbackInfoReturnable<Boolean> cir) {
        if (fluidAbove instanceof DrippableFluid) {
            cir.setReturnValue(true);
        }
    }
}
