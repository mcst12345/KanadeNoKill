package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@SuppressWarnings("unused")
public abstract class MinecraftServer implements ICommandSender, Runnable, IThreadListener, ISnooperInfo {
    public static final File USER_CACHE_FILE = new File("usercache.json");
    public static final Logger LOGGER = LogManager.getLogger();
    public final ICommandManager commandManager;
    public final Snooper usageSnooper = new Snooper("server", this, getCurrentTimeMillis());
    public final ServerStatusResponse statusResponse = new ServerStatusResponse();
    public final long[] tickTimeArray = new long[100];
    public final Queue<FutureTask<?>> futureTaskQueue = Queues.newArrayDeque();
    protected final Proxy serverProxy;
    private final ISaveFormat anvilConverterForAnvilFile;
    public final Random random = new Random();
    private final File anvilFile;
    private final List<ITickable> tickables = Lists.newArrayList();
    private final NetworkSystem networkSystem;
    public Profiler Profiler = new Profiler();
    public Profiler profiler = new Profiler();
    public WorldServer[] worlds = new WorldServer[0];
    public WorldServer[] Worlds = new WorldServer[0];
    private final DataFixer dataFixer;
    private final YggdrasilAuthenticationService authService;
    private final MinecraftSessionService sessionService;
    private final GameProfileRepository profileRepo;
    private final PlayerProfileCache profileCache;
    public String currentTask;
    public int percentDone;
    //public long[][] timeOfLastDimensionTick;
    public java.util.Hashtable<Integer, long[]> worldTickTimes = new java.util.Hashtable<>();
    @SideOnly(Side.CLIENT)
    public String worldName;
    public long currentTime = getCurrentTimeMillis();
    @SideOnly(Side.SERVER)
    private String hostname;
    private int serverPort = -1;
    public PlayerList PlayerList;
    public boolean serverRunning = true;
    public boolean serverStopped;
    public int tickCounter;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean canSpawnAnimals;
    private boolean canSpawnNPCs;
    public String folderName;
    private boolean pvpEnabled;
    private boolean allowFlight;
    public String motd;
    private int buildLimit;
    private int maxPlayerIdleMinutes;
    private KeyPair serverKeyPair;
    private String serverOwner;
    private boolean isDemo;
    private boolean enableBonusChest;
    private String resourcePackUrl = "";
    private String resourcePackHash = "";
    public boolean serverIsRunning;
    public long timeOfLastWarning;
    private String userMessage;
    public boolean startProfiling;
    private boolean isGamemodeForced;
    public long nanoTimeSinceStatusRefresh;
    private Thread serverThread;
    @SideOnly(Side.CLIENT)
    private boolean worldIconSet;

    public MinecraftServer(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {
        this.serverProxy = proxyIn;
        this.authService = authServiceIn;
        this.sessionService = sessionServiceIn;
        this.profileRepo = profileRepoIn;
        this.profileCache = profileCacheIn;
        this.anvilFile = anvilFileIn;
        this.networkSystem = new NetworkSystem(this);
        this.commandManager = this.createCommandManager();
        this.anvilConverterForAnvilFile = new AnvilSaveConverter(anvilFileIn, dataFixerIn);
        this.dataFixer = dataFixerIn;
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @SideOnly(Side.SERVER)
    public static void main(String[] p_main_0_) {
        //Forge: Copied from DedicatedServer.init as to run as early as possible, Old code left in place intentionally.
        //Done in good faith with permission: https://github.com/MinecraftForge/MinecraftForge/issues/3659#issuecomment-390467028
        ServerEula eula = new ServerEula(new File("eula.txt"));
        if (!eula.hasAcceptedEULA()) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            eula.createEULAFile();
            return;
        }

        Bootstrap.register();

        try {
            boolean flag = true;
            String s = null;
            String s1 = ".";
            String s2 = null;
            boolean flag1 = false;
            boolean flag2 = false;
            int l = -1;

            for (int i1 = 0; i1 < p_main_0_.length; ++i1) {
                String s3 = p_main_0_[i1];
                String s4 = i1 == p_main_0_.length - 1 ? null : p_main_0_[i1 + 1];
                boolean flag3 = false;

                if (!"nogui".equals(s3) && !"--nogui".equals(s3)) {
                    if ("--port".equals(s3) && s4 != null) {
                        flag3 = true;

                        try {
                            l = Integer.parseInt(s4);
                        } catch (NumberFormatException ignored) {
                        }
                    } else if ("--singleplayer".equals(s3) && s4 != null) {
                        flag3 = true;
                        s = s4;
                    } else if ("--universe".equals(s3) && s4 != null) {
                        flag3 = true;
                        s1 = s4;
                    } else if ("--world".equals(s3) && s4 != null) {
                        flag3 = true;
                        s2 = s4;
                    } else if ("--demo".equals(s3)) {
                        flag1 = true;
                    } else if ("--bonusChest".equals(s3)) {
                        flag2 = true;
                    }
                } else {
                    flag = false;
                }

                if (flag3) {
                    ++i1;
                }
            }

            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(s1, USER_CACHE_FILE.getName()));
            final DedicatedServer dedicatedserver = new DedicatedServer(new File(s1), DataFixesManager.createFixer(), yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, playerprofilecache);

            if (s != null) {
                dedicatedserver.setServerOwner(s);
            }

            if (s2 != null) {
                dedicatedserver.setFolderName(s2);
            }

            if (l >= 0) {
                dedicatedserver.setServerPort(l);
            }

            if (flag1) {
                dedicatedserver.setDemo(true);
            }

            if (flag2) {
                dedicatedserver.canCreateBonusChest(true);
            }

            if (flag && !GraphicsEnvironment.isHeadless()) {
                dedicatedserver.setGuiEnabled();
            }

            dedicatedserver.startServerThread();
            Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {
                public void run() {
                    dedicatedserver.stopServer();
                }
            });
        } catch (Exception exception) {
            LOGGER.fatal("Failed to start the minecraft server", exception);
        }
    }

    public ServerCommandManager createCommandManager() {
        return new ServerCommandManager(this);
    }

    public abstract boolean init() throws IOException;

    public void convertMapIfNeeded(String worldNameIn) {
        if (this.getActiveAnvilConverter().isOldMapFormat(worldNameIn)) {
            LOGGER.info("Converting map!");
            this.setUserMessage("menu.convertingLevel");
            this.getActiveAnvilConverter().convertMapFormat(worldNameIn, new IProgressUpdate() {
                private long startTime = System.currentTimeMillis();

                public void displaySavingString(String message) {
                }

                public void setLoadingProgress(int progress) {
                    if (System.currentTimeMillis() - this.startTime >= 1000L) {
                        this.startTime = System.currentTimeMillis();
                        MinecraftServer.LOGGER.info("Converting... {}%", progress);
                    }
                }

                @SideOnly(Side.CLIENT)
                public void resetProgressAndMessage(String message) {
                }

                @SideOnly(Side.CLIENT)
                public void setDoneWorking() {
                }

                public void displayLoadingString(String message) {
                }
            });
        }
    }

    @Nullable
    @SideOnly(Side.CLIENT)

    public synchronized String getUserMessage() {
        return this.userMessage;
    }

    protected synchronized void setUserMessage(String message) {
        this.userMessage = message;
    }

    public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
        this.convertMapIfNeeded(saveName);
        this.setUserMessage("menu.loadingLevel");
        ISaveHandler isavehandler = this.anvilConverterForAnvilFile.getSaveLoader(saveName, true);
        this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();
        WorldSettings worldsettings;

        if (worldinfo == null) {
            if (this.isDemo()) {
                worldsettings = WorldServerDemo.DEMO_WORLD_SETTINGS;
            } else {
                worldsettings = new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
                worldsettings.setGeneratorOptions(generatorOptions);

                if (this.enableBonusChest) {
                    worldsettings.enableBonusChest();
                }
            }

            worldinfo = new WorldInfo(worldsettings, worldNameIn);
        } else {
            worldinfo.setWorldName(worldNameIn);
            worldsettings = new WorldSettings(worldinfo);
        }

        if (false) { //Forge Dead code, reimplemented below
            for (int i = 0; i < this.Worlds.length; ++i) {
                int j = 0;

                if (i == 1) {
                    j = -1;
                }

                if (i == 2) {
                    j = 1;
                }

                if (i == 0) {
                    if (this.isDemo()) {
                        this.Worlds[i] = (WorldServer) (new WorldServerDemo(this, isavehandler, worldinfo, j, this.Profiler)).init();
                    } else {
                        this.Worlds[i] = (WorldServer) (new WorldServer(this, isavehandler, worldinfo, j, this.Profiler)).init();
                    }

                    this.Worlds[i].initialize(worldsettings);
                } else {
                    this.Worlds[i] = (WorldServer) (new WorldServerMulti(this, isavehandler, j, this.Worlds[0], this.Profiler)).init();
                }

                this.Worlds[i].addEventListener(new ServerWorldEventHandler(this, this.Worlds[i]));

                if (!this.isSinglePlayer()) {
                    this.Worlds[i].getWorldInfo().setGameType(this.getGameType());
                }
            }
        } //Forge: End dead code

        WorldServer overWorld = (WorldServer) (isDemo() ? new WorldServerDemo(this, isavehandler, worldinfo, 0, Profiler).init() : new WorldServer(this, isavehandler, worldinfo, 0, Profiler).init());
        overWorld.initialize(worldsettings);
        for (int dim : net.minecraftforge.common.DimensionManager.getStaticDimensionIDs()) {
            WorldServer world = (dim == 0 ? overWorld : (WorldServer) new WorldServerMulti(this, isavehandler, dim, overWorld, Profiler).init());
            world.addEventListener(new ServerWorldEventHandler(this, world));

            if (!this.isSinglePlayer()) {
                world.getWorldInfo().setGameType(this.getGameType());
            }
            MinecraftForge.Event_bus.post(new net.minecraftforge.event.world.WorldEvent.Load(world));
        }

        this.PlayerList.setPlayerManager(new WorldServer[]{overWorld});
        this.setDifficultyForAllWorlds(this.getDifficulty());
        this.initialWorldChunkLoad();
    }

    public void initialWorldChunkLoad() {
        int i = 16;
        int j = 4;
        int k = 192;
        int l = 625;
        int i1 = 0;
        this.setUserMessage("menu.generatingTerrain");
        int j1 = 0;
        LOGGER.info("Preparing start region for level 0");
        WorldServer worldserver = net.minecraftforge.common.DimensionManager.getWorld(j1);
        BlockPos blockpos = worldserver.getSpawnPoint();
        long k1 = getCurrentTimeMillis();

        for (int l1 = -192; l1 <= 192 && this.isServerRunning(); l1 += 16) {
            for (int i2 = -192; i2 <= 192 && this.isServerRunning(); i2 += 16) {
                long j2 = getCurrentTimeMillis();

                if (j2 - k1 > 1000L) {
                    this.outputPercentRemaining("Preparing spawn area", i1 * 100 / 625);
                    k1 = j2;
                }

                ++i1;
                worldserver.getChunkProvider().provideChunk(blockpos.getX() + l1 >> 4, blockpos.getZ() + i2 >> 4);
            }
        }

        this.clearCurrentTask();
    }

    public void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn) {
        File file1 = new File(saveHandlerIn.getWorldDirectory(), "resources.zip");

        if (file1.isFile()) {
            try {
                this.setResourcePack("level://" + URLEncoder.encode(worldNameIn, StandardCharsets.UTF_8.toString()) + "/" + "resources.zip", "");
            } catch (UnsupportedEncodingException var5) {
                LOGGER.warn("Something went wrong url encoding {}", worldNameIn);
            }
        }
    }

    public abstract boolean canStructuresSpawn();

    public abstract GameType getGameType();

    public void setGameType(GameType gameMode) {
        for (WorldServer worldserver1 : this.Worlds) {
            worldserver1.getWorldInfo().setGameType(gameMode);
        }
    }

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int getOpPermissionLevel();

    public abstract boolean shouldBroadcastRconToOps();

    public abstract boolean shouldBroadcastConsoleToOps();

    protected void outputPercentRemaining(String message, int percent) {
        this.currentTask = message;
        this.percentDone = percent;
        LOGGER.info("{}: {}%", message, percent);
    }

    protected void clearCurrentTask() {
        this.currentTask = null;
        this.percentDone = 0;
    }

    public void saveAllWorlds(boolean isSilent) {
        for (WorldServer worldserver : this.Worlds) {
            if (worldserver != null) {
                if (!isSilent) {
                    LOGGER.info("Saving chunks for level '{}'/{}", worldserver.getWorldInfo().getWorldName(), worldserver.provider.getDimensionType().getName());
                }

                try {
                    worldserver.saveAllChunks(true, null);
                } catch (MinecraftException minecraftexception) {
                    LOGGER.warn(minecraftexception.getMessage());
                }
            }
        }
    }

    public void stopServer() {
        LOGGER.info("Stopping server");

        if (this.getNetworkSystem() != null) {
            this.getNetworkSystem().terminateEndpoints();
        }

        if (this.PlayerList != null) {
            LOGGER.info("Saving players");
            this.PlayerList.saveAllPlayerData();
            this.PlayerList.removeAllPlayers();
        }

        if (this.Worlds != null) {
            LOGGER.info("Saving worlds");

            for (WorldServer worldserver : this.Worlds) {
                if (worldserver != null) {
                    worldserver.disableLevelSaving = false;
                }
            }

            this.saveAllWorlds(false);

            for (WorldServer worldserver1 : this.Worlds) {
                if (worldserver1 != null) {
                    MinecraftForge.Event_bus.post(new net.minecraftforge.event.world.WorldEvent.Unload(worldserver1));
                    worldserver1.flush();
                }
            }

            WorldServer[] tmp = Worlds;
            for (WorldServer world : tmp) {
                net.minecraftforge.common.DimensionManager.setWorld(world.provider.getDimension(), null, this);
            }
        }

        if (this.usageSnooper.isSnooperRunning()) {
            this.usageSnooper.stopSnooper();
        }

        CommandBase.setCommandListener(null); // Forge: fix MC-128561
    }

    public boolean isServerRunning() {
        return this.serverRunning;
    }

    public void initiateShutdown() {
        this.serverRunning = false;
    }

    public void run() {
        try {
            if (this.init()) {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStarted();
                this.currentTime = getCurrentTimeMillis();
                long i = 0L;
                this.statusResponse.setServerDescription(new TextComponentString(this.motd));
                this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
                this.applyServerIconToResponse(this.statusResponse);

                while (this.serverRunning) {
                    long k = getCurrentTimeMillis();
                    long j = k - this.currentTime;

                    if (j > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L) {
                        LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", j, j / 50L);
                        j = 2000L;
                        this.timeOfLastWarning = this.currentTime;
                    }

                    if (j < 0L) {
                        LOGGER.warn("Time ran backwards! Did the system time change?");
                        j = 0L;
                    }

                    i += j;
                    this.currentTime = k;

                    if (this.Worlds[0].areAllPlayersAsleep()) {
                        this.tick();
                        i = 0L;
                    } else {
                        while (i > 50L) {
                            i -= 50L;
                            this.tick();
                        }
                    }

                    Thread.sleep(Math.max(1L, 50L - i));
                    this.serverIsRunning = true;
                }
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopping();
                net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            } else {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
                this.finalTick(null);
            }
        } catch (net.minecraftforge.fml.common.StartupQuery.AbortedException e) {
            // ignore silently
            net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
        } catch (Throwable throwable1) {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport;

            if (throwable1 instanceof ReportedException) {
                crashreport = this.addServerInfoToCrashReport(((ReportedException) throwable1).getCrashReport());
            } else {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1)) {
                LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            this.finalTick(crashreport);
        } finally {
            try {
                this.stopServer();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopped();
                this.serverStopped = true;
                this.systemExitNow();
            }
        }
    }

    public void applyServerIconToResponse(ServerStatusResponse response) {
        File file1 = this.getFile("server-icon.png");

        if (!file1.exists()) {
            file1 = this.getActiveAnvilConverter().getFile(this.getFolderName(), "icon.png");
        }

        if (file1.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file1);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = Base64.encode(bytebuf);
                response.setFavicon("data:image/png;base64," + bytebuf1.toString(StandardCharsets.UTF_8));
                bytebuf1.release(); // Forge: fix MC-122085
            } catch (Exception exception) {
                LOGGER.error("Couldn't load server icon", exception);
            } finally {
                bytebuf.release();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isWorldIconSet() {
        this.worldIconSet = this.worldIconSet || this.getWorldIconFile().isFile();
        return this.worldIconSet;
    }

    @SideOnly(Side.CLIENT)
    public File getWorldIconFile() {
        return this.getActiveAnvilConverter().getFile(this.getFolderName(), "icon.png");
    }

    public File getDataDirectory() {
        return new File(".");
    }

    public void finalTick(CrashReport report) {
    }

    public void systemExitNow() {
    }

    public void tick() {
        long i = System.nanoTime();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreServerTick();
        ++this.tickCounter;

        if (this.startProfiling) {
            this.startProfiling = false;
            this.Profiler.profilingEnabled = true;
            this.Profiler.clearProfiling();
        }

        this.Profiler.startSection("root");
        this.updateTimeLightAndEntities();

        if (i - this.nanoTimeSinceStatusRefresh >= 5000000000L) {
            this.nanoTimeSinceStatusRefresh = i;
            this.statusResponse.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
            int j = MathHelper.getInt(this.random, 0, this.getCurrentPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = this.PlayerList.getPlayers().get(j + k).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.statusResponse.getPlayers().setPlayers(agameprofile);
            this.statusResponse.invalidateJson();
        }

        if (this.tickCounter % 900 == 0) {
            this.Profiler.startSection("save");
            this.PlayerList.saveAllPlayerData();
            this.saveAllWorlds(true);
            this.Profiler.endSection();
        }

        this.Profiler.startSection("tallying");
        this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
        this.Profiler.endSection();
        this.Profiler.startSection("snooper");

        if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100) {
            this.usageSnooper.startSnooper();
        }

        if (this.tickCounter % 6000 == 0) {
            this.usageSnooper.addMemoryStatsToSnooper();
        }

        this.Profiler.endSection();
        this.Profiler.endSection();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostServerTick();
    }

    public void updateTimeLightAndEntities() {
        this.Profiler.startSection("jobs");

        synchronized (this.futureTaskQueue) {
            while (!this.futureTaskQueue.isEmpty()) {
                Util.runTask(this.futureTaskQueue.poll(), LOGGER);
            }
        }

        this.Profiler.endStartSection("levels");
        net.minecraftforge.common.chunkio.ChunkIOExecutor.tick();

        Integer[] ids = net.minecraftforge.common.DimensionManager.getIDs(this.tickCounter % 200 == 0);
        for (int id : ids) {
            long i = System.nanoTime();

            if (id == 0 || this.getAllowNether()) {
                WorldServer worldserver = net.minecraftforge.common.DimensionManager.getWorld(id);
                this.Profiler.func_194340_a(() ->
                        worldserver.getWorldInfo().getWorldName());

                if (this.tickCounter % 20 == 0) {
                    this.Profiler.startSection("timeSync");
                    this.PlayerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")), worldserver.provider.getDimension());
                    this.Profiler.endSection();
                }

                this.Profiler.startSection("tick");
                net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreWorldTick(worldserver);

                try {
                    worldserver.tick();
                } catch (Throwable throwable1) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
                    worldserver.addWorldInfoToCrashReport(crashreport);
                    throw new ReportedException(crashreport);
                }

                try {
                    worldserver.updateEntities();
                } catch (Throwable throwable) {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
                    worldserver.addWorldInfoToCrashReport(crashreport1);
                    throw new ReportedException(crashreport1);
                }

                net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostWorldTick(worldserver);
                this.Profiler.endSection();
                this.Profiler.startSection("tracker");
                worldserver.getEntityTracker().tick();
                this.Profiler.endSection();
                this.Profiler.endSection();
            }

            worldTickTimes.get(id)[this.tickCounter % 100] = System.nanoTime() - i;
        }

        this.Profiler.endStartSection("dim_unloading");
        net.minecraftforge.common.DimensionManager.unloadWorlds(worldTickTimes);
        this.Profiler.endStartSection("connection");
        this.getNetworkSystem().networkTick();
        this.Profiler.endStartSection("players");
        this.PlayerList.onTick();
        this.Profiler.endStartSection("commandFunctions");
        this.getFunctionManager().update();
        this.Profiler.endStartSection("tickables");

        for (ITickable tickable : this.tickables) {
            tickable.update();
        }

        this.Profiler.endSection();
    }

    public boolean getAllowNether() {
        return true;
    }

    public void startServerThread() {
        net.minecraftforge.fml.common.StartupQuery.reset();
        this.serverThread = new Thread(net.minecraftforge.fml.common.thread.SidedThreadGroups.SERVER, this, "Server thread");
        this.serverThread.start();
    }

    public File getFile(String fileName) {
        return new File(this.getDataDirectory(), fileName);
    }

    public void logWarning(String msg) {
        LOGGER.warn(msg);
    }

    public WorldServer getWorld(int dimension) {
        WorldServer ret = net.minecraftforge.common.DimensionManager.getWorld(dimension, true);
        if (ret == null) {
            net.minecraftforge.common.DimensionManager.initDimension(dimension);
            ret = net.minecraftforge.common.DimensionManager.getWorld(dimension);
        }
        return ret;
    }

    public String getMinecraftVersion() {
        return "1.12.2";
    }

    public int getCurrentPlayerCount() {
        return this.PlayerList.getCurrentPlayerCount();
    }

    public int getMaxPlayers() {
        return this.PlayerList.getMaxPlayers();
    }

    public String[] getOnlinePlayerNames() {
        return this.PlayerList.getOnlinePlayerNames();
    }

    public GameProfile[] getOnlinePlayerProfiles() {
        return this.PlayerList.getOnlinePlayerProfiles();
    }

    public String getServerModName() {
        return net.minecraftforge.fml.common.FMLCommonHandler.instance().getModName();
    }

    public CrashReport addServerInfoToCrashReport(CrashReport report) {
        report.getCategory().addDetail("Profiler Position", () -> MinecraftServer.this.Profiler.profilingEnabled ? MinecraftServer.this.Profiler.getNameOfLastSection() : "N/A (disabled)");

        if (this.PlayerList != null) {
            report.getCategory().addDetail("Player Count", () -> MinecraftServer.this.PlayerList.getCurrentPlayerCount() + " / " + MinecraftServer.this.PlayerList.getMaxPlayers() + "; " + MinecraftServer.this.PlayerList.getPlayers());
        }

        return report;
    }

    public List<String> getTabCompletions(ICommandSender sender, String input, @Nullable BlockPos pos, boolean hasTargetBlock) {
        List<String> list = Lists.newArrayList();
        boolean flag = input.startsWith("/");

        if (flag) {
            input = input.substring(1);
        }

        if (!flag && !hasTargetBlock) {
            String[] astring = input.split(" ", -1);
            String s2 = astring[astring.length - 1];

            for (String s1 : this.PlayerList.getOnlinePlayerNames()) {
                if (CommandBase.doesStringStartWith(s2, s1)) {
                    list.add(s1);
                }
            }

        } else {
            boolean flag1 = !input.contains(" ");
            List<String> list1 = this.commandManager.getTabCompletions(sender, input, pos);

            if (!list1.isEmpty()) {
                for (String s : list1) {
                    if (flag1 && !hasTargetBlock) {
                        list.add("/" + s);
                    } else {
                        list.add(s);
                    }
                }
            }

        }
        return list;
    }

    public boolean isAnvilFileSet() {
        return this.anvilFile != null;
    }

    public String getName() {
        return "Server";
    }

    public void sendMessage(ITextComponent component) {
        LOGGER.info(component.getUnformattedText());
    }

    public boolean canUseCommand(int permLevel, String commandName) {
        return true;
    }

    public ICommandManager getCommandManager() {
        return this.commandManager;
    }

    public KeyPair getKeyPair() {
        return this.serverKeyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.serverKeyPair = keyPair;
    }

    public String getServerOwner() {
        return this.serverOwner;
    }

    public void setServerOwner(String owner) {
        this.serverOwner = owner;
    }

    public boolean isSinglePlayer() {
        return this.serverOwner != null;
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void setFolderName(String name) {
        this.folderName = name;
    }

    @SideOnly(Side.CLIENT)
    public String getWorldName() {
        return this.worldName;
    }

    @SideOnly(Side.CLIENT)
    public void setWorldName(String worldNameIn) {
        this.worldName = worldNameIn;
    }

    public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
        for (WorldServer worldserver1 : this.Worlds) {
            if (worldserver1 != null) {
                if (worldserver1.getWorldInfo().isHardcoreModeEnabled()) {
                    worldserver1.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
                    worldserver1.setAllowedSpawnTypes(true, true);
                } else if (this.isSinglePlayer()) {
                    worldserver1.getWorldInfo().setDifficulty(difficulty);
                    worldserver1.setAllowedSpawnTypes(worldserver1.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                } else {
                    worldserver1.getWorldInfo().setDifficulty(difficulty);
                    worldserver1.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
                }
            }
        }
    }

    public boolean allowSpawnMonsters() {
        return true;
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean demo) {
        this.isDemo = demo;
    }

    public void canCreateBonusChest(boolean enable) {
        this.enableBonusChest = enable;
    }

    public ISaveFormat getActiveAnvilConverter() {
        return this.anvilConverterForAnvilFile;
    }

    public String getResourcePackUrl() {
        return this.resourcePackUrl;
    }

    public String getResourcePackHash() {
        return this.resourcePackHash;
    }

    public void setResourcePack(String url, String hash) {
        this.resourcePackUrl = url;
        this.resourcePackHash = hash;
    }

    public void addServerStatsToSnooper(Snooper playerSnooper) {
        playerSnooper.addClientStat("whitelist_enabled", Boolean.FALSE);
        playerSnooper.addClientStat("whitelist_count", 0);

        if (this.PlayerList != null) {
            playerSnooper.addClientStat("players_current", this.getCurrentPlayerCount());
            playerSnooper.addClientStat("players_max", this.getMaxPlayers());
            playerSnooper.addClientStat("players_seen", this.PlayerList.getAvailablePlayerDat().length);
        }

        playerSnooper.addClientStat("uses_auth", this.onlineMode);
        playerSnooper.addClientStat("gui_state", this.getGuiEnabled() ? "enabled" : "disabled");
        playerSnooper.addClientStat("run_time", (getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
        playerSnooper.addClientStat("avg_tick_ms", (int) (MathHelper.average(this.tickTimeArray) * 1.0E-6D));
        int l = 0;

        if (this.Worlds != null) {
            for (WorldServer worldserver1 : this.Worlds) {
                if (worldserver1 != null) {
                    WorldInfo worldinfo = worldserver1.getWorldInfo();
                    playerSnooper.addClientStat("world[" + l + "][dimension]", worldserver1.provider.getDimensionType().getId());
                    playerSnooper.addClientStat("world[" + l + "][mode]", worldinfo.getGameType());
                    playerSnooper.addClientStat("world[" + l + "][difficulty]", worldserver1.getDifficulty());
                    playerSnooper.addClientStat("world[" + l + "][hardcore]", worldinfo.isHardcoreModeEnabled());
                    playerSnooper.addClientStat("world[" + l + "][generator_name]", worldinfo.getTerrainType().getName());
                    playerSnooper.addClientStat("world[" + l + "][generator_version]", worldinfo.getTerrainType().getVersion());
                    playerSnooper.addClientStat("world[" + l + "][height]", this.buildLimit);
                    playerSnooper.addClientStat("world[" + l + "][chunks_loaded]", worldserver1.getChunkProvider().getLoadedChunkCount());
                    ++l;
                }
            }
        }

        playerSnooper.addClientStat("worlds", l);
    }

    public void addServerTypeToSnooper(Snooper playerSnooper) {
        playerSnooper.addStatToSnooper("singleplayer", this.isSinglePlayer());
        playerSnooper.addStatToSnooper("server_brand", this.getServerModName());
        playerSnooper.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        playerSnooper.addStatToSnooper("dedicated", this.isDedicatedServer());
    }

    public boolean isSnooperEnabled() {
        return true;
    }

    public abstract boolean isDedicatedServer();

    public boolean isServerInOnlineMode() {
        return this.onlineMode;
    }

    public void setOnlineMode(boolean online) {
        this.onlineMode = online;
    }

    public boolean getPreventProxyConnections() {
        return this.preventProxyConnections;
    }

    @SideOnly(Side.SERVER)
    public void setPreventProxyConnections(boolean p_190517_1_) {
        this.preventProxyConnections = p_190517_1_;
    }

    public boolean getCanSpawnAnimals() {
        return this.canSpawnAnimals;
    }

    public void setCanSpawnAnimals(boolean spawnAnimals) {
        this.canSpawnAnimals = spawnAnimals;
    }

    public boolean getCanSpawnNPCs() {
        return this.canSpawnNPCs;
    }

    public void setCanSpawnNPCs(boolean spawnNpcs) {
        this.canSpawnNPCs = spawnNpcs;
    }

    public abstract boolean shouldUseNativeTransport();

    public boolean isPVPEnabled() {
        return this.pvpEnabled;
    }

    public void setAllowPvp(boolean allowPvp) {
        this.pvpEnabled = allowPvp;
    }

    public boolean isFlightAllowed() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean allow) {
        this.allowFlight = allow;
    }

    public abstract boolean isCommandBlockEnabled();

    public String getMOTD() {
        return this.motd;
    }

    public void setMOTD(String motdIn) {
        this.motd = motdIn;
    }

    public int getBuildLimit() {
        return this.buildLimit;
    }

    public void setBuildLimit(int maxBuildHeight) {
        this.buildLimit = maxBuildHeight;
    }

    public boolean isServerStopped() {
        return this.serverStopped;
    }

    public PlayerList getPlayerList() {
        return this.PlayerList;
    }

    public void setPlayerList(PlayerList list) {
        this.PlayerList = list;
    }

    public NetworkSystem getNetworkSystem() {
        return this.networkSystem;
    }

    @SideOnly(Side.CLIENT)
    public boolean serverIsInRunLoop() {
        return this.serverIsRunning;
    }

    public boolean getGuiEnabled() {
        return false;
    }

    public abstract String shareToLAN(GameType type, boolean allowCheats);

    public int getTickCounter() {
        return this.tickCounter;
    }

    public void enableProfiling() {
        this.startProfiling = true;
    }

    @SideOnly(Side.CLIENT)
    public Snooper getPlayerUsageSnooper() {
        return this.usageSnooper;
    }

    public World getEntityWorld() {
        return this.Worlds[0];
    }

    public boolean isBlockProtected(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        return false;
    }

    public boolean getForceGamemode() {
        return this.isGamemodeForced;
    }

    @SideOnly(Side.SERVER)
    public void setForceGamemode(boolean force) {
        this.isGamemodeForced = force;
    }

    public Proxy getServerProxy() {
        return this.serverProxy;
    }

    public int getMaxPlayerIdleMinutes() {
        return this.maxPlayerIdleMinutes;
    }

    public void setPlayerIdleTimeout(int idleTimeout) {
        this.maxPlayerIdleMinutes = idleTimeout;
    }

    public MinecraftSessionService getMinecraftSessionService() {
        return this.sessionService;
    }

    public GameProfileRepository getGameProfileRepository() {
        return this.profileRepo;
    }

    public PlayerProfileCache getPlayerProfileCache() {
        return this.profileCache;
    }

    public ServerStatusResponse getServerStatusResponse() {
        return this.statusResponse;
    }

    public void refreshStatusNextTick() {
        this.nanoTimeSinceStatusRefresh = 0L;
    }

    @Nullable
    public Entity getEntityFromUuid(UUID uuid) {
        for (WorldServer worldserver1 : this.Worlds) {
            if (worldserver1 != null) {
                Entity entity = worldserver1.getEntityFromUuid(uuid);

                if (entity != null) {
                    return entity;
                }
            }
        }

        return null;
    }

    public boolean sendCommandFeedback() {
        return this.Worlds[0].getGameRules().getBoolean("sendCommandFeedback");
    }

    public MinecraftServer getServer() {
        return this;
    }

    public int getMaxWorldSize() {
        return 29999984;
    }

    public <V> ListenableFuture<V> callFromMainThread(Callable<V> callable) {
        Validate.notNull(callable);

        if (!this.isCallingFromMinecraftThread() && !this.isServerStopped()) {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callable);

            synchronized (this.futureTaskQueue) {
                this.futureTaskQueue.add(listenablefuturetask);
                return listenablefuturetask;
            }
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        Validate.notNull(runnableToSchedule);
        return this.callFromMainThread(Executors.callable(runnableToSchedule));
    }

    public boolean isCallingFromMinecraftThread() {
        return Thread.currentThread() == this.serverThread;
    }

    public int getNetworkCompressionThreshold() {
        return 256;
    }

    public int getSpawnRadius(@Nullable WorldServer worldIn) {
        return worldIn != null ? worldIn.getGameRules().getInt("spawnRadius") : 10;
    }

    public AdvancementManager getAdvancementManager() {
        return this.Worlds[0].getAdvancementManager();
    }

    public FunctionManager getFunctionManager() {
        return this.Worlds[0].getFunctionManager();
    }

    public void reload() {
        if (this.isCallingFromMinecraftThread()) {
            this.getPlayerList().saveAllPlayerData();
            this.Worlds[0].getLootTableManager().reloadLootTables();
            this.getAdvancementManager().reload();
            this.getFunctionManager().reload();
            this.getPlayerList().reloadResources();
        } else {
            this.addScheduledTask(this::reload);
        }
    }

    @SideOnly(Side.SERVER)
    public String getServerHostname() {
        return this.hostname;
    }

    @SideOnly(Side.SERVER)
    public void setHostname(String host) {
        this.hostname = host;
    }

    @SideOnly(Side.SERVER)
    public void registerTickable(ITickable tickable) {
        this.tickables.add(tickable);
    }

    @SideOnly(Side.SERVER)
    public void logInfo(String msg) {
        LOGGER.info(msg);
    }

    @SideOnly(Side.SERVER)
    public boolean isDebuggingEnabled() {
        return false;
    }

    @SideOnly(Side.SERVER)
    public void logSevere(String msg) {
        LOGGER.error(msg);
    }

    @SideOnly(Side.SERVER)
    public void logDebug(String msg) {
        if (this.isDebuggingEnabled()) {
            LOGGER.info(msg);
        }
    }

    @SideOnly(Side.SERVER)
    public int getServerPort() {
        return this.serverPort;
    }

    @SideOnly(Side.SERVER)
    public void setServerPort(int port) {
        this.serverPort = port;
    }

    @SideOnly(Side.SERVER)
    public int getSpawnProtectionSize() {
        return 16;
    }

    @SideOnly(Side.SERVER)
    public long getCurrentTime() {
        return this.currentTime;
    }

    @SideOnly(Side.SERVER)
    public Thread getServerThread() {
        return this.serverThread;
    }

    public DataFixer getDataFixer() {
        return this.dataFixer;
    }
}
