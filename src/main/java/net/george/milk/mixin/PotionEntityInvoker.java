package net.george.milk.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractThrownPotion.class)
public interface PotionEntityInvoker {
    @Invoker("douseFire")
    void milkLib$douseFire(final BlockPos pos);
}
