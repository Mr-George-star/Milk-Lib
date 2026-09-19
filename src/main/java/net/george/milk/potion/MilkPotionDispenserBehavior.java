package net.george.milk.potion;

import com.mojang.datafixers.util.Function5;
import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.util.Util;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("resource")
public enum MilkPotionDispenserBehavior implements DispenseItemBehavior {
	SPLASH(ThrownSplashPotion::new),
	LINGERING(ThrownLingeringPotion::new);

	final Function5<Level, Double, Double, Double, ItemStack, AbstractThrownPotion> potionFactory;

	MilkPotionDispenserBehavior(Function5<Level, Double, Double, Double, ItemStack, AbstractThrownPotion> potionFactory) {
		this.potionFactory = potionFactory;
	}

	@NotNull
	@Override
	public ItemStack dispense(@NotNull BlockSource blockPointer, @NotNull ItemStack itemStack) {
		return (new DefaultDispenseItemBehavior() {
			@NotNull
			@Override
			public ItemStack execute(@NotNull BlockSource pointer, @NotNull ItemStack stack) {
				Level world = pointer.level();
				Direction direction = pointer.state().getValue(DispenserBlock.FACING);
				Position position = DispenserBlock.getDispensePosition(pointer);
				float power = 0.88F;
				float uncertainty = 3F;
				Projectile projectileEntity = Util.make(potionFactory.apply(world, position.x(), position.y(), position.z(), stack), entity -> {
					entity.setItem(stack);
					((PotionItemEntityExtensions) entity).setMilk(true);
				});
				projectileEntity.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), power, uncertainty);
				world.addFreshEntity(projectileEntity);
				stack.shrink(1);
				return stack;
			}

			@Override
			protected void playSound(@NotNull BlockSource pointer) {
				pointer.level().levelEvent(1002, pointer.pos(), 0);
			}
		}).dispense(blockPointer, itemStack);
	}
}
