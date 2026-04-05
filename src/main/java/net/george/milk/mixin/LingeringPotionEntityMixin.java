package net.george.milk.mixin;

import net.george.milk.potion.MilkAreaEffectCloudEntity;
import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.LingeringPotionEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(LingeringPotionEntity.class)
public abstract class LingeringPotionEntityMixin extends PotionEntity implements PotionItemEntityExtensions {
    @Unique
    private boolean milk = false;

    public LingeringPotionEntityMixin(EntityType<? extends PotionEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "spawnAreaEffectCloud", cancellable = true)
    private void milkLib$spawnAreaEffectCloud(ServerWorld world, ItemStack stack, Entity entityHit, CallbackInfo ci) {
        if (this.isMilk()) {
            MilkAreaEffectCloudEntity areaEffectCloudEntity = new MilkAreaEffectCloudEntity(this.getWorld(), this.getX(), this.getY(), this.getZ());
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                areaEffectCloudEntity.setOwner((LivingEntity) entity);
            }

            areaEffectCloudEntity.setRadius(3.0F);
            areaEffectCloudEntity.setRadiusOnUse(-0.5F);
            areaEffectCloudEntity.setWaitTime(10);
            areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());

            this.getWorld().spawnEntity(areaEffectCloudEntity);
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
