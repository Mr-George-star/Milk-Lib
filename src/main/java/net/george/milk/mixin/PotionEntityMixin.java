package net.george.milk.mixin;

import net.george.milk.potion.bottle.LingeringMilkBottle;
import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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

import java.util.Optional;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity implements FlyingItemEntity, PotionItemEntityExtensions {
    @Shadow protected abstract void extinguishFire(BlockPos pos);
    @Shadow protected abstract void spawnAreaEffectCloud(ServerWorld world, ItemStack stack, @Nullable Entity entityHit);
    @Shadow protected abstract void explodeWaterPotion(ServerWorld world);

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
                ServerWorld serverWorld = (ServerWorld) this.getWorld();
                explodeWaterPotion(serverWorld);
                if (this.getStack().getItem() instanceof LingeringMilkBottle) {
                    spawnAreaEffectCloud(serverWorld, null, null);
                } else {
                    spawnAreaEffectCloud(serverWorld, null, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) hitResult).getEntity() : null);
                }

                this.getWorld().syncWorldEvent(WorldEvents.INSTANT_SPLASH_POTION_SPLASHED, this.getBlockPos(), 0xFFFFFF);
                this.discard();
            }
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
        Optional<Boolean> isMilk = nbt.getBoolean("Milk");
        isMilk.ifPresent(this::setMilk);
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
