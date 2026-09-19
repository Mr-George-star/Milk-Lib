package net.george.milk;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import static net.george.milk.MilkLib.id;

@SuppressWarnings("UnstableApiUsage")
public class MilkLibTags {
    public static final TagKey<Block> MILK_PLACEMENT_DISALLOWED = TagKey
            .create(Registries.BLOCK, id("milk_placement_disallowed"));
    public static final TagKey<Fluid> MILK = TagKey
            .create(Registries.FLUID, id("milk"));

    // extra conventional tag key for milk bottles
    public static final TagKey<Item> MILK_BOTTLES = TagRegistration.ITEM_TAG.registerC("milk_bottle");

    static void register() {
    }
}
