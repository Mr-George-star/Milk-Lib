package net.george.milk.mixin;

import net.george.milk.potion.MilkAreaEffectCloudEntity;
import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ThrownLingeringPotion.class)
public abstract class LingeringPotionEntityMixin extends AbstractThrownPotion implements PotionItemEntityExtensions {
    @Unique
    private boolean milk = false;

    public LingeringPotionEntityMixin(EntityType<? extends AbstractThrownPotion> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "onHitAsPotion", cancellable = true)
    private void milkLib$spawnAreaEffectCloud(ServerLevel level, ItemStack potionItem, HitResult hitResult, CallbackInfo ci) {
        if (this.isMilk()) {
            MilkAreaEffectCloudEntity areaEffectCloudEntity = new MilkAreaEffectCloudEntity(this.level(), this.getX(), this.getY(), this.getZ());
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                areaEffectCloudEntity.setOwner((LivingEntity) entity);
            }

            areaEffectCloudEntity.setRadius(3.0F);
            areaEffectCloudEntity.setRadiusOnUse(-0.5F);
            areaEffectCloudEntity.setDuration(600);
            areaEffectCloudEntity.setWaitTime(10);
            areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());

            this.level().addFreshEntity(areaEffectCloudEntity);
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
