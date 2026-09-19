package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
    private static void onCraft(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        ItemStack ingredient = items.get(3);
        if (ingredient.isEmpty()) return;

        boolean changed = false;
        for (int i = 0; i < 3; i++) {
            ItemStack bottle = items.get(i);
            if (bottle.isEmpty()) continue;

            if (bottle.getItem() == MilkLib.MILK_BOTTLE && ingredient.getItem() == Items.GUNPOWDER) {
                items.set(i, new ItemStack(MilkLib.SPLASH_MILK_BOTTLE));
                changed = true;
            } else if (bottle.getItem() == MilkLib.SPLASH_MILK_BOTTLE && ingredient.getItem() == Items.DRAGON_BREATH) {
                items.set(i, new ItemStack(MilkLib.LINGERING_MILK_BOTTLE));
                changed = true;
            }
        }

        if (changed) {
            ingredient.shrink(1);
            items.set(3, ingredient.isEmpty() ? ItemStack.EMPTY : ingredient);
            level.levelEvent(LevelEvent.SOUND_BREWING_STAND_BREW, pos, 0);
            ci.cancel();
        }
    }
}
