package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private static void onCraft(World world, BlockPos pos, DefaultedList<ItemStack> slots, CallbackInfo ci) {
        ItemStack ingredient = slots.get(3);
        if (ingredient.isEmpty()) return;

        boolean changed = false;
        for (int i = 0; i < 3; i++) {
            ItemStack bottle = slots.get(i);
            if (bottle.isEmpty()) continue;

            if (bottle.getItem() == MilkLib.MILK_BOTTLE && ingredient.getItem() == Items.GUNPOWDER) {
                slots.set(i, new ItemStack(MilkLib.SPLASH_MILK_BOTTLE));
                changed = true;
            } else if (bottle.getItem() == MilkLib.SPLASH_MILK_BOTTLE && ingredient.getItem() == Items.DRAGON_BREATH) {
                slots.set(i, new ItemStack(MilkLib.LINGERING_MILK_BOTTLE));
                changed = true;
            }
        }

        if (changed) {
            ingredient.decrement(1);
            slots.set(3, ingredient.isEmpty() ? ItemStack.EMPTY : ingredient);
            world.syncWorldEvent(WorldEvents.BREWING_STAND_BREWS, pos, 0);
            ci.cancel();
        }
    }
}
