package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
//    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
//    private static void onCraft(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity, CallbackInfo ci) {
//        ItemStack ingredient = entity.getItem(3);
//        if (ingredient.isEmpty()) return;
//
//        boolean changed = false;
//        for (int i = 0; i < 3; i++) {
//            ItemStack bottle = entity.getItem(i);
//            if (bottle.isEmpty()) continue;
//
//            if (bottle.getItem() == MilkLib.MILK_BOTTLE && ingredient.getItem() == Items.GUNPOWDER) {
//                entity.setItem(i, new ItemStack(MilkLib.SPLASH_MILK_BOTTLE));
//                changed = true;
//            } else if (bottle.getItem() == MilkLib.SPLASH_MILK_BOTTLE && ingredient.getItem() == Items.DRAGON_BREATH) {
//                entity.setItem(i, new ItemStack(MilkLib.LINGERING_MILK_BOTTLE));
//                changed = true;
//            }
//        }
//
//        if (changed) {
//            ingredient.shrink(1);
//            entity.setItem(3, ingredient.isEmpty() ? ItemStack.EMPTY : ingredient);
//            level.levelEvent(LevelEvent.SOUND_BREWING_STAND_BREW, pos, 0);
//            ci.cancel();
//        }
//    }
}
