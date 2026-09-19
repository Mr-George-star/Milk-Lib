package net.george.milk.mixin;

import com.google.common.annotations.VisibleForTesting;
import net.george.milk.api.DrippableFluid;
import net.george.milk.api.DrippableFluidManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = PointedDripstoneBlock.class, priority = 429) // random number to apply overwriting early, let other mods inject
public abstract class PointedDripstoneBlockMixin {
    @Shadow
    private static boolean isStalactiteStartPos(BlockState state, LevelReader level, BlockPos pos) {
        throw new RuntimeException("Mixin application failed!");
    }

    @Shadow
    @Nullable
    private static BlockPos findTip(BlockState dripstoneState, LevelAccessor level, BlockPos dripstonePos, int maxSearchLength, boolean includeMergedTip) {
        throw new RuntimeException("Mixin application failed!");
    }

    @Shadow
    private static ParticleOptions getDripParticle(Level level, Fluid fluidAbove, BlockPos posAbove) {
        throw new RuntimeException("Mixin application failed!");
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

    @Shadow
    @Final
    private static double STALACTITE_DRIP_START_PIXEL;

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
     * @reason get particle effect for other fluids, requires access to the fluid gotten from getDripFluid
     * @author Tropheus Jay
     */
    @Overwrite
    private static void spawnDripParticle(Level world, BlockPos pos, BlockState state, Fluid fluid, BlockPos fluidPos) {
        Vec3 vec3d = state.getOffset(pos);
        double x = pos.getX() + 0.5F + vec3d.x;
        double y = pos.getY() + STALACTITE_DRIP_START_PIXEL - 0.0625F;
        double z = pos.getZ() + 0.5F + vec3d.z;
        ParticleOptions particleEffect;
        if (fluid instanceof DrippableFluid interactingFluid) {
            particleEffect = DrippableFluidManager.getInstance().getSet(interactingFluid).hang();
        } else {
            particleEffect = getDripParticle(world, fluid, fluidPos);
        }
        world.addParticle(particleEffect, x, y, z, 0.0F, 0.0F, 0.0F);
    }

    /**
     * @reason allow fluids other than water to grow dripstone
     * @author Tropheus Jay
     */
    @Overwrite
    private static boolean canGrow(BlockState dripstoneBlockState, BlockState fluidState) {
        Fluid fluid = fluidState.getFluidState().getType();
        boolean growsDripstone = fluidState.is(Blocks.WATER);
        if (fluid instanceof DrippableFluid interactingFluid) {
            growsDripstone = interactingFluid.growsDripstone(fluidState);
        }

        return dripstoneBlockState.is(Blocks.DRIPSTONE_BLOCK) && growsDripstone && fluidState.getFluidState().isSource();
    }

    @Inject(method = "canFillCauldron", at = @At("HEAD"), cancellable = true)
    private static void milkLib$makeCustomFluidsValid(Fluid fluidAbove, CallbackInfoReturnable<Boolean> cir) {
        if (fluidAbove instanceof DrippableFluid) {
            cir.setReturnValue(true);
        }
    }
}
