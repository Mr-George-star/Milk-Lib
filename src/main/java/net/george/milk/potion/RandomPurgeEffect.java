package net.george.milk.potion;

import net.george.milk.MilkLib;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class RandomPurgeEffect extends StatusEffect {
    public RandomPurgeEffect() {
        super(StatusEffectCategory.NEUTRAL, 16777215);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        MilkLib.tryRemoveRandomEffect(entity);
    }
}
