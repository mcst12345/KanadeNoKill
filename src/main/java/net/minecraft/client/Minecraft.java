package net.minecraft.client;

import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import kanade.kill.util.Util;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Session;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.net.Proxy;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class Minecraft {
    public static boolean dead;
    public static Logger LOGGER;
    public final Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();
    public Entity pointedEntity;
    public EntityClientPlayerMP PLAYER;
    public WorldClient WORLD;
    public boolean skipRenderWorld;
    public FontRenderer fontRenderer;
    public int leftClickCounter;
    public boolean isGamePaused;
    public int displayWidth;
    public int displayHeight;
    public int startNanoTime;
    public int rightClickDelayTimer;
    public FontRenderer standardGalacticFontRenderer;

    public Minecraft() {
    }

    public Minecraft(Session p_i1103_1_, int p_i1103_2_, int p_i1103_3_, boolean p_i1103_4_, boolean p_i1103_5_, File p_i1103_6_, File p_i1103_7_, File p_i1103_8_, Proxy p_i1103_9_, String p_i1103_10_, Multimap p_i1103_11_, String p_i1103_12_) {

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

    public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule) {
        if (Util.FromModClass(callableToSchedule)) {
            return Futures.immediateFuture(null);
        }
        Validate.notNull(callableToSchedule);

        if (this.isCallingFromMinecraftThread()) {
            try {
                return Futures.immediateFuture(callableToSchedule.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        } else {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callableToSchedule);

            synchronized (this.scheduledTasks) {
                this.scheduledTasks.add(listenablefuturetask);
                return listenablefuturetask;
            }
        }
    }

    public boolean isCallingFromMinecraftThread() {
        return true;
    }

    public IResourceManager getResourceManager() {
        return null;
    }

    public void setServer(String s6, int i) {

    }
}
