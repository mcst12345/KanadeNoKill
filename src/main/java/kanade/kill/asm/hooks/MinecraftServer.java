package kanade.kill.asm.hooks;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import kanade.kill.Launch;
import kanade.kill.timemanagement.TimeStop;
import kanade.kill.util.Util;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.integrated.IntegratedPlayerList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;

import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.FutureTask;

import static net.minecraft.server.MinecraftServer.LOGGER;
import static net.minecraft.server.MinecraftServer.getCurrentTimeMillis;

public class MinecraftServer {
    public static final Queue<FutureTask<?>> futureTaskQueue = Queues.newArrayDeque();

    public static void AddTask(Runnable runnable) {
        ListenableFutureTask<Object> task = ListenableFutureTask.create(runnable, null);
        synchronized (futureTaskQueue) {
            futureTaskQueue.add(task);
        }
    }

    public static void tick(net.minecraft.server.MinecraftServer server) {
        synchronized (futureTaskQueue) {
            while (!futureTaskQueue.isEmpty()) {
                Runnable task = futureTaskQueue.poll();
                task.run();
            }
        }
        if (Util.killing) {
            return;
        }
        boolean stop = TimeStop.isTimeStop();
        long i = System.nanoTime();
        if (!stop) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreServerTick();
            ++server.tickCounter;
        }

        if (server.startProfiling) {
            server.startProfiling = false;
            server.Profiler.profilingEnabled = true;
            server.Profiler.clearProfiling();
        }

        server.Profiler.startSection("root");
        try {
            server.updateTimeLightAndEntities();
        } catch (Throwable t) {
            Launch.LOGGER.warn("Catch exception when ticking:", t);
        }

        if (!stop && i - server.nanoTimeSinceStatusRefresh >= 5000000000L) {
            server.nanoTimeSinceStatusRefresh = i;
            server.statusResponse.setPlayers(new ServerStatusResponse.Players(server.getMaxPlayers(), server.getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(server.getCurrentPlayerCount(), 12)];
            int j = MathHelper.getInt(server.random, 0, server.getCurrentPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = server.PlayerList.getPlayers().get(j + k).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            server.statusResponse.getPlayers().setPlayers(agameprofile);
            server.statusResponse.invalidateJson();
        }

        if (server.tickCounter % 900 == 0) {
            server.Profiler.startSection("save");
            try {
                server.PlayerList.saveAllPlayerData();
                server.saveAllWorlds(true);
            } catch (Throwable t) {
                Launch.LOGGER.warn("Exception when saving data:", t);
            }
            server.Profiler.endSection();
        }

        if (!stop) {
            server.Profiler.startSection("tallying");
            server.tickTimeArray[server.tickCounter % 100] = System.nanoTime() - i;
            server.Profiler.endSection();
            server.Profiler.startSection("snooper");

            if (!server.usageSnooper.isSnooperRunning() && server.tickCounter > 100) {
                server.usageSnooper.startSnooper();
            }

            if (server.tickCounter % 6000 == 0) {
                server.usageSnooper.addMemoryStatsToSnooper();
            }

            server.Profiler.endSection();
        }
        server.Profiler.endSection();
        if (!stop) {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostServerTick();
        }
    }

    public static void run(net.minecraft.server.MinecraftServer server) {
        try {
            if (server.init()) {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStarted();
                server.currentTime = getCurrentTimeMillis();
                long i = 0L;
                server.statusResponse.setServerDescription(new TextComponentString(server.motd));
                server.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
                server.applyServerIconToResponse(server.statusResponse);

                while (server.serverRunning) {
                    long k = getCurrentTimeMillis();
                    long j = k - server.currentTime;

                    if (j > 2000L && server.currentTime - server.timeOfLastWarning >= 15000L) {
                        LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", j, j / 50L);
                        j = 2000L;
                        server.timeOfLastWarning = server.currentTime;
                    }

                    if (j < 0L) {
                        LOGGER.warn("Time ran backwards! Did the system time change?");
                        j = 0L;
                    }

                    i += j;
                    server.currentTime = k;

                    try {
                        if (server.Worlds[0].areAllPlayersAsleep()) {
                            server.tick();
                            i = 0L;
                        } else {
                            while (i > 50L) {
                                i -= 50L;
                                server.tick();
                            }
                        }
                    } catch (Throwable t) {
                        Launch.LOGGER.warn("Catch exception when server ticking:", t);
                    }

                    Thread.sleep(Math.max(1L, 50L - i));
                    server.serverIsRunning = true;
                }
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopping();
                net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            } else {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
                server.finalTick(null);
            }
        } catch (net.minecraftforge.fml.common.StartupQuery.AbortedException e) {
            // ignore silently
            net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
        } catch (Throwable throwable1) {
            Launch.LOGGER.warn("Catch exception in server main loop,continue anyway", throwable1);
        } finally {
            try {
                server.stopServer();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopped();
                server.serverStopped = true;
                server.systemExitNow();
            }
        }
    }

    public static void setPlayerList(net.minecraft.server.MinecraftServer server, PlayerList list) {
        if ((!Launch.client && list.getClass() != DedicatedPlayerList.class) || (Launch.client && list.getClass() != IntegratedPlayerList.class)) {
            Launch.LOGGER.warn("Someone wanted to modify the PlayerList to a bad one.");
            return;
        }
        server.PlayerList = list;
    }
}
