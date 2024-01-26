package net.minecraft.client;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import kanade.kill.util.ObjectUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveFormat;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class Minecraft implements ISnooperInfo {
    public static boolean dead;
    public final Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();
    public Entity pointedEntity;
    public static Logger LOGGER;
    public EntityPlayerSP PLAYER;
    public final Snooper usageSnooper = new Snooper("client", this, MinecraftServer.getCurrentTimeMillis());
    public WorldClient WORLD;
    public boolean skipRenderWorld;
    public Framebuffer framebuffer;
    public GuiScreen field_71462_r;
    public SoundHandler soundHandler;
    public static int debugFPS;
    public EntityRenderer EntityRenderer;
    public Timer timer;
    public FontRenderer fontRenderer;
    public int leftClickCounter;
    public boolean isGamePaused;
    public int displayWidth;
    public int displayHeight;
    public Profiler Profiler;
    public FrameTimer frameTimer;
    public int rightClickDelayTimer;
    public GuiToast toastGui;
    public long startNanoTime;
    public GameSettings gameSettings;
    public long prevFrameTime;
    public int fpsCounter;
    public GuiScreen CurrentScreen;
    public IntegratedServer integratedServer;
    public float renderPartialTicksPaused;
    public long debugUpdateTime;
    public String debug;
    public RenderGlobal renderGlobal;
    public ParticleManager effectRenderer;
    public EntityRenderer entityRenderer;
    public long systemTime;
    public RayTraceResult objectMouseOver;
    public PlayerControllerMP playerController;

    public Minecraft() {
    }

    public Minecraft(GameConfiguration gameconfiguration) {
    }

    public static Minecraft getMinecraft() {
        return null;
    }
    public ISaveFormat saveLoader;

    @Nullable
    public NetHandlerPlayClient getConnection() {
        return null;
    }

    public void launchIntegratedServer(String folderName, String worldName, @Nullable WorldSettings worldSettingsIn) {
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

    public void runGameLoop() throws IOException {
        kanade.kill.asm.hooks.Minecraft.RunGameLoop(this);
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

    public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule) {
        if (ObjectUtil.FromModClass(callableToSchedule)) {
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

    public void displayDebugInfo(long i1) {

    }

    public boolean isSingleplayer() {
        return false;
    }

    public RenderManager getRenderManager() {
        return null;
    }

    public ItemColors getItemColors() {
        return null;
    }

    public RenderItem getRenderItem() {
        return null;
    }

    public TextureManager getTextureManager() {
        return null;
    }

    public Framebuffer getFramebuffer() {
        return null;
    }
}
