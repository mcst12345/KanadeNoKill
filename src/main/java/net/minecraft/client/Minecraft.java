package net.minecraft.client;

import com.google.common.collect.Queues;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.FutureTask;

public class Minecraft implements ISnooperInfo {
    public static boolean dead;
    public Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();
    public Entity pointedEntity;
    public static Logger LOGGER;
    public EntityPlayerSP PLAYER;
    public final Snooper usageSnooper = new Snooper("client", this, MinecraftServer.getCurrentTimeMillis());
    public WorldClient WORLD;
    public boolean skipRenderWorld;
    public Framebuffer framebuffer;
    public GuiScreen field_71462_r;
    public SoundHandler soundHandler;
    public EntityRenderer entityRenderer;
    public Profiler profiler;
    public Timer timer;
    public FontRenderer fontRenderer;
    public int leftClickCounter;
    public boolean isGamePaused;
    public int displayWidth;
    public int displayHeight;
    public int startNanoTime;
    public FrameTimer frameTimer;
    public int rightClickDelayTimer;
    public GuiToast toastGui;

    public Minecraft() {
    }

    public Minecraft(GameConfiguration gameconfiguration) {
    }

    public static Minecraft getMinecraft() {
        return null;
    }

    public static long getSystemTime() {
        return 0;
    }

    public static void stopIntegratedServer() {
    }

    public void displayGuiScreen(GuiScreen guiScreenIn) {
    }
    public FontRenderer standardGalacticFontRenderer;

    public void SetIngameNotInFocus() {
    }

    @Nullable
    public Entity getRenderViewEntity() {
        return null;
    }

    public void checkWindowResize() {
    }

    public void run() {
    }

    public void updateDisplay() {
    }

    public void runGameLoop() {
    }

    public void shutdown() {

    }

    public void runTick() {
    }

    public int getLimitFramerate() {
        return 0;
    }

    @Override
    public void addServerStatsToSnooper(Snooper playerSnooper) {

    }

    @Override
    public void addServerTypeToSnooper(Snooper playerSnooper) {

    }

    @Override
    public boolean isSnooperEnabled() {
        return false;
    }

    public boolean isIntegratedServerRunning() {
        return true;
    }

    public Session getSession() {
        return new Session("", "", "", "");
    }

    public void loadWorld(@Nullable WorldClient worldClient) {
    }

    public void runTickMouse() {

    }

    public void runTickKeyboard() {

    }

    public boolean isFramerateLimitBelowMax() {
        return true;
    }

    public float getTickLength() {
        return 0.0f;
    }
}
