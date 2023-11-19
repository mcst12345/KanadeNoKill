package net.minecraft.client;

import kanade.kill.util.GuiDeath;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;

public class Minecraft {
    public EntityPlayerSP PLAYER;
    public WorldClient WORLD;
    public boolean skipRenderWorld;
    public GuiScreen field_71462_r;

    public static Minecraft getMinecraft() {
        return null;
    }

    public void displayGuiScreen(GuiScreen guiScreenIn) {
        if (field_71462_r instanceof GuiDeath) {
            if (!((GuiDeath) field_71462_r).close) {
                return;
            }
        }

        if (guiScreenIn == null && this.WORLD == null) {
            guiScreenIn = new GuiMainMenu();
        }
    }

    public void loadWorld(WorldClient worldClient) {
    }

    public void SetIngameNotInFocus() {
    }
}
