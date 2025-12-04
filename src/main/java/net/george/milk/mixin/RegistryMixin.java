package net.george.milk.mixin;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.george.milk.api.Constants;
import net.george.milk.api.DripstoneInteractingFluid;
import net.george.milk.api.ParticleTypeSet;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public interface RegistryMixin {
    @Inject(at = @At("HEAD"), method = "register(Lnet/minecraft/registry/Registry;Lnet/minecraft/util/Identifier;Ljava/lang/Object;)Ljava/lang/Object;")
    private static <V, T extends V> void milkLib$register(Registry<V> registry, Identifier id, T entry, CallbackInfoReturnable<T> cir) {
        if (entry instanceof DripstoneInteractingFluid interactingFluid) {
            SimpleParticleType hang = Registry.register(Registries.PARTICLE_TYPE,
                    Identifier.of(id.getNamespace(), id.getPath() + "_milk_lib_particle_type_hang"),
                    FabricParticleTypes.simple());
            SimpleParticleType fall = Registry.register(Registries.PARTICLE_TYPE,
                    Identifier.of(id.getNamespace(), id.getPath() + "_milk_lib_particle_type_fall"),
                    FabricParticleTypes.simple());
            SimpleParticleType splash = Registry.register(Registries.PARTICLE_TYPE,
                    Identifier.of(id.getNamespace(), id.getPath() + "_milk_lib_particle_type_splash"),
                    FabricParticleTypes.simple());
            Constants.FLUIDS_TO_PARTICLES.put(interactingFluid, new ParticleTypeSet(hang, fall, splash));
        }
    }
}
