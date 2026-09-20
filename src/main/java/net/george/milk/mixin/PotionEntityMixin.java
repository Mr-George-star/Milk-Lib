package net.george.milk.mixin;

import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(AbstractThrownPotion.class)
public abstract class PotionEntityMixin extends ThrowableItemProjectile implements ItemSupplier, PotionItemEntityExtensions {
    @Shadow protected abstract void onHitAsPotion(ServerLevel world, ItemStack stack, HitResult hitResult);
    @Shadow protected abstract void affectEntitiesAround(ServerLevel level, PotionContents potion);

    @Unique
    private boolean milk = false;

    public PotionEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "onHitBlock", at = @At(value = "HEAD"))
    protected void milkLib$onBlockHit(BlockHitResult hitResult, CallbackInfo ci) {
        if (isMilk()) {
            Direction side = hitResult.getDirection();
            BlockPos pos = hitResult.getBlockPos().relative(side);
            ((PotionEntityInvoker) this).milkLib$douseFire(pos);
            ((PotionEntityInvoker) this).milkLib$douseFire(pos.relative(side.getOpposite()));

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                ((PotionEntityInvoker) this).milkLib$douseFire(pos.relative(direction));
            }
        }
    }

    @Inject(method = "onHit", at = @At(value = "HEAD"), cancellable = true)
    protected void milkLib$onCollision(HitResult hitResult, CallbackInfo ci) {
        if (isMilk()) {
            super.onHit(hitResult);
            if (!this.level().isClientSide()) {
                ServerLevel serverWorld = (ServerLevel) this.level();
                affectEntitiesAround(serverWorld, PotionContents.EMPTY);
                onHitAsPotion(serverWorld, null, hitResult);

                this.level().levelEvent(LevelEvent.PARTICLES_INSTANT_POTION_SPLASH, this.blockPosition(), 0xFFFFFF);
                this.discard();
            }
            ci.cancel();
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putBoolean("Milk", this.milk);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput view) {
        super.readAdditionalSaveData(view);
        this.milk = view.getBooleanOr("Milk", false);
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
