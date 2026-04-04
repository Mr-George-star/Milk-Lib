package net.george.milk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static net.minecraft.item.Items.*;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class MilkLib implements ModInitializer {
	public static final String MOD_ID = "milk-lib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// tags
	public static final TagKey<Block> MILK_PLACEMENT_DISALLOWED = TagKey.of(RegistryKeys.BLOCK, id("milk_placement_disallowed"));

	// fluid registries
	public static FlowableFluid STILL_MILK = Registry.register(Registries.FLUID, id("still_milk"), new MilkFluid.Still());
	public static FlowableFluid FLOWING_MILK = Registry.register(Registries.FLUID, id("flowing_milk"), new MilkFluid.Flowing());

	// block registries
	public static Block MILK_FLUID_BLOCK = Registry.register(Registries.BLOCK, id("milk_fluid_block"),
			new FluidBlock(STILL_MILK, AbstractBlock.Settings.copy(Blocks.WATER).mapColor(MapColor.WHITE)
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id("milk_fluid_block")))));
	public static Block MILK_CAULDRON = Registry.register(Registries.BLOCK, id("milk_cauldron"),
			new MilkCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON)
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id("milk_cauldron")))));

	// item registries
	public static Item MILK_BOTTLE = registerItem("milk_bottle", MilkBottle::new,
			new Item.Settings()
					.recipeRemainder(Items.GLASS_BOTTLE)
					.maxCount(1)
					.component(DataComponentTypes.CONSUMABLE, ConsumableComponent.builder()
							.consumeSeconds(1.6F).useAction(UseAction.DRINK).sound(SoundEvents.ENTITY_GENERIC_DRINK)
							.finishSound(SoundEvents.ENTITY_GENERIC_DRINK).consumeParticles(false).build()));
	public static Item SPLASH_MILK_BOTTLE = registerItem("splash_milk_bottle", SplashMilkBottle::new,
			new Item.Settings().maxCount(1));
	public static Item LINGERING_MILK_BOTTLE = registerItem("lingering_milk_bottle", LingeringMilkBottle::new,
			new Item.Settings().maxCount(1));

	// entity registries
	public static EntityType<MilkAreaEffectCloudEntity> MILK_EFFECT_CLOUD_ENTITY_TYPE = Registry.register(
			Registries.ENTITY_TYPE,
			id("milk_area_effect_cloud"),
			EntityType.Builder.<MilkAreaEffectCloudEntity>create(SpawnGroup.MISC)
					.makeFireImmune()
					.dimensions(6.0F, 0.5F)
					.trackingTickInterval(10)
					.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id("milk_area_effect_cloud")))
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
		// fill into empty behaviors
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
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
			entries.add(MILK_BOTTLE);
			entries.add(SPLASH_MILK_BOTTLE);
			entries.add(LINGERING_MILK_BOTTLE);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Registries.ITEM.forEach(item -> {
				if (item.getDefaultStack().isIn(ItemTags.DYEABLE)) {
					LOGGER.info(String.valueOf(Registries.ITEM.getId(item)));
					MilkCauldronBlock.addBehavior(MilkCauldronBlock.MILKIFY_DYEABLE_ITEM, item);
				} else if (item instanceof BannerItem) {
					MilkCauldronBlock.addBehavior(MilkCauldronBlock.MILKIFY_BANNER, item);
				} else if (item instanceof BlockItem blockItem) {
					if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
						MilkCauldronBlock.addBehavior(MilkCauldronBlock.MILKIFY_SHULKER_BOX, item);
					}
				}
			});
			MilkCauldronBlock.addBehavior(MilkCauldronBlock.FILL_FROM_BUCKET, Items.MILK_BUCKET);
			MilkCauldronBlock.addBehavior(MilkCauldronBlock.EMPTY_TO_BUCKET, Items.BUCKET);
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

	private static Item registerItem(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
		Identifier id = id(name);
		return Registry.register(Registries.ITEM, id, itemFactory.apply(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, id))));
	}
}