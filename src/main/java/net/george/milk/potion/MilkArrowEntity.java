package net.george.milk.potion;

import com.google.common.base.Suppliers;
import net.george.milk.MilkLib;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.EffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MilkArrowEntity extends PersistentProjectileEntity {
    private static final Supplier<EffectParticleEffect> effectSupplier = Suppliers.memoize(() ->
            EffectParticleEffect.of(ParticleTypes.INSTANT_EFFECT, 16777215, 10)
    );

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
        if (this.getEntityWorld().isClient() && !this.isInGround()) {
            this.getEntityWorld().addParticleClient(effectSupplier.get(),
                    this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0);
        }
    }
}
