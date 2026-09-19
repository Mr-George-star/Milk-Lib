package net.george.milk.potion;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MilkArrowItem extends ArrowItem {
    public MilkArrowItem(Item.Properties settings) {
        super(settings);
    }

    @NotNull
    @Override
    public AbstractArrow createArrow(@NotNull Level world, ItemStack stack, @NotNull LivingEntity shooter, @Nullable ItemStack shotFrom) {
        return new MilkArrowEntity(shooter, world, stack.copyWithCount(1), shotFrom);
    }

    @NotNull
    @Override
    public Projectile asProjectile(@NotNull Level world, Position pos, ItemStack stack, @NotNull Direction direction) {
        MilkArrowEntity arrow = new MilkArrowEntity(pos.x(), pos.y(), pos.z(), world, stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }
}
