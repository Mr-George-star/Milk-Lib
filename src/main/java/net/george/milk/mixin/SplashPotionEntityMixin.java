package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ThrownSplashPotion.class)
public abstract class SplashPotionEntityMixin extends AbstractThrownPotion implements PotionItemEntityExtensions {
    @Unique
    private boolean milk = false;

    public SplashPotionEntityMixin(EntityType<? extends AbstractThrownPotion> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "onHitAsPotion", cancellable = true)
    private void milkLib$spawnAreaEffectCloud(ServerLevel level, ItemStack potionItem, HitResult hitResult, CallbackInfo ci) {
        if (this.isMilk()) {
            AABB box = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
            List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, box);
            if (!list.isEmpty()) {
                for (LivingEntity livingEntity : list) {
                    if (livingEntity.isAffectedByPotions() && !livingEntity.hasEffect(MilkLib.RANDOM_PURGE)) {
                        double d = this.distanceToSqr(livingEntity);
                        if (d < 16.0) {
                            livingEntity.addEffect(MilkLib.createRandomPurgeEffect());
                        }
                    }
                }
            }
            ci.cancel();
        }
    }

    @Override
    public boolean isMilk() {
        return this.milk;
    }

    @Override
    public void setMilk(boolean value) {
        this.milk = value;
    }
}
