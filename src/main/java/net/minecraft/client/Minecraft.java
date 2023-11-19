package net.minecraft.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;

public class Minecraft {
    public EntityPlayerSP PLAYER;
    public WorldClient WORLD;
    public boolean skipRenderWorld;

    public static Minecraft getMinecraft() {
        return null;
    }

    public void displayGuiScreen(GuiScreen guiScreen) {

    }

    public void loadWorld(WorldClient worldClient) {
    }

    public void SetIngameNotInFocus() {
    }
}
