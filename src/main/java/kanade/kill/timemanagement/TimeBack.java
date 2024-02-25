package kanade.kill.timemanagement;

import kanade.kill.Launch;
import kanade.kill.util.FileUtils;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeBack {
    private static final File saves;
    private static final List<String> savedPoints = new ArrayList<>();
    private static int current_time_point = 0;

    static {
        saves = new File("saved_time_points");
        if (!saves.isDirectory()) {
            if (!saves.mkdir()) {
                throw new RuntimeException("MikuFATAL:Failed to create directory:saved_time_points");
            }
        } else {
            File[] files = saves.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) savedPoints.add(file.getAbsolutePath());
                }
            }
        }
    }

    public static File save() {
        Util.killing = true;

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd--HH:mm:ss");
        Date date = new Date();
        String time = sdf.format(date);
        Launch.LOGGER.info("Saving time point at " + time);
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        server.getPlayerList().saveAllPlayerData();
        for (WorldServer worldserver : server.Worlds) {
            if (worldserver != null) {
                worldserver.disableLevelSaving = false;
            }
        }
        server.saveAllWorlds(false);
        for (WorldServer worldserver : server.Worlds) {
            if (worldserver != null) {
                worldserver.flush();
            }
        }
        if (Launch.client && server instanceof IntegratedServer) {
            WorldClient worldClient = Minecraft.getMinecraft().WORLD;
            worldClient.getSaveHandler().flush();
            if (Minecraft.getMinecraft().integratedServer != null) {
                Minecraft.getMinecraft().integratedServer.saveAllWorlds(false);
                Minecraft.getMinecraft().integratedServer.getPlayerList().saveAllPlayerData();
            }
            Minecraft.getMinecraft().saveLoader.flushCache();
        }
        File world;
        File save = new File("saved_time_points" + File.separator + time);
        if (Launch.client && server instanceof IntegratedServer) {
            world = new File("saves" + File.separator + server.folderName);
        } else {
            world = new File("world");
        }
        if (!world.exists()) {
            throw new RuntimeException("The fuck?");
        }
        try {
            FileUtils.copyDir(world, save);
            savedPoints.add(save.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Util.killing = false;
        return save;
    }

    @Nullable
    public static File SwitchTimePoint() {
        if (savedPoints.isEmpty()) return null;
        if (current_time_point + 1 < savedPoints.size() && savedPoints.get(current_time_point + 1) != null) {
            current_time_point++;
        } else {
            current_time_point = 0;
        }
        return new File(savedPoints.get(current_time_point));
    }

    public static void back() {
        if (savedPoints.isEmpty()) {
            return;
        }
        Util.killing = true;

        File save = new File(savedPoints.get(current_time_point));
        if (!save.exists()) {
            System.out.println("The fuck? A saved time point doesn't exist!");
            savedPoints.remove(current_time_point);
            return;
        }
        File world;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (Launch.client && server instanceof IntegratedServer) {
            if (server.folderName == null) {
                throw new IllegalStateException("The fuck?");
            }
            world = new File("saves" + File.separator + server.folderName);
        } else {
            world = new File("world");
        }
        if (!world.exists()) {
            throw new RuntimeException("The fuck?");
        }
        try {
            FileUtils.deleteDir(world);
            FileUtils.copyDir(save, world);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        WorldServer worldServer = DimensionManager.getWorld(0);

        server.loadAllWorlds(server.folderName, Launch.client ? server.worldName : "world", worldServer.getWorldInfo().getSeed(), worldServer.getWorldInfo().getTerrainType(), worldServer.getWorldInfo().getGeneratorOptions());

        Util.killing = false;
    }
}
