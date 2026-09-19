package net.george.milk.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.PointedDripstoneBlock.FluidInfo;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("unused")
public interface DrippableFluid {
    float WATER_DRIP_CHANCE = 0.17578125f;
    float LAVA_DRIP_CHANCE = 0.05859375f;

    /**
     * The chance for this fluid to drip from dripstone, between 0 and 1.
     * @see DrippableFluid#WATER_DRIP_CHANCE
     * @see DrippableFluid#LAVA_DRIP_CHANCE
     */
    float getFluidDripChance(Level world, FluidInfo fluid);

    /**
     * @return true if this fluid should cause pointed dripstone to grow downwards when placed above
     */
    boolean growsDripstone(BlockState state);

    /**
     * The color, in integer form, of the particle created by this fluid.
     * Tip: use hex codes! Ex. 0xFFFFFF for white.
     */
    int getParticleColor(Level world, double x, double y, double z, double velocityX, double velocityY, double velocityZ);

    /**
     * @return true if this fluid should drip into and full cauldrons.
     */
    boolean fillsCauldrons(BlockState state, Level world, BlockPos cauldronPos);

    /**
     * The block state to set when dripstone drips this fluid into a {@link CauldronBlock}, filling it.
     * Only called if {@link DrippableFluid#fillsCauldrons(BlockState, Level, BlockPos)} returns true.
     * Remember to implement {@link AbstractCauldronBlock#receiveStalactiteDrip} if you have a custom cauldron block.
     */
    BlockState getCauldronBlockState(BlockState state, Level world, BlockPos cauldronPos);

    /**
     * The world event triggered when a cauldron is filled with this fluid
     * @see LevelEvent#SOUND_DRIP_WATER_INTO_CAULDRON
     * @see LevelEvent#SOUND_DRIP_LAVA_INTO_CAULDRON
     */
    default int getFluidDripWorldEvent(BlockState state, Level world, BlockPos cauldronPos) {
        return LevelEvent.SOUND_DRIP_WATER_INTO_CAULDRON;
    }
}
