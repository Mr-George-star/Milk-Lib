package net.george.milk.potion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.MilkLib;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class MilkArrowEntityRenderer extends ArrowRenderer<MilkArrowEntity, ArrowRenderState> {
    public static final Identifier TEXTURE = MilkLib.id("textures/entity/projectiles/milk_arrow.png");

    public MilkArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @NotNull
    @Override
    protected Identifier getTextureLocation(@NotNull ArrowRenderState state) {
        return TEXTURE;
    }

    @NotNull
    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
