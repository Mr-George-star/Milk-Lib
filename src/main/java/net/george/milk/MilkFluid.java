package net.george.milk;

import net.george.milk.api.DrippableFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class MilkFluid extends FlowingFluid implements DrippableFluid {
    @NotNull
    @Override
    public Fluid getSource() {
        return MilkLib.STILL_MILK;
    }

    @NotNull
    @Override
    public Fluid getFlowing() {
        return MilkLib.FLOWING_MILK;
    }

    @NotNull
    @Override
    public Item getBucket() {
        return Items.MILK_BUCKET;
    }

    @Override
    protected int getSlopeFindDistance(@NotNull LevelReader world) {
        return 2;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    protected boolean canBeReplacedWith(@NotNull FluidState fluidState, @NotNull BlockGetter blockView, @NotNull BlockPos blockPos, @NotNull Fluid fluid, @NotNull Direction direction) {
        return false;
    }

    @Override
    public int getTickDelay(@NotNull LevelReader worldView) {
        return 5;
    }

    @Override
    protected int getDropOff(@NotNull LevelReader worldView) {
        return 1;
    }

    @Override
    protected void beforeDestroyingBlock(@NotNull LevelAccessor world, @NotNull BlockPos pos, BlockState state) {
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropResources(state, world, pos, blockEntity);
    }

    @Override
    protected boolean canConvertToSource(@NotNull ServerLevel world) {
        return false;
    }

    @Override
    public boolean isSame(@NotNull Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }

    @NotNull
    @Override
    protected BlockState createLegacyBlock(@NotNull FluidState fluidState) {
        return MilkLib.MILK_FLUID_BLOCK.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(fluidState));
    }

    @NotNull
    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    public int getParticleColor(Level world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return 0xFFFFFF;
    }

    @Override
    public boolean growsDripstone(BlockState state) {
        return true;
    }

    @Override
    public boolean fillsCauldrons(BlockState state, Level world, BlockPos cauldronPos) {
        return MilkLib.MILK_CAULDRON != null;
    }

    @Override
    public @Nullable BlockState getCauldronBlockState(BlockState state, Level world, BlockPos cauldronPos) {
        return MilkLib.MILK_CAULDRON.defaultBlockState();
    }

    @Override
    public float getFluidDripChance(Level world, PointedDripstoneBlock.FluidInfo drippingFluid) {
        return WATER_DRIP_CHANCE;
    }

    public static class Flowing extends MilkFluid {
        @Override
        protected void createFluidStateDefinition(@NotNull StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState fluidState) {
            return false;
        }
    }

    public static class Still extends MilkFluid {
        @Override
        public int getAmount(@NotNull FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isSource(@NotNull FluidState fluidState) {
            return true;
        }
    }
}
