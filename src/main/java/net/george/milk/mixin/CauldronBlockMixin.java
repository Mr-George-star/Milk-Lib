package net.george.milk.mixin;

import net.george.milk.api.DrippableFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CauldronBlock.class)
public abstract class CauldronBlockMixin {
    @Inject(method = "receiveStalactiteDrip", at = @At("HEAD"), cancellable = true)
    private void milkLib$customFluidsFillCauldrons(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        if (fluid instanceof DrippableFluid drippableFluid) {
            if (drippableFluid.fillsCauldrons(state, level, pos)) {
                BlockState newState = drippableFluid.getCauldronBlockState(state, level, pos);
                level.setBlockAndUpdate(pos, newState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
                level.levelEvent(drippableFluid.getFluidDripWorldEvent(state, level, pos), pos, 0);
                ci.cancel();
            }
        }
    }
}
