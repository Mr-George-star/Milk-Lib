package net.george.milk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.george.milk.potion.MilkAreaEffectCloudEntity;
import net.george.milk.potion.MilkPotionDispenserBehavior;
import net.george.milk.potion.bottle.LingeringMilkBottle;
import net.george.milk.potion.bottle.MilkBottle;
import net.george.milk.potion.bottle.SplashMilkBottle;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.item.Items.*;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class MilkLib implements ModInitializer {
	public static final String MOD_ID = "milk-lib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// fluid registries
	public static FlowableFluid STILL_MILK = Registry.register(Registries.FLUID, id("still_milk"), new MilkFluid.Still());
	public static FlowableFluid FLOWING_MILK = Registry.register(Registries.FLUID, id("flowing_milk"), new MilkFluid.Flowing());

	// block registries
	public static Block MILK_FLUID_BLOCK = Registry.register(Registries.BLOCK, id("milk_fluid_block"),
			new FluidBlock(STILL_MILK, AbstractBlock.Settings.copy(Blocks.WATER).mapColor(MapColor.WHITE)));
	public static Block MILK_CAULDRON = Registry.register(Registries.BLOCK, id("milk_cauldron"),
			new MilkCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON)));

	// item registries
	public static Item MILK_BOTTLE = Registry.register(Registries.ITEM, id("milk_bottle"),
			new MilkBottle(new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE).maxCount(1)));
	public static Item SPLASH_MILK_BOTTLE = Registry.register(Registries.ITEM, id("splash_milk_bottle"),
			new SplashMilkBottle(new Item.Settings().maxCount(1)));
	public static Item LINGERING_MILK_BOTTLE = Registry.register(Registries.ITEM, id("lingering_milk_bottle"),
			new LingeringMilkBottle(new Item.Settings().maxCount(1)));

	// entity registries
	public static EntityType<MilkAreaEffectCloudEntity> MILK_EFFECT_CLOUD_ENTITY_TYPE = Registry.register(
			Registries.ENTITY_TYPE,
			id("milk_area_effect_cloud"),
			EntityType.Builder.<MilkAreaEffectCloudEntity>create(SpawnGroup.MISC)
					.makeFireImmune()
					.dimensions(6.0F, 0.5F)
					.trackingTickInterval(10)
					.build()
	);

	// extra conventional tag key for milk bottles
	public static final TagKey<Item> MILK_BOTTLES = TagRegistration.ITEM_TAG.registerC("milk_bottle");

	@Override
	public void onInitialize() {
		FluidStorage.combinedItemApiProvider(MILK_BUCKET).register(context ->
				new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(STILL_MILK), FluidConstants.BUCKET));
		FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
				new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(MILK_BUCKET), STILL_MILK, FluidConstants.BUCKET));
		FluidStorage.combinedItemApiProvider(MILK_BOTTLE).register(context ->
				new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE));
		FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
				new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(MILK_BOTTLE), STILL_MILK, FluidConstants.BOTTLE));
		FluidStorage.combinedItemApiProvider(SPLASH_MILK_BOTTLE).register(context ->
				new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE));
		FluidStorage.combinedItemApiProvider(LINGERING_MILK_BOTTLE).register(context ->
				new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE));

		CauldronFluidContent.registerCauldron(MILK_CAULDRON, STILL_MILK, FluidConstants.BOTTLE, LeveledCauldronBlock.LEVEL);
		// fill into empty behaviours
		CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.map().put(MILK_BUCKET, MilkCauldronBlock.FILL_FROM_BUCKET);
		CauldronBehavior fillFromMilkBottle = MilkCauldronBlock.addInputToCauldronExchange(
				MILK_BOTTLE.getDefaultStack(), Items.GLASS_BOTTLE.getDefaultStack(), true);
		CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.map().put(MILK_BOTTLE, fillFromMilkBottle);
		// milk cauldron behaviors
		MilkCauldronBlock.MILK_CAULDRON_BEHAVIOR.map().put(MILK_BOTTLE, fillFromMilkBottle);
		CauldronBehavior emptyToBottle = MilkCauldronBlock.addOutputToItemExchange(
				Items.GLASS_BOTTLE.getDefaultStack(), MILK_BOTTLE.getDefaultStack(), true);
		MilkCauldronBlock.MILK_CAULDRON_BEHAVIOR.map().put(Items.GLASS_BOTTLE, emptyToBottle);

		DispenserBlock.registerBehavior(SPLASH_MILK_BOTTLE, MilkPotionDispenserBehavior.INSTANCE);
		DispenserBlock.registerBehavior(LINGERING_MILK_BOTTLE, MilkPotionDispenserBehavior.INSTANCE);

		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			builder.registerItemRecipe(MILK_BOTTLE, GUNPOWDER, SPLASH_MILK_BOTTLE);
			builder.registerItemRecipe(SPLASH_MILK_BOTTLE, DRAGON_BREATH, LINGERING_MILK_BOTTLE);
		});
	}

	public static boolean isMilk(BlockState state) {
		return isMilk(state.getFluidState());
	}

	public static boolean isMilk(FluidState state) {
		return (STILL_MILK != null && state.isOf(STILL_MILK)) || (FLOWING_MILK != null && state.isOf(FLOWING_MILK));
	}

	public static boolean isMilkBottle(Item item) {
		return item == MILK_BOTTLE || item == SPLASH_MILK_BOTTLE || item == LINGERING_MILK_BOTTLE;
	}

	public static boolean tryRemoveRandomEffect(LivingEntity user) {
		if (!user.getStatusEffects().isEmpty()) {
			int indexOfEffectToRemove = user.getWorld().random.nextInt(user.getStatusEffects().size());
			StatusEffectInstance effectToRemove = (StatusEffectInstance) user.getStatusEffects().toArray()[indexOfEffectToRemove];
			user.removeStatusEffect(effectToRemove.getEffectType());
			return true;
		}
		return false;
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}
}