package net.george.milk;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import static net.george.milk.MilkLib.id;

public class MilkLibKeys {
    public static final ResourceKey<Fluid> STILL_MILK = ResourceKey.create(Registries.FLUID, id("still_milk"));
    public static final ResourceKey<Fluid> FLOWING_MILK = ResourceKey.create(Registries.FLUID, id("flowing_milk"));

    public static final ResourceKey<Block> MILK_FLUID_BLOCK = ResourceKey.create(Registries.BLOCK, id("milk_fluid_block"));
    public static final ResourceKey<Block> MILK_CAULDRON = ResourceKey.create(Registries.BLOCK, id("milk_cauldron"));

    public static final ResourceKey<Item> MILK_BOTTLE = ResourceKey.create(Registries.ITEM, id("milk_bottle"));
    public static final ResourceKey<Item> SPLASH_MILK_BOTTLE = ResourceKey.create(Registries.ITEM, id("splash_milk_bottle"));
    public static final ResourceKey<Item> LINGERING_MILK_BOTTLE = ResourceKey.create(Registries.ITEM, id("lingering_milk_bottle"));
    public static final ResourceKey<Item> MILK_ARROW = ResourceKey.create(Registries.ITEM, id("milk_arrow"));

    public static final ResourceKey<EntityType<?>> MILK_ARROW_ENTITY_TYPE = ResourceKey.create(
            Registries.ENTITY_TYPE,
            id("milk_arrow")
    );
}
