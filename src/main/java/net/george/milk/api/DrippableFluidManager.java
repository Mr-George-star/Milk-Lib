package net.george.milk.api;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.george.milk.MilkLib;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface DrippableFluidManager {
    List<Identifier> DRIP_HANG = ImmutableList.of(Identifier.withDefaultNamespace("drip_hang"));
    List<Identifier> DRIP_FALL = ImmutableList.of(Identifier.withDefaultNamespace("drip_fall"));
    List<Identifier> SPLASH = ImmutableList.of(
            MilkLib.id("splash_0"),
            MilkLib.id("splash_1"),
            MilkLib.id("splash_2"),
            MilkLib.id("splash_3")
    );

    static DrippableFluidManager getInstance() {
        return DrippableFluidManagerImpl.INSTANCE;
    }

    @Environment(EnvType.CLIENT)
    static void registerClient(ParticleTypeSet particles, DrippableFluid fluid) {
        ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
        registry.register(particles.hang(), provider ->
                new ParticleFactories.DrippingDripstoneFluidFactory(fluid, provider)
        );
        registry.register(particles.fall(), provider ->
                new ParticleFactories.FallingDripstoneFluidFactory(fluid, provider)
        );
        registry.register(particles.splash(), provider ->
                new ParticleFactories.DripstoneFluidSplashFactory(provider, fluid)
        );
    }

    ParticleTypeSet getSet(DrippableFluid fluid);

    Register get(String modId);

    DataProvider createProvider(String modId, PackOutput output);

    interface Register {
        ParticleTypeSet register(String name, DrippableFluid fluid);
    }
}
