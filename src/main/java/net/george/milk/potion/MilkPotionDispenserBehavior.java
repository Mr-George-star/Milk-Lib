package net.george.milk.potion;

import net.george.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

@SuppressWarnings("resource")
public enum MilkPotionDispenserBehavior implements DispenserBehavior {
	INSTANCE;

	@Override
	public ItemStack dispense(BlockPointer blockPointer, ItemStack itemStack) {
		return (new ItemDispenserBehavior() {
			@Override
			public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				World world = pointer.world();
				Direction direction = pointer.state().get(DispenserBlock.FACING);
				Position position = DispenserBlock.getOutputLocation(pointer);
				float power = 0.88F;
				float uncertainty = 3F;
				ProjectileEntity projectileEntity = Util.make(new PotionEntity(world, position.getX(), position.getY(), position.getZ()), entity -> {
					entity.setItem(stack);
					((PotionItemEntityExtensions) entity).setMilk(true);
				});
				projectileEntity.setVelocity(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ(), power, uncertainty);
				world.spawnEntity(projectileEntity);
				stack.decrement(1);
				return stack;
			}

			@Override
			protected void playSound(BlockPointer pointer) {
				pointer.world().syncWorldEvent(1002, pointer.pos(), 0);
			}
		}).dispense(blockPointer, itemStack);
	}
}
