package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataCallableSave;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class WorldServer extends World implements IThreadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<NextTickListEntry> pendingTickListEntriesHashSet = Sets.newHashSet();
    protected final VillageSiege villageSiege = new VillageSiege(this);
    private final MinecraftServer server;
    private final EntityTracker entityTracker;
    private final PlayerChunkMap playerChunkMap;
    private final TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet = new TreeSet<>();
    private final List<NextTickListEntry> pendingTickListEntriesThisTick = Lists.newArrayList();
    private final Teleporter worldTeleporter;
    private final WorldEntitySpawner entitySpawner = new WorldEntitySpawner();
    private final ServerBlockEventList[] blockEventQueue = new ServerBlockEventList[]{new ServerBlockEventList(), new ServerBlockEventList()};
    public Map<UUID, Entity> entitiesByUuid = Maps.newHashMap();
    public boolean disableLevelSaving;
    public List<Teleporter> customTeleporters = new ArrayList<>();
    /**
     * Stores the recently processed (lighting) chunks
     */
    protected Set<ChunkPos> doneChunks = new java.util.HashSet<>();
    private boolean allPlayersSleeping;
    private int updateEntityTick;
    private int blockEventCacheIndex;

    public WorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn) {
        super(saveHandlerIn, info, net.minecraftforge.common.DimensionManager.createProviderFor(dimensionId), profilerIn, false);
        this.server = server;
        this.entityTracker = new EntityTracker(this);
        this.playerChunkMap = new PlayerChunkMap(this);
        // Guarantee the dimension ID was not reset by the provider
        int providerDim = this.provider.getDimension();
        this.provider.setWorld(this);
        this.provider.setDimension(providerDim);
        this.chunkProvider = this.createChunkProvider();
        perWorldStorage = new MapStorage(new net.minecraftforge.common.WorldSpecificSaveHandler(this, saveHandlerIn));
        this.worldTeleporter = new Teleporter(this);
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
        this.getWorldBorder().setSize(server.getMaxWorldSize());
        net.minecraftforge.common.DimensionManager.setWorld(dimensionId, this, server);
    }

    public World init() {
        this.mapStorage = new MapStorage(this.saveHandler);
        String s = VillageCollection.fileNameForProvider(this.provider);
        VillageCollection villagecollection = (VillageCollection) this.perWorldStorage.getOrLoadData(VillageCollection.class, s);

        if (villagecollection == null) {
            this.villageCollection = new VillageCollection(this);
            this.perWorldStorage.setData(s, this.villageCollection);
        } else {
            this.villageCollection = villagecollection;
            this.villageCollection.setWorldsForAll(this);
        }

        this.worldScoreboard = new ServerScoreboard(this.server);
        ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData) this.mapStorage.getOrLoadData(ScoreboardSaveData.class, "scoreboard");

        if (scoreboardsavedata == null) {
            scoreboardsavedata = new ScoreboardSaveData();
            this.mapStorage.setData("scoreboard", scoreboardsavedata);
        }

        scoreboardsavedata.setScoreboard(this.worldScoreboard);
        ((ServerScoreboard) this.worldScoreboard).addDirtyRunnable(new WorldSavedDataCallableSave(scoreboardsavedata));
        this.lootTable = new LootTableManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "loot_tables"));
        this.advancementManager = new AdvancementManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "advancements"));
        this.functionManager = new FunctionManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "functions"), this.server);
        this.getWorldBorder().setCenter(this.worldInfo.getBorderCenterX(), this.worldInfo.getBorderCenterZ());
        this.getWorldBorder().setDamageAmount(this.worldInfo.getBorderDamagePerBlock());
        this.getWorldBorder().setDamageBuffer(this.worldInfo.getBorderSafeZone());
        this.getWorldBorder().setWarningDistance(this.worldInfo.getBorderWarningDistance());
        this.getWorldBorder().setWarningTime(this.worldInfo.getBorderWarningTime());

        if (this.worldInfo.getBorderLerpTime() > 0L) {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize(), this.worldInfo.getBorderLerpTarget(), this.worldInfo.getBorderLerpTime());
        } else {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize());
        }

        this.initCapabilities();
        return this;
    }

    public void tick() {
        super.tick();

        if (this.getWorldInfo().isHardcoreModeEnabled() && this.getDifficulty() != EnumDifficulty.HARD) {
            this.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
        }

        this.provider.getBiomeProvider().cleanupCache();

        if (this.areAllPlayersAsleep()) {
            if (this.getGameRules().getBoolean("doDaylightCycle")) {
                long i = this.getWorldTime() + 24000L;
                this.setWorldTime(i - i % 24000L);
            }

            this.wakeAllPlayers();
        }

        this.profiler.startSection("mobSpawner");

        if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            this.entitySpawner.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
        }

        this.profiler.endStartSection("chunkSource");
        this.chunkProvider.tick();
        int j = this.calculateSkylightSubtracted(1.0F);

        if (j != this.getSkylightSubtracted()) {
            this.setSkylightSubtracted(j);
        }

        this.worldInfo.setWorldTotalTime(this.worldInfo.getWorldTotalTime() + 1L);

        if (this.getGameRules().getBoolean("doDaylightCycle")) {
            this.setWorldTime(this.getWorldTime() + 1L);
        }

        this.profiler.endStartSection("tickPending");
        this.tickUpdates(false);
        this.profiler.endStartSection("tickBlocks");
        this.updateBlocks();
        this.profiler.endStartSection("chunkMap");
        this.playerChunkMap.tick();
        this.profiler.endStartSection("village");
        this.villageCollection.tick();
        this.villageSiege.tick();
        this.profiler.endStartSection("portalForcer");
        this.worldTeleporter.removeStalePortalLocations(this.getTotalWorldTime());
        for (Teleporter tele : customTeleporters) {
            tele.removeStalePortalLocations(getTotalWorldTime());
        }
        this.profiler.endSection();
        this.sendQueuedBlockEvents();
    }

    @Nullable
    public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType creatureType, BlockPos pos) {
        List<Biome.SpawnListEntry> list = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
        list = net.minecraftforge.event.ForgeEventFactory.getPotentialSpawns(this, creatureType, pos, list);
        return list != null && !list.isEmpty() ? WeightedRandom.getRandomItem(this.rand, list) : null;
    }

    public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, Biome.SpawnListEntry spawnListEntry, BlockPos pos) {
        List<Biome.SpawnListEntry> list = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
        list = net.minecraftforge.event.ForgeEventFactory.getPotentialSpawns(this, creatureType, pos, list);
        return list != null && !list.isEmpty() && list.contains(spawnListEntry);
    }

    public void updateAllPlayersSleepingFlag() {
        this.allPlayersSleeping = false;

        if (!this.playerEntities.isEmpty()) {
            int i = 0;
            int j = 0;

            for (EntityPlayer entityplayer : this.playerEntities) {
                if (entityplayer.isSpectator()) {
                    ++i;
                } else if (entityplayer.isPlayerSleeping()) {
                    ++j;
                }
            }

            this.allPlayersSleeping = j > 0 && j >= this.playerEntities.size() - i;
        }
    }

    protected void wakeAllPlayers() {
        this.allPlayersSleeping = false;

        for (EntityPlayer entityplayer : this.playerEntities.stream().filter(EntityPlayer::isPlayerSleeping).collect(Collectors.toList())) {
            entityplayer.wakeUpPlayer(false, false, true);
        }

        if (this.getGameRules().getBoolean("doWeatherCycle")) {
            this.resetRainAndThunder();
        }
    }

    private void resetRainAndThunder() {
        this.provider.resetRainAndThunder();
    }

    public boolean areAllPlayersAsleep() {
        if (this.allPlayersSleeping && !this.isRemote) {
            for (EntityPlayer entityplayer : this.playerEntities) {
                if (!entityplayer.isSpectator() && !entityplayer.isPlayerFullyAsleep()) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public void setInitialSpawnLocation() {
        if (this.worldInfo.getSpawnY() <= 0) {
            this.worldInfo.setSpawnY(this.getSeaLevel() + 1);
        }

        int i = this.worldInfo.getSpawnX();
        int j = this.worldInfo.getSpawnZ();
        int k = 0;

        while (this.getGroundAboveSeaLevel(new BlockPos(i, 0, j)).getMaterial() == Material.AIR) {
            i += this.rand.nextInt(8) - this.rand.nextInt(8);
            j += this.rand.nextInt(8) - this.rand.nextInt(8);
            ++k;

            if (k == 10000) {
                break;
            }
        }

        this.worldInfo.setSpawnX(i);
        this.worldInfo.setSpawnZ(j);
    }

    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return this.getChunkProvider().chunkExists(x, z);
    }

    protected void playerCheckLight() {
        this.profiler.startSection("playerCheckLight");

        if (!this.playerEntities.isEmpty()) {
            int i = this.rand.nextInt(this.playerEntities.size());
            EntityPlayer entityplayer = this.playerEntities.get(i);
            int j = MathHelper.floor(entityplayer.posX) + this.rand.nextInt(11) - 5;
            int k = MathHelper.floor(entityplayer.posY) + this.rand.nextInt(11) - 5;
            int l = MathHelper.floor(entityplayer.posZ) + this.rand.nextInt(11) - 5;
            this.checkLight(new BlockPos(j, k, l));
        }

        this.profiler.endSection();
    }

    protected void updateBlocks() {
        this.playerCheckLight();

        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            Iterator<Chunk> iterator1 = this.playerChunkMap.getChunkIterator();

            while (iterator1.hasNext()) {
                iterator1.next().onTick(false);
            }
        } else {
            int i = this.getGameRules().getInt("randomTickSpeed");
            boolean flag = this.isRaining();
            boolean flag1 = this.isThundering();
            this.profiler.startSection("pollingChunks");

            for (Iterator<Chunk> iterator = getPersistentChunkIterable(this.playerChunkMap.getChunkIterator()); iterator.hasNext(); this.profiler.endSection()) {
                this.profiler.startSection("getChunk");
                Chunk chunk = iterator.next();
                int j = chunk.x * 16;
                int k = chunk.z * 16;
                this.profiler.endStartSection("checkNextLight");
                chunk.enqueueRelightChecks();
                this.profiler.endStartSection("tickChunk");
                chunk.onTick(false);
                this.profiler.endStartSection("thunder");

                if (this.provider.canDoLightning(chunk) && flag && flag1 && this.rand.nextInt(100000) == 0) {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int l = this.updateLCG >> 2;
                    BlockPos blockpos = this.adjustPosToNearbyEntity(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));

                    if (this.isRainingAt(blockpos)) {
                        DifficultyInstance difficultyinstance = this.getDifficultyForLocation(blockpos);

                        if (this.getGameRules().getBoolean("doMobSpawning") && this.rand.nextDouble() < (double) difficultyinstance.getAdditionalDifficulty() * 0.01D) {
                            EntitySkeletonHorse entityskeletonhorse = new EntitySkeletonHorse(this);
                            entityskeletonhorse.setTrap(true);
                            entityskeletonhorse.setGrowingAge(0);
                            entityskeletonhorse.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                            this.spawnEntity(entityskeletonhorse);
                            this.addWeatherEffect(new EntityLightningBolt(this, blockpos.getX(), blockpos.getY(), blockpos.getZ(), true));
                        } else {
                            this.addWeatherEffect(new EntityLightningBolt(this, blockpos.getX(), blockpos.getY(), blockpos.getZ(), false));
                        }
                    }
                }

                this.profiler.endStartSection("iceandsnow");

                if (this.provider.canDoRainSnowIce(chunk) && this.rand.nextInt(16) == 0) {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int j2 = this.updateLCG >> 2;
                    BlockPos blockpos1 = this.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
                    BlockPos blockpos2 = blockpos1.down();

                    if (this.isAreaLoaded(blockpos2, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
                        if (this.canBlockFreezeNoWater(blockpos2)) {
                            this.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
                        }

                    if (flag && this.canSnowAt(blockpos1, true)) {
                        this.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState());
                    }

                    if (flag && this.getBiome(blockpos2).canRain()) {
                        this.getBlockState(blockpos2).getBlock().fillWithRain(this, blockpos2);
                    }
                }

                this.profiler.endStartSection("tickBlocks");

                if (i > 0) {
                    for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray()) {
                        if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.needsRandomTick()) {
                            for (int i1 = 0; i1 < i; ++i1) {
                                this.updateLCG = this.updateLCG * 3 + 1013904223;
                                int j1 = this.updateLCG >> 2;
                                int k1 = j1 & 15;
                                int l1 = j1 >> 8 & 15;
                                int i2 = j1 >> 16 & 15;
                                IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
                                Block block = iblockstate.getBlock();
                                this.profiler.startSection("randomTick");

                                if (block.getTickRandomly()) {
                                    block.randomTick(this, new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), iblockstate, this.rand);
                                }

                                this.profiler.endSection();
                            }
                        }
                    }
                }
            }

            this.profiler.endSection();
        }
    }

    protected BlockPos adjustPosToNearbyEntity(BlockPos pos) {
        BlockPos blockpos = this.getPrecipitationHeight(pos);
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos, new BlockPos(blockpos.getX(), this.getHeight(), blockpos.getZ()))).grow(3.0D);
        List<EntityLivingBase> list = this.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb, p_apply_1_ -> p_apply_1_ != null && p_apply_1_.isEntityAlive() && WorldServer.this.canSeeSky(p_apply_1_.getPosition()));

        if (!list.isEmpty()) {
            return list.get(this.rand.nextInt(list.size())).getPosition();
        } else {
            if (blockpos.getY() == -1) {
                blockpos = blockpos.up(2);
            }

            return blockpos;
        }
    }

    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockType);
        return this.pendingTickListEntriesThisTick.contains(nextticklistentry);
    }

    public boolean isUpdateScheduled(BlockPos pos, Block blk) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blk);
        return this.pendingTickListEntriesHashSet.contains(nextticklistentry);
    }

    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
        this.updateBlockTick(pos, blockIn, delay, 0);
    }

    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
        Material material = blockIn.getDefaultState().getMaterial();

        if (this.scheduledUpdatesAreImmediate && material != Material.AIR) {
            if (blockIn.requiresUpdates()) {
                //Keeping here as a note for future when it may be restored.
                boolean isForced = getPersistentChunks().containsKey(new ChunkPos(pos));
                int range = isForced ? 0 : 8;
                if (this.isAreaLoaded(pos.add(-range, -range, -range), pos.add(range, range, range))) {
                    IBlockState iblockstate = this.getBlockState(pos);

                    if (iblockstate.getMaterial() != Material.AIR && iblockstate.getBlock() == blockIn) {
                        iblockstate.getBlock().updateTick(this, pos, iblockstate, this.rand);
                    }
                }

                return;
            }

            delay = 1;
        }

        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);

        if (this.isBlockLoaded(pos)) {
            if (material != Material.AIR) {
                nextticklistentry.setScheduledTime((long) delay + this.worldInfo.getWorldTotalTime());
                nextticklistentry.setPriority(priority);
            }

            if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
                this.pendingTickListEntriesHashSet.add(nextticklistentry);
                this.pendingTickListEntriesTreeSet.add(nextticklistentry);
            }
        }
    }

    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
        if (blockIn == null)
            return; //Forge: Prevent null blocks from ticking, can happen if blocks are removed in old worlds. TODO: Fix real issue causing block to be null.
        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
        nextticklistentry.setPriority(priority);
        Material material = blockIn.getDefaultState().getMaterial();

        if (material != Material.AIR) {
            nextticklistentry.setScheduledTime((long) delay + this.worldInfo.getWorldTotalTime());
        }

        if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
            this.pendingTickListEntriesHashSet.add(nextticklistentry);
            this.pendingTickListEntriesTreeSet.add(nextticklistentry);
        }
    }

    public void updateEntities() {
        if (this.playerEntities.isEmpty() && getPersistentChunks().isEmpty()) {
            if (this.updateEntityTick++ >= 300) {
                return;
            }
        } else {
            this.resetUpdateEntityTick();
        }

        this.provider.onWorldUpdateEntities();
        super.updateEntities();
    }

    public void tickPlayers() {
        super.tickPlayers();
        this.profiler.endStartSection("players");

        for (Entity entity : this.playerEntities) {
            Entity entity1 = entity.getRidingEntity();

            if (entity1 != null) {
                if (!entity1.isDead && entity1.isPassenger(entity)) {
                    continue;
                }

                entity.dismountRidingEntity();
            }

            this.profiler.startSection("tick");

            if (!entity.isDead) {
                try {
                    this.updateEntity(entity);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
                    entity.addEntityCrashInfo(crashreportcategory);
                    throw new ReportedException(crashreport);
                }
            }

            this.profiler.endSection();
            this.profiler.startSection("remove");

            if (entity.isDead) {
                int j = entity.chunkCoordX;
                int k = entity.chunkCoordZ;

                if (entity.addedToChunk && this.isChunkLoaded(j, k, true)) {
                    this.getChunk(j, k).removeEntity(entity);
                }

                this.loadedEntityList.remove(entity);
                this.onEntityRemoved(entity);
            }

            this.profiler.endSection();
        }
    }

    public void resetUpdateEntityTick() {
        this.updateEntityTick = 0;
    }

    public boolean tickUpdates(boolean runAllPending) {
        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            int i = this.pendingTickListEntriesTreeSet.size();

            if (i != this.pendingTickListEntriesHashSet.size()) {
                throw new IllegalStateException("TickNextTick list out of synch");
            } else {
                if (i > 65536) {
                    i = 65536;
                }

                this.profiler.startSection("cleaning");

                for (int j = 0; j < i; ++j) {
                    NextTickListEntry nextticklistentry = this.pendingTickListEntriesTreeSet.first();

                    if (!runAllPending && nextticklistentry.scheduledTime > this.worldInfo.getWorldTotalTime()) {
                        break;
                    }

                    this.pendingTickListEntriesTreeSet.remove(nextticklistentry);
                    this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                    this.pendingTickListEntriesThisTick.add(nextticklistentry);
                }

                this.profiler.endSection();
                this.profiler.startSection("ticking");
                Iterator<NextTickListEntry> iterator = this.pendingTickListEntriesThisTick.iterator();

                while (iterator.hasNext()) {
                    NextTickListEntry nextticklistentry1 = iterator.next();
                    iterator.remove();
                    //Keeping here as a note for future when it may be restored.
                    //boolean isForced = getPersistentChunks().containsKey(new ChunkPos(nextticklistentry.xCoord >> 4, nextticklistentry.zCoord >> 4));
                    //byte b0 = isForced ? 0 : 8;
                    int k = 0;

                    if (this.isAreaLoaded(nextticklistentry1.position.add(0, 0, 0), nextticklistentry1.position.add(0, 0, 0))) {
                        IBlockState iblockstate = this.getBlockState(nextticklistentry1.position);

                        if (iblockstate.getMaterial() != Material.AIR && Block.isEqualTo(iblockstate.getBlock(), nextticklistentry1.getBlock())) {
                            try {
                                iblockstate.getBlock().updateTick(this, nextticklistentry1.position, iblockstate, this.rand);
                            } catch (Throwable throwable) {
                                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while ticking a block");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                                CrashReportCategory.addBlockInfo(crashreportcategory, nextticklistentry1.position, iblockstate);
                                throw new ReportedException(crashreport);
                            }
                        }
                    } else {
                        this.scheduleUpdate(nextticklistentry1.position, nextticklistentry1.getBlock(), 0);
                    }
                }

                this.profiler.endSection();
                this.pendingTickListEntriesThisTick.clear();
                return !this.pendingTickListEntriesTreeSet.isEmpty();
            }
        }
    }

    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove) {
        ChunkPos chunkpos = chunkIn.getPos();
        int i = (chunkpos.x << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkpos.z << 4) - 2;
        int l = k + 16 + 2;
        return this.getPendingBlockUpdates(new StructureBoundingBox(i, 0, k, j, 256, l), remove);
    }

    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove) {
        List<NextTickListEntry> list = null;

        for (int i = 0; i < 2; ++i) {
            Iterator<NextTickListEntry> iterator;

            if (i == 0) {
                iterator = this.pendingTickListEntriesTreeSet.iterator();
            } else {
                iterator = this.pendingTickListEntriesThisTick.iterator();
            }

            while (iterator.hasNext()) {
                NextTickListEntry nextticklistentry = iterator.next();
                BlockPos blockpos = nextticklistentry.position;

                if (blockpos.getX() >= structureBB.minX && blockpos.getX() < structureBB.maxX && blockpos.getZ() >= structureBB.minZ && blockpos.getZ() < structureBB.maxZ) {
                    if (remove) {
                        if (i == 0) {
                            this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                        }

                        iterator.remove();
                    }

                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(nextticklistentry);
                }
            }
        }

        return list;
    }

    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
        if (!this.canSpawnAnimals() && (entityIn instanceof EntityAnimal || entityIn instanceof EntityWaterMob)) {
            entityIn.setDead();
        }

        if (!this.canSpawnNPCs() && entityIn instanceof INpc) {
            entityIn.setDead();
        }

        super.updateEntityWithOptionalForce(entityIn, forceUpdate);
    }

    private boolean canSpawnNPCs() {
        return this.server.getCanSpawnNPCs();
    }

    private boolean canSpawnAnimals() {
        return this.server.getCanSpawnAnimals();
    }

    protected IChunkProvider createChunkProvider() {
        IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
        return new ChunkProviderServer(this, ichunkloader, this.provider.createChunkGenerator());
    }

    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return super.isBlockModifiable(player, pos);
    }

    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return !this.server.isBlockProtected(this, pos, player) && this.getWorldBorder().contains(pos);
    }

    public void initialize(WorldSettings settings) {
        if (!this.worldInfo.isInitialized()) {
            try {
                this.createSpawnPosition(settings);

                if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                    this.setDebugWorldSettings();
                }

                super.initialize(settings);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception initializing level");

                try {
                    this.addWorldInfoToCrashReport(crashreport);
                } catch (Throwable ignored) {
                }

                throw new ReportedException(crashreport);
            }

            this.worldInfo.setServerInitialized(true);
        }
    }

    private void setDebugWorldSettings() {
        this.worldInfo.setMapFeaturesEnabled(false);
        this.worldInfo.setAllowCommands(true);
        this.worldInfo.setRaining(false);
        this.worldInfo.setThundering(false);
        this.worldInfo.setCleanWeatherTime(1000000000);
        this.worldInfo.setWorldTime(6000L);
        this.worldInfo.setGameType(GameType.SPECTATOR);
        this.worldInfo.setHardcore(false);
        this.worldInfo.setDifficulty(EnumDifficulty.PEACEFUL);
        this.worldInfo.setDifficultyLocked(true);
        this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
    }

    private void createSpawnPosition(WorldSettings settings) {
        if (!this.provider.canRespawnHere()) {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up(this.provider.getAverageGroundLevel()));
        } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
        } else {
            if (net.minecraftforge.event.ForgeEventFactory.onCreateWorldSpawn(this, settings)) return;
            this.findingSpawnPoint = true;
            BiomeProvider biomeprovider = this.provider.getBiomeProvider();
            List<Biome> list = biomeprovider.getBiomesToSpawnIn();
            Random random = new Random(this.getSeed());
            BlockPos blockpos = biomeprovider.findBiomePosition(0, 0, 256, list, random);
            int i = 8;
            int j = this.provider.getAverageGroundLevel();
            int k = 8;

            if (blockpos != null) {
                i = blockpos.getX();
                k = blockpos.getZ();
            } else {
                LOGGER.warn("Unable to find spawn biome");
            }

            int l = 0;

            while (!this.provider.canCoordinateBeSpawn(i, k)) {
                i += random.nextInt(64) - random.nextInt(64);
                k += random.nextInt(64) - random.nextInt(64);
                ++l;

                if (l == 1000) {
                    break;
                }
            }

            this.worldInfo.setSpawn(new BlockPos(i, j, k));
            this.findingSpawnPoint = false;

            if (settings.isBonusChestEnabled()) {
                this.createBonusChest();
            }
        }
    }

    protected void createBonusChest() {
        WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest();

        for (int i = 0; i < 10; ++i) {
            int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
            BlockPos blockpos = this.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();

            if (worldgeneratorbonuschest.generate(this, this.rand, blockpos)) {
                break;
            }
        }
    }

    @Nullable
    public BlockPos getSpawnCoordinate() {
        return this.provider.getSpawnCoordinate();
    }

    public void saveAllChunks(boolean all, @Nullable IProgressUpdate progressCallback) throws MinecraftException {
        ChunkProviderServer chunkproviderserver = this.getChunkProvider();

        if (chunkproviderserver.canSave()) {
            if (progressCallback != null) {
                progressCallback.displaySavingString("Saving level");
            }

            this.saveLevel();

            if (progressCallback != null) {
                progressCallback.displayLoadingString("Saving chunks");
            }

            chunkproviderserver.saveChunks(all);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Save(this));

            for (Chunk chunk : Lists.newArrayList(chunkproviderserver.getLoadedChunks())) {
                if (chunk != null && !this.playerChunkMap.contains(chunk.x, chunk.z)) {
                    chunkproviderserver.queueUnload(chunk);
                }
            }
        }
    }

    public void flushToDisk() {
        ChunkProviderServer chunkproviderserver = this.getChunkProvider();

        if (chunkproviderserver.canSave()) {
            chunkproviderserver.flushToDisk();
        }
    }

    protected void saveLevel() throws MinecraftException {
        this.checkSessionLock();

        for (WorldServer worldserver : this.server.Worlds) {
            if (worldserver instanceof WorldServerMulti) {
                ((WorldServerMulti) worldserver).saveAdditionalData();
            }
        }

        this.worldInfo.setBorderSize(this.getWorldBorder().getDiameter());
        this.worldInfo.getBorderCenterX(this.getWorldBorder().getCenterX());
        this.worldInfo.getBorderCenterZ(this.getWorldBorder().getCenterZ());
        this.worldInfo.setBorderSafeZone(this.getWorldBorder().getDamageBuffer());
        this.worldInfo.setBorderDamagePerBlock(this.getWorldBorder().getDamageAmount());
        this.worldInfo.setBorderWarningDistance(this.getWorldBorder().getWarningDistance());
        this.worldInfo.setBorderWarningTime(this.getWorldBorder().getWarningTime());
        this.worldInfo.setBorderLerpTarget(this.getWorldBorder().getTargetSize());
        this.worldInfo.setBorderLerpTime(this.getWorldBorder().getTimeUntilTarget());
        this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.server.getPlayerList().getHostPlayerData());
        this.mapStorage.saveAllData();
        this.perWorldStorage.saveAllData();
    }

    public boolean spawnEntity(Entity entityIn) {
        return this.canAddEntity(entityIn) && super.spawnEntity(entityIn);
    }

    public void loadEntities(Collection<Entity> entityCollection) {
        for (Entity entity : Lists.newArrayList(entityCollection)) {
            if (this.canAddEntity(entity) && !net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity, this))) {
                this.loadedEntityList.add(entity);
                this.onEntityAdded(entity);
            }
        }
    }

    private boolean canAddEntity(Entity entityIn) {
        if (entityIn.isDead) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityList.getKey(entityIn));
            return false;
        } else {
            UUID uuid = entityIn.getUniqueID();

            if (this.entitiesByUuid.containsKey(uuid)) {
                Entity entity = this.entitiesByUuid.get(uuid);

                if (this.unloadedEntityList.contains(entity)) {
                    this.unloadedEntityList.remove(entity);
                } else {
                    if (!(entityIn instanceof EntityPlayer)) {
                        LOGGER.warn("Keeping entity {} that already exists with UUID {}", EntityList.getKey(entity), uuid.toString());
                        return false;
                    }

                    LOGGER.warn("Force-added player with duplicate UUID {}", uuid.toString());
                }

                this.removeEntityDangerously(entity);
            }

            return true;
        }
    }

    public void onEntityAdded(Entity entityIn) {
        super.onEntityAdded(entityIn);
        this.entitiesById.addKey(entityIn.getEntityId(), entityIn);
        this.entitiesByUuid.put(entityIn.getUniqueID(), entityIn);
        Entity[] aentity = entityIn.getParts();

        if (aentity != null) {
            for (Entity entity : aentity) {
                this.entitiesById.addKey(entity.getEntityId(), entity);
            }
        }
    }

    public void onEntityRemoved(Entity entityIn) {
        super.onEntityRemoved(entityIn);
        this.entitiesById.removeObject(entityIn.getEntityId());
        this.entitiesByUuid.remove(entityIn.getUniqueID());
        Entity[] aentity = entityIn.getParts();

        if (aentity != null) {
            for (Entity entity : aentity) {
                this.entitiesById.removeObject(entity.getEntityId());
            }
        }
    }

    public boolean addWeatherEffect(Entity entityIn) {
        if (super.addWeatherEffect(entityIn)) {
            this.server.getPlayerList().sendToAllNearExcept(null, entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, this.provider.getDimension(), new SPacketSpawnGlobalEntity(entityIn));
            return true;
        } else {
            return false;
        }
    }

    public void setEntityState(Entity entityIn, byte state) {
        this.getEntityTracker().sendToTrackingAndSelf(entityIn, new SPacketEntityStatus(entityIn, state));
    }

    public ChunkProviderServer getChunkProvider() {
        return (ChunkProviderServer) super.getChunkProvider();
    }

    public Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean causesFire, boolean damagesTerrain) {
        Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, causesFire, damagesTerrain);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;
        explosion.doExplosionA();
        explosion.doExplosionB(false);

        if (!damagesTerrain) {
            explosion.clearAffectedBlockPositions();
        }

        for (EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                ((EntityPlayerMP) entityplayer).connection.sendPacket(new SPacketExplosion(x, y, z, strength, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(entityplayer)));
            }
        }

        return explosion;
    }

    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        BlockEventData blockeventdata = new BlockEventData(pos, blockIn, eventID, eventParam);

        for (BlockEventData blockeventdata1 : this.blockEventQueue[this.blockEventCacheIndex]) {
            if (blockeventdata1.equals(blockeventdata)) {
                return;
            }
        }

        this.blockEventQueue[this.blockEventCacheIndex].add(blockeventdata);
    }

    private void sendQueuedBlockEvents() {
        while (!this.blockEventQueue[this.blockEventCacheIndex].isEmpty()) {
            int i = this.blockEventCacheIndex;
            this.blockEventCacheIndex ^= 1;

            for (BlockEventData blockeventdata : this.blockEventQueue[i]) {
                if (this.fireBlockEvent(blockeventdata)) {
                    this.server.getPlayerList().sendToAllNearExcept(null, blockeventdata.getPosition().getX(), blockeventdata.getPosition().getY(), blockeventdata.getPosition().getZ(), 64.0D, this.provider.getDimension(), new SPacketBlockAction(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
                }
            }

            this.blockEventQueue[i].clear();
        }
    }

    private boolean fireBlockEvent(BlockEventData event) {
        IBlockState iblockstate = this.getBlockState(event.getPosition());
        return iblockstate.getBlock() == event.getBlock() && iblockstate.onBlockEventReceived(this, event.getPosition(), event.getEventID(), event.getEventParameter());
    }

    public void flush() {
        this.saveHandler.flush();
    }

    protected void updateWeather() {
        boolean flag = this.isRaining();
        super.updateWeather();

        if (this.prevRainingStrength != this.rainingStrength) {
            this.server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(7, this.rainingStrength), this.provider.getDimension());
        }

        if (this.prevThunderingStrength != this.thunderingStrength) {
            this.server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimension());
        }

        /* The function in use here has been replaced in order to only send the weather info to players in the correct dimension,
         * rather than to all players on the server. This is what causes the client-side rain, as the
         * client believes that it has started raining locally, rather than in another dimension.
         */
        if (flag != this.isRaining()) {
            if (flag) {
                this.server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(2, 0.0F), this.provider.getDimension());
            } else {
                this.server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(1, 0.0F), this.provider.getDimension());
            }

            this.server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(7, this.rainingStrength), this.provider.getDimension());
            this.server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimension());
        }
    }

    @Nullable
    public MinecraftServer getMinecraftServer() {
        return this.server;
    }

    public EntityTracker getEntityTracker() {
        return this.entityTracker;
    }

    public PlayerChunkMap getPlayerChunkMap() {
        return this.playerChunkMap;
    }

    public Teleporter getDefaultTeleporter() {
        return this.worldTeleporter;
    }

    public TemplateManager getStructureTemplateManager() {
        return this.saveHandler.getStructureTemplateManager();
    }

    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
        this.spawnParticle(particleType, false, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, particleArguments);
    }

    public void spawnParticle(EnumParticleTypes particleType, boolean longDistance, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
        SPacketParticles spacketparticles = new SPacketParticles(particleType, longDistance, (float) xCoord, (float) yCoord, (float) zCoord, (float) xOffset, (float) yOffset, (float) zOffset, (float) particleSpeed, numberOfParticles, particleArguments);

        for (EntityPlayer playerEntity : this.playerEntities) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) playerEntity;
            this.sendPacketWithinDistance(entityplayermp, longDistance, xCoord, yCoord, zCoord, spacketparticles);
        }
    }

    public void spawnParticle(EntityPlayerMP player, EnumParticleTypes particle, boolean longDistance, double x, double y, double z, int count, double xOffset, double yOffset, double zOffset, double speed, int... arguments) {
        Packet<?> packet = new SPacketParticles(particle, longDistance, (float) x, (float) y, (float) z, (float) xOffset, (float) yOffset, (float) zOffset, (float) speed, count, arguments);
        this.sendPacketWithinDistance(player, longDistance, x, y, z, packet);
    }

    private void sendPacketWithinDistance(EntityPlayerMP player, boolean longDistance, double x, double y, double z, Packet<?> packetIn) {
        BlockPos blockpos = player.getPosition();
        double d0 = blockpos.distanceSq(x, y, z);

        if (d0 <= 1024.0D || longDistance && d0 <= 262144.0D) {
            player.connection.sendPacket(packetIn);
        }
    }

    @Nullable
    public Entity getEntityFromUuid(UUID uuid) {
        return this.entitiesByUuid.get(uuid);
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
        return this.server.addScheduledTask(runnableToSchedule);
    }

    public boolean isCallingFromMinecraftThread() {
        return this.server.isCallingFromMinecraftThread();
    }

    @Nullable
    public BlockPos findNearestStructure(String structureName, BlockPos position, boolean findUnexplored) {
        return this.getChunkProvider().getNearestStructurePos(this, structureName, position, findUnexplored);
    }

    public AdvancementManager getAdvancementManager() {
        return this.advancementManager;
    }

    public FunctionManager getFunctionManager() {
        return this.functionManager;
    }

    public File getChunkSaveLocation() {
        return ((net.minecraft.world.chunk.storage.AnvilChunkLoader) getChunkProvider().chunkLoader).chunkSaveLocation;
    }

    static class ServerBlockEventList extends ArrayList<BlockEventData> {
        private ServerBlockEventList() {
        }
    }
}
