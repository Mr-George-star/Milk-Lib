package net.george.milk.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;

public interface DrippableFluidManager {
    static DrippableFluidManager getInstance() {
        return DrippableFluidManagerImpl.INSTANCE;
    }

    @Environment(EnvType.CLIENT)
    static void registerClient(ParticleTypeSet particles, DripstoneInteractingFluid fluid) {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
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

    ParticleTypeSet getSet(DripstoneInteractingFluid fluid);

    Register get(String modId);

    DataProvider createProvider(String modId, DataOutput output);

    interface Register {
        ParticleTypeSet register(String name, DripstoneInteractingFluid fluid);
    }
}
