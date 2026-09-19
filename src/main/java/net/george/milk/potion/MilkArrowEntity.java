package net.george.milk.potion;

import com.google.common.base.Suppliers;
import net.george.milk.MilkLib;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MilkArrowEntity extends AbstractArrow {
    private static final Supplier<SpellParticleOption> effectSupplier = Suppliers.memoize(() ->
            SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT, 16777215, 10)
    );

    public MilkArrowEntity(EntityType<MilkArrowEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected MilkArrowEntity(double x, double y, double z, Level world, ItemStack stack, @Nullable ItemStack weapon) {
        super(MilkLib.MILK_ARROW_ENTITY_TYPE, x, y, z, world, stack, weapon);
    }

    protected MilkArrowEntity(LivingEntity owner, Level world, ItemStack stack, @Nullable ItemStack shotFrom) {
        super(MilkLib.MILK_ARROW_ENTITY_TYPE, owner, world, stack, shotFrom);
    }

    @NotNull
    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(MilkLib.MILK_ARROW);
    }

    @Override
    protected void doPostHurtEffects(@NotNull LivingEntity target) {
        super.doPostHurtEffects(target);
        target.addEffect(MilkLib.createRandomPurgeEffect(), this.getEffectSource());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && !this.isInGround()) {
            this.level().addParticle(effectSupplier.get(),
                    this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0);
        }
    }
}
