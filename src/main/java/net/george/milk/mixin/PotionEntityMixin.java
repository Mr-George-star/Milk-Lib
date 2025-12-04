package net.george.milk.mixin;

import net.george.milk.MilkLib;
import net.george.milk.potion.MilkAreaEffectCloudEntity;
import net.george.milk.potion.bottle.LingeringMilkBottle;
import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity implements FlyingItemEntity, PotionItemEntityExtensions {
    @Shadow protected abstract void extinguishFire(BlockPos pos);
    @Shadow
    protected abstract void applyWater();
    @Shadow protected abstract void applyLingeringPotion(PotionContentsComponent potion);
    @Shadow protected abstract void applySplashPotion(Iterable<StatusEffectInstance> effects, @Nullable Entity entity);

    @Unique
    private boolean milk = false;

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onBlockHit", at = @At(value = "HEAD"))
    protected void milkLib$onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (isMilk()) {
            Direction side = blockHitResult.getSide();
            BlockPos pos = blockHitResult.getBlockPos().offset(side);
            this.extinguishFire(pos);
            this.extinguishFire(pos.offset(side.getOpposite()));

            for(Direction direction2 : Direction.Type.HORIZONTAL) {
                this.extinguishFire(pos.offset(direction2));
            }
        }
    }

    @Inject(method = "onCollision", at = @At(value = "HEAD"), cancellable = true)
    protected void milkLib$onCollision(HitResult hitResult, CallbackInfo ci) {
        if (isMilk()) {
            super.onCollision(hitResult);
            if (!this.getWorld().isClient) {
                applyWater();
                if (this.getStack().getItem() instanceof LingeringMilkBottle) {
                    applyLingeringPotion(null);
                } else {
                    applySplashPotion(null, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) hitResult).getEntity() : null);
                }

                this.getWorld().syncWorldEvent(WorldEvents.INSTANT_SPLASH_POTION_SPLASHED, this.getBlockPos(), 0xFFFFFF);
                this.discard();
            }
            ci.cancel();
        }
    }

    @Inject(method = "applySplashPotion", at = @At("HEAD"), cancellable = true)
    private void milkLib$applySplashPotion(Iterable<StatusEffectInstance> effects, Entity entity, CallbackInfo ci) {
        if (isMilk()) {
            Box box = this.getBoundingBox().expand(4.0, 2.0, 4.0);
            List<LivingEntity> list = this.getWorld().getNonSpectatingEntities(LivingEntity.class, box);
            if (!list.isEmpty()) {
                for (LivingEntity livingEntity : list) {
                    if (livingEntity.isAffectedBySplashPotions()) {
                        double d = this.squaredDistanceTo(livingEntity);
                        if (d < 16.0) {
                            MilkLib.tryRemoveRandomEffect(livingEntity);
                        }
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "applyLingeringPotion", at = @At("HEAD"), cancellable = true)
    private void milkLib$applyLingeringPotion(PotionContentsComponent potion, CallbackInfo ci) {
        if (isMilk()) {
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
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Milk", this.milk);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setMilk(nbt.getBoolean("Milk"));
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
