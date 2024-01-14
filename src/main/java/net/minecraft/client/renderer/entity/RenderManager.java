package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

@SideOnly(Side.CLIENT)
public class RenderManager {

    public Map<Class<? extends Entity>, Render<? extends Entity>> EntityRenderMap = Maps.newHashMap();
}
