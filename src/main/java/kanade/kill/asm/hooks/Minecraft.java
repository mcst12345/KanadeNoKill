package kanade.kill.asm.hooks;

import kanade.kill.item.KillItem;
import kanade.kill.timemanagement.TimeStop;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.opengl.Display;

import static net.minecraft.client.Minecraft.*;

public class Minecraft {
    public static void runTick(net.minecraft.client.Minecraft minecraft) {
        if (TimeStop.isTimeStop()) {
            if (minecraft.PLAYER != null) {
                if (KillItem.inList(minecraft.PLAYER)) {
                    //minecraft.PLAYER.onUpdate();
                }
            }
        }
    }

    public static void RunGameLoop(net.minecraft.client.Minecraft mc) {
        if (dead || kanade.kill.util.Util.killing) {
            return;
        }
        boolean stop = TimeStop.isTimeStop();
        long i = System.nanoTime();
        mc.Profiler.startSection("root");

        if (Display.isCreated() && Display.isCloseRequested()) {
            mc.shutdown();
        }

        if (!stop) mc.timer.updateTimer();
        mc.Profiler.startSection("scheduledExecutables");

        synchronized (mc.scheduledTasks) {
            while (!mc.scheduledTasks.isEmpty()) {
                try {
                    Util.runTask(mc.scheduledTasks.poll(), LOGGER);
                } catch (Throwable ignored) {
                }
            }
        }

        mc.Profiler.endSection();
        long l = System.nanoTime();
        mc.Profiler.startSection("tick");

        for (int j = 0; j < Math.min(10, mc.timer.elapsedTicks); ++j) {
            mc.runTick();
        }

        mc.Profiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        mc.Profiler.endStartSection("sound");
        mc.soundHandler.setListener(mc.getRenderViewEntity(), mc.timer.renderPartialTicks); //Forge: MC-46445 Spectator mode particles and sounds computed from where you have been before
        mc.Profiler.endSection();
        mc.Profiler.startSection("render");
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        mc.framebuffer.bindFramebuffer(true);
        mc.Profiler.startSection("display");
        GlStateManager.enableTexture2D();
        mc.Profiler.endSection();

        if (!mc.skipRenderWorld) {
            if (!stop) try {
                FMLCommonHandler.instance().onRenderTickStart(mc.timer.renderPartialTicks);
            } catch (Throwable ignored) {
            }
            mc.Profiler.endStartSection("gameRenderer");
            mc.EntityRenderer.updateCameraAndRender((mc.isGamePaused || stop) ? mc.renderPartialTicksPaused : mc.timer.renderPartialTicks, i);
            mc.Profiler.endStartSection("toasts");
            mc.toastGui.drawToast(new ScaledResolution(mc));
            mc.Profiler.endSection();
            if (!stop) try {
                FMLCommonHandler.instance().onRenderTickEnd(mc.timer.renderPartialTicks);
            } catch (Throwable ignored) {
            }
        }

        mc.Profiler.endSection();

        if (!stop) {
            if (mc.gameSettings.showDebugInfo && mc.gameSettings.showDebugProfilerChart && !mc.gameSettings.hideGUI) {
                if (!mc.Profiler.profilingEnabled) {
                    mc.Profiler.clearProfiling();
                }

                mc.Profiler.profilingEnabled = true;
                mc.displayDebugInfo(i1);
            } else {
                mc.Profiler.profilingEnabled = false;
                mc.prevFrameTime = System.nanoTime();
            }
        }

        mc.framebuffer.unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        mc.framebuffer.framebufferRender(mc.displayWidth, mc.displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        mc.EntityRenderer.renderStreamIndicator(mc.timer.renderPartialTicks);
        GlStateManager.popMatrix();
        mc.Profiler.startSection("root");
        mc.updateDisplay();
        Thread.yield();
        ++mc.fpsCounter;
        boolean flag = mc.isSingleplayer() && mc.CurrentScreen != null && mc.CurrentScreen.doesGuiPauseGame() && !mc.integratedServer.getPublic();

        if (!stop) {
            if (mc.isGamePaused != flag) {
                if (mc.isGamePaused) {
                    mc.renderPartialTicksPaused = mc.timer.renderPartialTicks;
                } else {
                    mc.timer.renderPartialTicks = mc.renderPartialTicksPaused;
                }

                mc.isGamePaused = flag;
            }
        }

        long k = System.nanoTime();
        mc.frameTimer.addFrame(k - mc.startNanoTime);
        mc.startNanoTime = k;

        if (!stop) {
            while (getSystemTime() >= mc.debugUpdateTime + 1000L) {
                debugFPS = mc.fpsCounter;
                mc.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated == 1 ? "" : "s", (float) mc.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : mc.gameSettings.limitFramerate, mc.gameSettings.enableVsync ? " vsync" : "", mc.gameSettings.fancyGraphics ? "" : " fast", mc.gameSettings.clouds == 0 ? "" : (mc.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
                RenderChunk.renderChunksUpdated = 0;
                mc.debugUpdateTime += 1000L;
                mc.fpsCounter = 0;
                mc.usageSnooper.addMemoryStatsToSnooper();

                if (!mc.usageSnooper.isSnooperRunning()) {
                    mc.usageSnooper.startSnooper();
                }
            }
        }

        if (mc.isFramerateLimitBelowMax()) {
            mc.Profiler.startSection("fpslimit_wait");
            Display.sync(mc.getLimitFramerate());
            mc.Profiler.endSection();
        }

        mc.Profiler.endSection();
    }
}
