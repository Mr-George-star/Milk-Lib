package net.george.milk.potion;

import net.george.milk.MilkLib;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class RandomPurgeEffect extends MobEffect {
    public RandomPurgeEffect() {
        super(MobEffectCategory.NEUTRAL, 16777215);
    }

    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        MilkLib.tryRemoveRandomEffect(entity);
    }
}
