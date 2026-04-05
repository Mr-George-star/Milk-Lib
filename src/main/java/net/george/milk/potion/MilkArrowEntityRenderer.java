package net.george.milk.potion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.george.milk.MilkLib;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MilkArrowEntityRenderer extends ProjectileEntityRenderer<MilkArrowEntity, ProjectileEntityRenderState> {
    public static final Identifier TEXTURE = MilkLib.id("textures/entity/projectiles/milk_arrow.png");

    public MilkArrowEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTexture(ProjectileEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public ProjectileEntityRenderState createRenderState() {
        return new ProjectileEntityRenderState();
    }
}
