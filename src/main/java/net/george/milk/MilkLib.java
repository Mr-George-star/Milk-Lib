package net.george.milk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.george.milk.api.DrippableFluidManager;
import net.george.milk.api.ParticleTypeSet;
import net.george.milk.potion.*;
import net.george.milk.potion.bottle.LingeringMilkBottle;
import net.george.milk.potion.bottle.MilkBottle;
import net.george.milk.potion.bottle.SplashMilkBottle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

import static net.minecraft.world.item.Items.*;

@SuppressWarnings("unused")
public class MilkLib implements ModInitializer {
	public static final String MOD_ID = "milk-lib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// fluid registries
	public static MilkFluid STILL_MILK = Registry.register(BuiltInRegistries.FLUID,
			MilkLibKeys.STILL_MILK, new MilkFluid.Still());
	public static MilkFluid FLOWING_MILK = Registry.register(BuiltInRegistries.FLUID,
			MilkLibKeys.FLOWING_MILK, new MilkFluid.Flowing());
	public static ParticleTypeSet STILL_MILK_PARTICLES;
	public static ParticleTypeSet FLOWING_MILK_PARTICLES;

	// block registries
	public static Block MILK_FLUID_BLOCK = registerBlock(MilkLibKeys.MILK_FLUID_BLOCK,
			settings -> new LiquidBlock(STILL_MILK, settings),
			BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.SNOW)
	);
	public static Block MILK_CAULDRON = registerBlock(MilkLibKeys.MILK_CAULDRON,
			MilkCauldronBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON));

	// item registries
	public static Item MILK_BOTTLE = registerItem(MilkLibKeys.MILK_BOTTLE, MilkBottle::new,
			new Item.Properties()
					.craftRemainder(Items.GLASS_BOTTLE)
					.stacksTo(1)
					.component(DataComponents.CONSUMABLE, Consumable.builder()
							.consumeSeconds(1.6F).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK)
							.soundAfterConsume(SoundEvents.GENERIC_DRINK).hasConsumeParticles(false).build()));
	public static Item SPLASH_MILK_BOTTLE = registerItem(MilkLibKeys.SPLASH_MILK_BOTTLE, SplashMilkBottle::new,
			new Item.Properties().stacksTo(1));
	public static Item LINGERING_MILK_BOTTLE = registerItem(MilkLibKeys.LINGERING_MILK_BOTTLE, LingeringMilkBottle::new,
			new Item.Properties().stacksTo(1));
	public static Item MILK_ARROW = registerItem(MilkLibKeys.MILK_ARROW, MilkArrowItem::new, new Item.Properties());

	// entity registries
	public static EntityType<MilkArrowEntity> MILK_ARROW_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			MilkLibKeys.MILK_ARROW_ENTITY_TYPE,
			EntityType.Builder.of(MilkArrowEntity::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.noLootTable()
					.sized(0.5F, 0.5F)
					.eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20)
					.build(MilkLibKeys.MILK_ARROW_ENTITY_TYPE)
	);

	// effect & potion
	public static final Holder.Reference<MobEffect> RANDOM_PURGE = Registry
			.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("random_purge"), new RandomPurgeEffect());
	public static final Holder.Reference<Potion> RANDOM_PURGE_POTION = Registry
			.registerForHolder(BuiltInRegistries.POTION, id("random_purge"), new Potion("random_purge", createRandomPurgeEffect()));

	@Override
	public void onInitialize() {
		MilkLibTags.register();
		registerMilkCauldronInteractions();

		DrippableFluidManager.Register register = DrippableFluidManager.getInstance().get(MOD_ID);
		STILL_MILK_PARTICLES = register.register("still_milk", STILL_MILK);
		FLOWING_MILK_PARTICLES = register.register("flowing_milk", FLOWING_MILK);

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

		CauldronFluidContent.registerCauldron(MILK_CAULDRON, STILL_MILK, FluidConstants.BOTTLE, LayeredCauldronBlock.LEVEL);

		DispenserBlock.registerBehavior(SPLASH_MILK_BOTTLE, MilkPotionDispenserBehavior.SPLASH);
		DispenserBlock.registerBehavior(LINGERING_MILK_BOTTLE, MilkPotionDispenserBehavior.LINGERING);
		DispenserBlock.registerProjectileBehavior(MILK_ARROW);

		/* events */
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.getDisplayStacks().removeIf(stack -> {
				if (!(stack.getItem() instanceof PotionItem)) {
					return false;
				}
				PotionContents component = stack.get(DataComponents.POTION_CONTENTS);
				if (component == null) {
					return false;
				}

				return component.potion()
						.map(entry -> BuiltInRegistries.POTION.getKey(entry.value()))
						.map(id -> id.getNamespace().equals(MOD_ID))
						.orElse(false);
			});
			entries.accept(MILK_BOTTLE);
			entries.accept(SPLASH_MILK_BOTTLE);
			entries.accept(LINGERING_MILK_BOTTLE);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries ->
				entries.accept(MILK_ARROW));
		ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
			if (itemStack.is(MILK_BOTTLE)) {
				list.add(Component.translatable("item.milk-lib.milk_bottle.tooltip").withStyle(ChatFormatting.GRAY));
			} else if (itemStack.is(SPLASH_MILK_BOTTLE)) {
				list.add(Component.translatable("item.milk-lib.splash_milk_bottle.tooltip").withStyle(ChatFormatting.GRAY));
			} else if (itemStack.is(LINGERING_MILK_BOTTLE)) {
				list.add(Component.translatable("item.milk-lib.lingering_milk_bottle.tooltip").withStyle(ChatFormatting.GRAY));
			}
		});
	}

	public static void registerMilkCauldronInteractions() {
		// fill into empty behaviors
		CauldronInteractions.EMPTY.put(MILK_BUCKET, MilkCauldronBlock.FILL_FROM_BUCKET);
		CauldronInteraction fillFromMilkBottle = MilkCauldronBlock.addInputToCauldronExchange(
				MILK_BOTTLE, Items.GLASS_BOTTLE);
		CauldronInteractions.EMPTY.put(MILK_BOTTLE, fillFromMilkBottle);
		// milk cauldron behaviors
		MilkCauldronBlock.addBehavior(fillFromMilkBottle, MILK_BOTTLE);
		CauldronInteraction emptyToBottle = MilkCauldronBlock.addOutputToItemExchange(
				Items.GLASS_BOTTLE, MILK_BOTTLE);
		MilkCauldronBlock.addBehavior(emptyToBottle, Items.GLASS_BOTTLE);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			BuiltInRegistries.ITEM.forEach(item -> {
				if (item instanceof BannerItem) {
					MilkCauldronBlock.addBehavior(MilkCauldronBlock.MILKIFY_BANNER, item);
				} else if (item instanceof BlockItem blockItem) {
					if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
						MilkCauldronBlock.addBehavior(MilkCauldronBlock.MILKIFY_SHULKER_BOX, item);
					}
				}
			});
			MilkCauldronBlock.addBehavior(MilkCauldronBlock.MILKIFY_DYEABLE_ITEM, ItemTags.CAULDRON_CAN_REMOVE_DYE);
			MilkCauldronBlock.addBehavior(MilkCauldronBlock.FILL_FROM_BUCKET, Items.MILK_BUCKET);
			MilkCauldronBlock.addBehavior(MilkCauldronBlock.EMPTY_TO_BUCKET, Items.BUCKET);
		});
	}

	public static boolean isMilk(BlockState state) {
		return isMilk(state.getFluidState());
	}

	public static boolean isMilk(FluidState state) {
		return (STILL_MILK != null && state.is(STILL_MILK)) || (FLOWING_MILK != null && state.is(FLOWING_MILK));
	}

	public static boolean isMilkBottle(Item item) {
		return item == MILK_BOTTLE || item == SPLASH_MILK_BOTTLE || item == LINGERING_MILK_BOTTLE;
	}

	public static boolean tryRemoveRandomEffect(LivingEntity user) {
		if (user.level().isClientSide()) {
			return false;
		}
		List<MobEffectInstance> effects = user.getActiveEffects().stream()
				.filter(instance -> instance.getEffect() != RANDOM_PURGE).toList();
		if (effects.isEmpty()) {
			return false;
		}
		MobEffectInstance effect = effects.get(user.level().getRandom().nextInt(effects.size()));
		return user.removeEffect(effect.getEffect());
	}

	public static MobEffectInstance createRandomPurgeEffect() {
		return new MobEffectInstance(RANDOM_PURGE, 10);
	}

	public static Identifier id(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}

	private static Block registerBlock(ResourceKey<Block> key,
									   Function<BlockBehaviour.Properties, Block> blockFactory,
									   BlockBehaviour.Properties settings) {
		return Registry.register(BuiltInRegistries.BLOCK, key, blockFactory.apply(settings.setId(key)));
	}

	private static Item registerItem(ResourceKey<Item> key,
									 Function<Item.Properties, Item> itemFactory,
									 Item.Properties settings) {
		return Registry.register(BuiltInRegistries.ITEM, key, itemFactory.apply(settings.setId(key)));
	}
}