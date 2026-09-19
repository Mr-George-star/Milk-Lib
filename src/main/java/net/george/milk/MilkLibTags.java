package net.george.milk;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import static net.george.milk.MilkLib.id;

public class MilkLibTags {
    public static final TagKey<Block> MILK_PLACEMENT_DISALLOWED = TagKey
            .create(Registries.BLOCK, id("milk_placement_disallowed"));
    public static final TagKey<Fluid> MILK = TagKey
            .create(Registries.FLUID, id("milk"));

    static void register() {
    }
}
