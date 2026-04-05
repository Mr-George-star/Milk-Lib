package net.george.milk.potion;

import net.george.milk.MilkLib;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MilkArrowEntity extends PersistentProjectileEntity {
    public MilkArrowEntity(EntityType<MilkArrowEntity> entityType, World world) {
        super(entityType, world);
    }

    protected MilkArrowEntity(double x, double y, double z, World world, ItemStack stack, @Nullable ItemStack weapon) {
        super(MilkLib.MILK_ARROW_ENTITY_TYPE, x, y, z, world, stack, weapon);
    }

    protected MilkArrowEntity(LivingEntity owner, World world, ItemStack stack, @Nullable ItemStack shotFrom) {
        super(MilkLib.MILK_ARROW_ENTITY_TYPE, owner, world, stack, shotFrom);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(MilkLib.MILK_ARROW);
    }

    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);
        target.addStatusEffect(MilkLib.createRandomPurgeEffect(), this.getEffectCause());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient && !this.isInGround()) {
            this.getWorld().addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0);
        }
    }
}
