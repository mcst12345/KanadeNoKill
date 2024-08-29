package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class RenderManager {
    public double renderPosX;
    public double renderPosY;
    public double renderPosZ;

    public Map<Class<? extends Entity>, Render<? extends Entity>> EntityRenderMap = Maps.newHashMap();

    public Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Maps.newHashMap();
    public TextureManager renderEngine;
    public Entity renderViewEntity;

    public void setWorld(WorldClient worldClientIn) {

    }

    public void cacheActiveRenderInfo(WorldClient world, FontRenderer fontRenderer, Entity renderViewEntity, Entity pointedEntity, GameSettings gameSettings, float partialTicks) {

    }

    public void setRenderPosition(double d3, double d4, double d5) {

    }

    public void renderEntityStatic(Entity entity1, float partialTicks, boolean b) {

    }

    public boolean shouldRender(Entity entity2, ICamera camera, double d0, double d1, double d2) {
        return true;
    }

    public boolean isRenderMultipass(Entity entity2) {
        return true;
    }

    public void renderMultipass(Entity entity3, float partialTicks) {
    }

    public void setRenderOutlines(boolean b) {
    }

    @Nullable
    public <T extends Entity> Render<T> getEntityRenderObject(Entity entityIn) {
        return null;
    }
}
