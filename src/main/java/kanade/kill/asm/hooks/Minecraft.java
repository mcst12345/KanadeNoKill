package kanade.kill.asm.hooks;

import kanade.kill.Core;
import kanade.kill.Keys;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.*;
import kanade.kill.timemanagement.TimeStop;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static kanade.kill.item.KillItem.mode;
import static net.minecraft.client.Minecraft.*;

public class Minecraft {
    public static int tickCount = 0;
    public static final ResourceLocation BlackAndWhite = new ResourceLocation("shaders/post/desaturate.json");
    static final boolean optifineInstalled;

    static {
        boolean tmp;
        try {
            Class.forName("optifine.OptiFineForgeTweaker");
            tmp = true;
        } catch (ClassNotFoundException e) {
            tmp = false;
        }
        optifineInstalled = tmp;
    }

    public static void clickMouse(net.minecraft.client.Minecraft mc) {
        if (mc.leftClickCounter <= 0) {
            boolean flag = KillItem.inList(mc.PLAYER);
            if (mc.objectMouseOver == null) {
                LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");

                if (mc.PlayerController.isNotCreative() && !flag) {
                    mc.leftClickCounter = 10;
                }
            } else if (!mc.PLAYER.isRowingBoat()) {
                switch (mc.objectMouseOver.typeOfHit) {
                    case ENTITY:
                        mc.PlayerController.attackEntity(mc.PLAYER, mc.objectMouseOver.entityHit);
                        break;
                    case BLOCK:
                        BlockPos blockpos = mc.objectMouseOver.getBlockPos();

                        if (!mc.WORLD.isAirBlock(blockpos)) {
                            mc.PlayerController.clickBlock(blockpos, mc.objectMouseOver.sideHit);
                            break;
                        }

                    case MISS:

                        if (mc.PlayerController.isNotCreative() && !flag) {
                            mc.leftClickCounter = 10;
                        }

                        mc.PLAYER.resetCooldown();
                        if (!(flag && mc.PLAYER.isSneaking())) {
                            ForgeHooks.onEmptyLeftClick(mc.PLAYER);
                        } else {
                            NetworkHandler.INSTANCE.sendMessageToServer(new BlackHole(mc.PLAYER.getUniqueID()));
                        }
                }

                mc.PLAYER.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }
    public static final Queue<Runnable> tasks = new LinkedBlockingQueue<>();
    private static long lastTime;

    public static void runTickKeyboard(net.minecraft.client.Minecraft minecraft) {
        if (Keys.SWITCH_TIME_POINT.isKeyDown()) {
            NetworkHandler.INSTANCE.sendMessageToServer(new SwitchTimePoint(minecraft.PLAYER.getUniqueID()));
        } else if (Keys.SAVE.isKeyDown()) {
            NetworkHandler.INSTANCE.sendMessageToServer(new SaveTimePoint(minecraft.PLAYER.getUniqueID()));
        }
    }

    public static void rightClickMouse(net.minecraft.client.Minecraft mc) {
        if (!mc.PlayerController.getIsHittingBlock()) {
            mc.rightClickDelayTimer = 4;

            boolean flag = KillItem.inList(mc.PLAYER);

            if (flag) {
                if (mc.PLAYER.isSneaking()) {
                    mc.rightClickDelayTimer = 8;
                    if (mode == 0) {
                        NetworkHandler.INSTANCE.sendMessageToServer(new Annihilation(mc.PLAYER.dimension, (int) mc.PLAYER.X, (int) mc.PLAYER.Y, (int) mc.PLAYER.Z));
                    } else if (!Core.demo) {
                        if (mode == 1) {
                            NetworkHandler.INSTANCE.sendMessageToServer(new ServerTimeStop(!TimeStop.isTimeStop()));
                        } else if (mode == 2) {
                            NetworkHandler.INSTANCE.sendMessageToServer(new TimeBack());
                        }
                    } else {
                        mc.PLAYER.sendMessage(new TextComponentString("当前版本为试用版，完整版请至#QQ2981196615购买。"));
                    }
                } else {
                    if (mc.PLAYER.getHeldItem(EnumHand.MAIN_HAND).getITEM() == ModMain.kill_item) {
                        NetworkHandler.INSTANCE.sendMessageToServer(new KillAll(getMinecraft().PLAYER.getUniqueID()));
                    }
                }
            }

            if (!mc.PLAYER.isRowingBoat() || flag) {
                if (mc.objectMouseOver == null) {
                    LOGGER.warn("Null returned as 'hitResult', mc shouldn't happen!");
                }

                for (EnumHand enumhand : EnumHand.values()) {
                    ItemStack itemstack = mc.PLAYER.getHeldItem(enumhand);

                    if (mc.objectMouseOver != null) {
                        switch (mc.objectMouseOver.typeOfHit) {
                            case ENTITY: {
                                if (mc.PlayerController.interactWithEntity(mc.PLAYER, mc.objectMouseOver.entityHit, mc.objectMouseOver, enumhand) == EnumActionResult.SUCCESS) {
                                    return;
                                }

                                if (mc.PlayerController.interactWithEntity(mc.PLAYER, mc.objectMouseOver.entityHit, enumhand) == EnumActionResult.SUCCESS) {
                                    return;
                                }

                                break;
                            }
                            case BLOCK: {
                                BlockPos blockpos = mc.objectMouseOver.getBlockPos();

                                if (mc.WORLD.getBlockState(blockpos).getMaterial() != Material.AIR) {
                                    int i = itemstack.getCount();
                                    EnumActionResult enumactionresult = mc.PlayerController.processRightClickBlock(mc.PLAYER, mc.WORLD, blockpos, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, enumhand);

                                    if (enumactionresult == EnumActionResult.SUCCESS) {
                                        mc.PLAYER.swingArm(enumhand);

                                        if (!itemstack.isEmpty() && (itemstack.getCount() != i || mc.PlayerController.isInCreativeMode())) {
                                            mc.EntityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                                        }

                                        return;
                                    }
                                }
                            }
                        }
                    }

                    if (itemstack.isEmpty() && (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit == RayTraceResult.Type.MISS))
                        net.minecraftforge.common.ForgeHooks.onEmptyClick(mc.PLAYER, enumhand);
                    if (!itemstack.isEmpty() && mc.PlayerController.processRightClick(mc.PLAYER, mc.WORLD, enumhand) == EnumActionResult.SUCCESS) {
                        mc.EntityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                        return;
                    }
                }
            }
        }
    }

    public static void RunGameLoop(net.minecraft.client.Minecraft mc) {
        if (dead || kanade.kill.util.Util.killing) {
            return;
        }
        while (!tasks.isEmpty()) {
            Runnable run = tasks.poll();
            run.run();
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

        int ticks = !stop ? (tickCount > 0 ? tickCount : Math.min(mc.timer.elapsedTicks, 10)) : 5;

        for (int j = 0; j < ticks; ++j) {
            if (kanade.kill.util.Util.killing) {
                return;
            }
            if (stop) {
                mc.EntityRenderer.loadShader(BlackAndWhite);
            } else {
                mc.EntityRenderer.stopUseShader();
            }
            try {
                mc.runTick();
            } catch (Throwable t) {
                Launch.LOGGER.warn("Catch exception", t);
            }
        }

        mc.Profiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        if (!stop) {
            mc.Profiler.endStartSection("sound");
            mc.soundHandler.setListener(mc.getRenderViewEntity(), mc.timer.renderPartialTicks); //Forge: MC-46445 Spectator mode particles and sounds computed from where you have been before
            mc.Profiler.endSection();
        }
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
            //onRenderTick();
            mc.Profiler.endStartSection("gameRenderer");
            try {
                mc.EntityRenderer.updateCameraAndRender((mc.isGamePaused || stop) ? mc.renderPartialTicksPaused : mc.timer.renderPartialTicks, i);
            } catch (Throwable ignored) {
            }
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

    public static long getSystemTime() {
        if (TimeStop.isTimeStop()) {
            return lastTime;
        }
        long ret = Sys.getTime() * 1000L / Sys.getTimerResolution();
        lastTime = ret;
        return ret;
    }

    /*private static FloatBuffer cosmicUVs = BufferUtils.createFloatBuffer(4 * 10);

    private static void onRenderTick(){
        cosmicUVs = BufferUtils.createFloatBuffer(4 * ModMain.COSMIC.length);
        TextureAtlasSprite icon;
        for (TextureAtlasSprite cosmicIcon : ModMain.COSMIC) {
            icon = cosmicIcon;

            cosmicUVs.put(icon.getMinU());
            cosmicUVs.put(icon.getMinV());
            cosmicUVs.put(icon.getMaxU());
            cosmicUVs.put(icon.getMaxV());
        }
        cosmicUVs.flip();
    }*/
}
