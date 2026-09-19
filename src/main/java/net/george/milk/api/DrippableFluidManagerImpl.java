package net.george.milk.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@NonExtendable
@Internal
public class DrippableFluidManagerImpl implements DrippableFluidManager {
    static final DrippableFluidManagerImpl INSTANCE = new DrippableFluidManagerImpl();
    private final Map<String, RegisterImpl> registers = new ConcurrentHashMap<>();
    private final Map<String, Map<DrippableFluid, RegisteredParticleTypeSet>> registered = new ConcurrentHashMap<>();
    private final Map<DrippableFluid, RegisteredParticleTypeSet> registeredByFluid = new ConcurrentHashMap<>();

    private DrippableFluidManagerImpl() {
    }

    @Override
    public ParticleTypeSet getSet(DrippableFluid fluid) {
        return Optional.ofNullable(this.registeredByFluid.get(fluid))
                .map(RegisteredParticleTypeSet::getDefault)
                .orElseThrow(() ->
                        new IllegalArgumentException("Fluid is not registered: " + fluid));
    }

    @Override
    public Register get(String modId) {
        return this.registers.computeIfAbsent(modId, RegisterImpl::new);
    }

    @Override
    public GenerationProvider createProvider(String modId, PackOutput output) {
        return new GenerationProvider(modId, output);
    }

    public class RegisterImpl implements Register {
        private final String modId;

        RegisterImpl(String modId) {
            this.modId = modId;
        }

        @Override
        public ParticleTypeSet register(String name, DrippableFluid fluid) {
            if (registeredByFluid.containsKey(fluid)) {
                throw new IllegalStateException("Fluid is already registered: " + fluid);
            }
            Identifier hangId = Identifier.fromNamespaceAndPath(this.modId, name + "_hang");
            Identifier fallId = Identifier.fromNamespaceAndPath(this.modId, name + "_fall");
            Identifier splashId = Identifier.fromNamespaceAndPath(this.modId, name + "_splash");

            SimpleParticleType hang = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                    hangId,
                    FabricParticleTypes.simple());
            SimpleParticleType fall = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                    fallId,
                    FabricParticleTypes.simple());
            SimpleParticleType splash = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                    splashId,
                    FabricParticleTypes.simple());
            RegisteredParticleTypeSet set = new RegisteredParticleTypeSet(
                    new RegisteredObject<>(hangId, hang),
                    new RegisteredObject<>(fallId, fall),
                    new RegisteredObject<>(splashId, splash)
            );

            registered.computeIfAbsent(this.modId, ignored -> new HashMap<>()).put(fluid, set);
            registeredByFluid.put(fluid, set);
            return set.getDefault();
        }
    }

    public class GenerationProvider implements DataProvider {
        private final String modId;
        private final PackOutput.PathProvider resolver;

        GenerationProvider(String modId, PackOutput output) {
            this.modId = modId;
            this.resolver = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "particles");
        }

        @NotNull
        @Override
        public CompletableFuture<?> run(@NotNull CachedOutput output) {
            Map<DrippableFluid, RegisteredParticleTypeSet> data = registered.get(this.modId);

            if (data.isEmpty()) {
                return CompletableFuture.runAsync(() -> {});
            } else {
                Map<Identifier, ParticleDataObject> result = new HashMap<>();
                for (RegisteredParticleTypeSet registered : data.values()) {
                    result.putAll(registered.getSprites());
                }
                return DataProvider.saveAll(output, ParticleDataObject.CODEC, this.resolver, result);
            }
        }

        @NotNull
        @Override
        public String getName() {
            return "Drippable Fluid Particle Data Generator [" + this.modId + "]";
        }
    }

    static class ParticleDataObject {
        static final Codec<ParticleDataObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.listOf().fieldOf("textures").forGetter(object -> object.spriteIds)
        ).apply(instance, ParticleDataObject::new));
        private final List<Identifier> spriteIds;

        ParticleDataObject(List<Identifier> spriteIds) {
            this.spriteIds = spriteIds;
        }
    }

    static class RegisteredParticleTypeSet {
        private final RegisteredObject<SimpleParticleType> hang;
        private final RegisteredObject<SimpleParticleType> fall;
        private final RegisteredObject<SimpleParticleType> splash;
        private final ParticleTypeSet defaultSet;

        RegisteredParticleTypeSet(
                RegisteredObject<SimpleParticleType> hang,
                RegisteredObject<SimpleParticleType> fall,
                RegisteredObject<SimpleParticleType> splash) {
            this.hang = hang;
            this.fall = fall;
            this.splash = splash;
            this.defaultSet = new ParticleTypeSet(this.hang.object, this.fall.object, this.splash.object);
        }

        public Map<Identifier, ParticleDataObject> getSprites() {
            Map<Identifier, ParticleDataObject> result = new HashMap<>();
            result.put(this.hang.id, new ParticleDataObject(DrippableFluidManager.DRIP_HANG));
            result.put(this.fall.id, new ParticleDataObject(DrippableFluidManager.DRIP_FALL));
            result.put(this.splash.id, new ParticleDataObject(DrippableFluidManager.SPLASH));
            return result;
        }

        public ParticleTypeSet getDefault() {
            return this.defaultSet;
        }
    }

    record RegisteredObject<T>(Identifier id, T object) {
    }
}
