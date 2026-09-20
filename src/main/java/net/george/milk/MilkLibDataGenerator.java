package net.george.milk;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalFluidTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.george.milk.api.DrippableFluidManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.BrewingRecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class MilkLibDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        AtomicReference<BlockTagsProvider> blockTags = new AtomicReference<>();
        pack.addProvider((output, registriesFuture) -> {
            blockTags.set(new BlockTagsProvider(output, registriesFuture));
            return blockTags.get();
        });
        pack.addProvider((output, registriesFuture) ->
                new ItemTagsProvider(output, registriesFuture, blockTags.get())
        );
        pack.addProvider(FluidTagsProvider::new);
        pack.addProvider(EntityTypeTagsProvider::new);
        pack.addProvider(LootTableProvider::new);
        pack.addProvider(MilkLibRecipeProvider::new);
        pack.addProvider((output, ignored) ->
                DrippableFluidManager.getInstance().createProvider(MilkLib.MOD_ID, output)
        );
    }

    static class BlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
        public BlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(@NotNull HolderLookup.Provider registries) {
            this.builder(MilkLibTags.MILK_PLACEMENT_DISALLOWED);
            this.builder(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(MilkLibKeys.MILK_CAULDRON);
        }
    }

    static class ItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {
        public ItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, @Nullable BlockTagsProvider blockTagsProvider) {
            super(output, registriesFuture, blockTagsProvider);
        }

        @Override
        protected void addTags(@NotNull HolderLookup.Provider registries) {
            this.builder(MilkLibTags.MILK_BOTTLES)
                    .add(MilkLibKeys.MILK_BOTTLE)
                    .add(MilkLibKeys.SPLASH_MILK_BOTTLE)
                    .add(MilkLibKeys.LINGERING_MILK_BOTTLE);
            this.builder(ConventionalItemTags.MILK_DRINKS)
                    .addOptionalTag(MilkLibTags.MILK_BOTTLES);
            this.builder(ItemTags.ARROWS)
                    .add(MilkLibKeys.MILK_ARROW);
        }
    }

    static class FluidTagsProvider extends FabricTagsProvider.FluidTagsProvider {
        public FluidTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(@NotNull HolderLookup.Provider registries) {
            this.builder(MilkLibTags.MILK)
                    .add(MilkLibKeys.STILL_MILK)
                    .add(MilkLibKeys.FLOWING_MILK);
            this.builder(ConventionalFluidTags.MILK)
                    .addOptionalTag(MilkLibTags.MILK);
            this.builder(FluidTags.WATER)
                    .addOptionalTag(MilkLibTags.MILK);
        }
    }

    static class EntityTypeTagsProvider extends FabricTagsProvider.EntityTypeTagsProvider {
        public EntityTypeTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(@NotNull HolderLookup.Provider registries) {
            this.builder(EntityTypeTags.ARROWS)
                    .add(MilkLibKeys.MILK_ARROW_ENTITY_TYPE);
        }
    }

    static class LootTableProvider extends FabricBlockLootSubProvider {
        protected LootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(packOutput, registriesFuture);
        }

        @Override
        public void generate() {
            this.dropOther(MilkLib.MILK_CAULDRON, Blocks.CAULDRON);
        }
    }

    static class MilkLibRecipeProvider extends FabricRecipeProvider {
        public MilkLibRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @NotNull
        @Override
        protected RecipeProvider createRecipeProvider(@NotNull HolderLookup.Provider registries,
                                                      @NotNull BootstrapContext<Recipe<?>> recipes,
                                                      @NotNull BootstrapContext<Advancement> advancements) {
            return new RecipeProvider(recipes, advancements) {
                @Override
                public void buildRecipes() {
                    this.shaped(RecipeCategory.MISC, MilkLib.MILK_ARROW, 8)
                            .define('A', Items.ARROW)
                            .define('L', MilkLib.LINGERING_MILK_BOTTLE)
                            .pattern("AAA")
                            .pattern("ALA")
                            .pattern("AAA")
                            .unlockedBy(getHasName(Items.ARROW), has(Items.ARROW))
                            .save(this.output);
                    BrewingRecipeBuilder.brewingContainerTransform(
                            MilkLib.MILK_BOTTLE,
                            MilkLib.RANDOM_PURGE_POTION,
                            Items.GUNPOWDER,
                            MilkLib.SPLASH_MILK_BOTTLE
                    ).save(this.output);
                    BrewingRecipeBuilder.brewingContainerTransform(
                            MilkLib.SPLASH_MILK_BOTTLE,
                            MilkLib.RANDOM_PURGE_POTION,
                            Items.DRAGON_BREATH,
                            MilkLib.LINGERING_MILK_BOTTLE
                    ).save(this.output);
                }
            };
        }

        @NotNull
        @Override
        public String getName() {
            return "Milk Lib Recipe Provider";
        }
    }
}
