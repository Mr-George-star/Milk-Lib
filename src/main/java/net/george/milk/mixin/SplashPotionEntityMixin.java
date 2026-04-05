package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SplashPotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(SplashPotionEntity.class)
public abstract class SplashPotionEntityMixin extends PotionEntity implements PotionItemEntityExtensions {
    @Unique
    private boolean milk = false;

    public SplashPotionEntityMixin(EntityType<? extends PotionEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "spawnAreaEffectCloud", cancellable = true)
    private void milkLib$spawnAreaEffectCloud(ServerWorld world, ItemStack stack, Entity entityHit, CallbackInfo ci) {
        if (this.isMilk()) {
            Box box = this.getBoundingBox().expand(4.0, 2.0, 4.0);
            List<LivingEntity> list = this.getWorld().getNonSpectatingEntities(LivingEntity.class, box);
            if (!list.isEmpty()) {
                for (LivingEntity livingEntity : list) {
                    if (livingEntity.isAffectedBySplashPotions() && !livingEntity.hasStatusEffect(MilkLib.RANDOM_PURGE)) {
                        double d = this.squaredDistanceTo(livingEntity);
                        if (d < 16.0) {
                            livingEntity.addStatusEffect(MilkLib.createRandomPurgeEffect());
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
