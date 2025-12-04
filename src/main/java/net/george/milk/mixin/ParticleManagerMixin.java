package net.george.milk.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.api.Constants;
import net.george.milk.api.Shenanigans;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Redirect(method = "method_45766",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private void milkLib$addReloadResults(List<Object> reloadResults, Consumer<Object> consumer) {
        List<Object> modifiableResults = new ArrayList<>(reloadResults);
        Registries.PARTICLE_TYPE.streamEntries().forEach(holder -> {
            Identifier id = holder.registryKey().getValue();
            String path = id.getPath();
            List<Identifier> sprites = null;
            if (path.endsWith("_milk_lib_particle_type_hang")) {
                sprites = Constants.DRIP_HANG;
            } else if (path.endsWith("_milk_lib_particle_type_fall")) {
                sprites = Constants.DRIP_FALL;
            } else if (path.endsWith("_milk_lib_particle_type_splash")) {
                sprites = Constants.SPLASH;
            }
            if (sprites != null) {
                modifiableResults.add(Shenanigans.createReloadResult(id, sprites));
            }
        });
        modifiableResults.forEach(consumer);
    }
}
