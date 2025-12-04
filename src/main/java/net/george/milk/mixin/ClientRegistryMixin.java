package net.george.milk.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.api.Constants;
import net.george.milk.api.DripstoneInteractingFluid;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Registry.class)
public interface ClientRegistryMixin {
    @Inject(at = @At("RETURN"), method = "register(Lnet/minecraft/registry/Registry;Lnet/minecraft/util/Identifier;Ljava/lang/Object;)Ljava/lang/Object;")
    private static <V, T extends V> void milkLib$clientRegister(Registry<V> registry, Identifier id, T entry, CallbackInfoReturnable<T> cir) {
        if (entry instanceof DripstoneInteractingFluid interactingFluid) {
            Constants.TO_REGISTER.add(interactingFluid);
        }
    }
}
