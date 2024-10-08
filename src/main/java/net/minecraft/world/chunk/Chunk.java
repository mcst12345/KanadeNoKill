package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Chunk implements net.minecraftforge.common.capabilities.ICapabilityProvider {
    public static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;
    private static final Logger LOGGER = LogManager.getLogger();
    private static ChunkPos populating = null; // keep track of cascading chunk generation during chunk population
    public final int x;
    public final int z;
    public ClassInheritanceMultiMap<Entity>[] entityLists;
    public ClassInheritanceMultiMap<Entity>[] entities;
    private final ExtendedBlockStorage[] storageArrays;
    private final byte[] blockBiomeArray;
    private final int[] precipitationHeightMap;
    private final boolean[] updateSkylightColumns;
    private final World world;
    private final int[] heightMap;
    public final Map<BlockPos, TileEntity> tileEntities;
    private final ConcurrentLinkedQueue<BlockPos> tileEntityPosQueue;
    private final net.minecraftforge.common.capabilities.CapabilityDispatcher capabilities;
    public boolean unloadQueued;
    private boolean loaded;
    private boolean isGapLightingUpdated;
    private boolean isTerrainPopulated;
    private boolean isLightPopulated;
    private boolean ticked;
    private boolean dirty;
    private boolean hasEntities;
    private long lastSaveTime;
    private int heightMapMinimum;
    private long inhabitedTime;
    private int queuedLightChecks;

    public Chunk(World worldIn, int x, int z) {
        this.storageArrays = new ExtendedBlockStorage[16];
        this.blockBiomeArray = new byte[256];
        this.precipitationHeightMap = new int[256];
        this.updateSkylightColumns = new boolean[256];
        this.tileEntities = Maps.newHashMap();
        this.queuedLightChecks = 4096;
        this.tileEntityPosQueue = Queues.newConcurrentLinkedQueue();
        this.entityLists = (ClassInheritanceMultiMap[]) (new ClassInheritanceMultiMap[16]);
        this.world = worldIn;
        this.x = x;
        this.z = z;
        this.heightMap = new int[256];

        for (int i = 0; i < this.entityLists.length; ++i) {
            this.entityLists[i] = new ClassInheritanceMultiMap(Entity.class);
        }

        Arrays.fill(this.precipitationHeightMap, -999);
        Arrays.fill(this.blockBiomeArray, (byte) -1);
        capabilities = net.minecraftforge.event.ForgeEventFactory.gatherCapabilities(this);
    }

    public Chunk(World worldIn, ChunkPrimer primer, int x, int z) {
        this(worldIn, x, z);
        int i = 256;
        boolean flag = worldIn.provider.hasSkyLight();

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 256; ++l) {
                    IBlockState iblockstate = primer.getBlockState(j, l, k);

                    if (iblockstate.getMaterial() != Material.AIR) {
                        int i1 = l >> 4;

                        if (this.storageArrays[i1] == NULL_BLOCK_STORAGE) {
                            this.storageArrays[i1] = new ExtendedBlockStorage(i1 << 4, flag);
                        }

                        this.storageArrays[i1].set(j, l & 15, k, iblockstate);
                    }
                }
            }
        }
    }

    public boolean isAtLocation(int x, int z) {
        return x == this.x && z == this.z;
    }

    public int getHeight(BlockPos pos) {
        return this.getHeightValue(pos.getX() & 15, pos.getZ() & 15);
    }

    public int getHeightValue(int x, int z) {
        return this.heightMap[z << 4 | x];
    }

    @Nullable
    private ExtendedBlockStorage getLastExtendedBlockStorage() {
        for (int i = this.storageArrays.length - 1; i >= 0; --i) {
            if (this.storageArrays[i] != NULL_BLOCK_STORAGE) {
                return this.storageArrays[i];
            }
        }

        return null;
    }

    public int getTopFilledSegment() {
        ExtendedBlockStorage extendedblockstorage = this.getLastExtendedBlockStorage();
        return extendedblockstorage == null ? 0 : extendedblockstorage.getYLocation();
    }

    public ExtendedBlockStorage[] getBlockStorageArray() {
        return this.storageArrays;
    }

    @SideOnly(Side.CLIENT)
    protected void generateHeightMap() {
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                for (int l = i + 16; l > 0; --l) {
                    IBlockState iblockstate = this.getBlockState(j, l - 1, k);

                    if (this.getBlockLightOpacity(j, l - 1, k) != 0) {
                        this.heightMap[k << 4 | j] = l;

                        if (l < this.heightMapMinimum) {
                            this.heightMapMinimum = l;
                        }

                        break;
                    }
                }
            }
        }

        this.dirty = true;
    }

    public void generateSkylightMap() {
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                for (int l = i + 16; l > 0; --l) {
                    if (this.getBlockLightOpacity(j, l - 1, k) != 0) {
                        this.heightMap[k << 4 | j] = l;

                        if (l < this.heightMapMinimum) {
                            this.heightMapMinimum = l;
                        }

                        break;
                    }
                }

                if (this.world.provider.hasSkyLight()) {
                    int k1 = 15;
                    int i1 = i + 16 - 1;

                    do {
                        int j1 = this.getBlockLightOpacity(j, i1, k);

                        if (j1 == 0 && k1 != 15) {
                            j1 = 1;
                        }

                        k1 -= j1;

                        if (k1 > 0) {
                            ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

                            if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                                extendedblockstorage.setSkyLight(j, i1 & 15, k, k1);
                                this.world.notifyLightSet(new BlockPos((this.x << 4) + j, i1, (this.z << 4) + k));
                            }
                        }

                        --i1;

                    } while (i1 > 0 && k1 > 0);
                }
            }
        }

        this.dirty = true;
    }

    private void propagateSkylightOcclusion(int x, int z) {
        this.updateSkylightColumns[x + z * 16] = true;
        this.isGapLightingUpdated = true;
    }

    private void recheckGaps(boolean onlyOne) {
        this.world.profiler.startSection("recheckGaps");

        if (this.world.isAreaLoaded(new BlockPos(this.x * 16 + 8, 0, this.z * 16 + 8), 16)) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    if (this.updateSkylightColumns[i + j * 16]) {
                        this.updateSkylightColumns[i + j * 16] = false;
                        int k = this.getHeightValue(i, j);
                        int l = this.x * 16 + i;
                        int i1 = this.z * 16 + j;
                        int j1 = Integer.MAX_VALUE;

                        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                            j1 = Math.min(j1, this.world.getChunksLowestHorizon(l + enumfacing.getXOffset(), i1 + enumfacing.getZOffset()));
                        }

                        this.checkSkylightNeighborHeight(l, i1, j1);

                        for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
                            this.checkSkylightNeighborHeight(l + enumfacing1.getXOffset(), i1 + enumfacing1.getZOffset(), k);
                        }

                        if (onlyOne) {
                            this.world.profiler.endSection();
                            return;
                        }
                    }
                }
            }

            this.isGapLightingUpdated = false;
        }

        this.world.profiler.endSection();
    }

    private void checkSkylightNeighborHeight(int x, int z, int maxValue) {
        int i = this.world.getHeight(new BlockPos(x, 0, z)).getY();

        if (i > maxValue) {
            this.updateSkylightNeighborHeight(x, z, maxValue, i + 1);
        } else if (i < maxValue) {
            this.updateSkylightNeighborHeight(x, z, i, maxValue + 1);
        }
    }

    private void updateSkylightNeighborHeight(int x, int z, int startY, int endY) {
        if (endY > startY && this.world.isAreaLoaded(new BlockPos(x, 0, z), 16)) {
            for (int i = startY; i < endY; ++i) {
                this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, i, z));
            }

            this.dirty = true;
        }
    }

    private void relightBlock(int x, int y, int z) {
        int i = this.heightMap[z << 4 | x] & 255;

        int j = Math.max(y, i);

        while (j > 0 && this.getBlockLightOpacity(x, j - 1, z) == 0) {
            --j;
        }

        if (j != i) {
            this.world.markBlocksDirtyVertical(x + this.x * 16, z + this.z * 16, j, i);
            this.heightMap[z << 4 | x] = j;
            int k = this.x * 16 + x;
            int l = this.z * 16 + z;

            if (this.world.provider.hasSkyLight()) {
                if (j < i) {
                    for (int j1 = j; j1 < i; ++j1) {
                        ExtendedBlockStorage extendedblockstorage2 = this.storageArrays[j1 >> 4];

                        if (extendedblockstorage2 != NULL_BLOCK_STORAGE) {
                            extendedblockstorage2.setSkyLight(x, j1 & 15, z, 15);
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, j1, (this.z << 4) + z));
                        }
                    }
                } else {
                    for (int i1 = i; i1 < j; ++i1) {
                        ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

                        if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                            extendedblockstorage.setSkyLight(x, i1 & 15, z, 0);
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, i1, (this.z << 4) + z));
                        }
                    }
                }

                int k1 = 15;

                while (j > 0 && k1 > 0) {
                    --j;
                    int i2 = this.getBlockLightOpacity(x, j, z);

                    if (i2 == 0) {
                        i2 = 1;
                    }

                    k1 -= i2;

                    if (k1 < 0) {
                        k1 = 0;
                    }

                    ExtendedBlockStorage extendedblockstorage1 = this.storageArrays[j >> 4];

                    if (extendedblockstorage1 != NULL_BLOCK_STORAGE) {
                        extendedblockstorage1.setSkyLight(x, j & 15, z, k1);
                    }
                }
            }

            int l1 = this.heightMap[z << 4 | x];
            int j2 = i;
            int k2 = l1;

            if (l1 < i) {
                j2 = l1;
                k2 = i;
            }

            if (l1 < this.heightMapMinimum) {
                this.heightMapMinimum = l1;
            }

            if (this.world.provider.hasSkyLight()) {
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                    this.updateSkylightNeighborHeight(k + enumfacing.getXOffset(), l + enumfacing.getZOffset(), j2, k2);
                }

                this.updateSkylightNeighborHeight(k, l, j2, k2);
            }

            this.dirty = true;
        }
    }

    public int getBlockLightOpacity(BlockPos pos) {
        return this.getBlockState(pos).getLightOpacity(this.world, pos);
    }

    private int getBlockLightOpacity(int x, int y, int z) {
        IBlockState state = this.getBlockState(x, y, z); //Forge: Can sometimes be called before we are added to the global world list. So use the less accurate one during that. It'll be recalculated later
        return !loaded ? state.getLightOpacity() : state.getLightOpacity(world, new BlockPos(this.x << 4 | x & 15, y, this.z << 4 | z & 15));
    }

    public IBlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public IBlockState getBlockState(final int x, final int y, final int z) {
        if (this.world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            IBlockState iblockstate = null;

            if (y == 60) {
                iblockstate = Blocks.BARRIER.getDefaultState();
            }

            if (y == 70) {
                iblockstate = ChunkGeneratorDebug.getBlockStateFor(x, z);
            }

            return iblockstate == null ? Blocks.AIR.getDefaultState() : iblockstate;
        } else {
            try {
                if (y >= 0 && y >> 4 < this.storageArrays.length) {
                    ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];

                    if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                        return extendedblockstorage.get(x & 15, y & 15, z & 15);
                    }
                }

                return Blocks.AIR.getDefaultState();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block state");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
                crashreportcategory.addDetail("Location", () -> CrashReportCategory.getCoordinateInfo(x, y, z));
                throw new ReportedException(crashreport);
            }
        }
    }

    @Nullable
    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= this.precipitationHeightMap[l] - 1) {
            this.precipitationHeightMap[l] = -999;
        }

        int i1 = this.heightMap[l];
        IBlockState iblockstate = this.getBlockState(pos);

        if (iblockstate == state) {
            return null;
        } else {
            Block block = state.getBlock();
            Block block1 = iblockstate.getBlock();
            int k1 = iblockstate.getLightOpacity(this.world, pos); // Relocate old light value lookup here, so that it is called before TE is removed.
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
            boolean flag = false;

            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
                if (block == Blocks.AIR) {
                    return null;
                }

                extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[j >> 4] = extendedblockstorage;
                flag = j >= i1;
            }

            extendedblockstorage.set(i, j & 15, k, state);

            //if (block1 != block)
            {
                if (!this.world.isRemote) {
                    if (block1 != block) //Only fire block breaks when the block changes.
                        block1.breakBlock(this.world, pos, iblockstate);
                    TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                        this.world.removeTileEntity(pos);
                } else if (block1.hasTileEntity(iblockstate)) {
                    TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state))
                        this.world.removeTileEntity(pos);
                }
            }

            if (extendedblockstorage.get(i, j & 15, k).getBlock() != block) {
                return null;
            } else {
                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int j1 = state.getLightOpacity(this.world, pos);

                    if (j1 > 0) {
                        if (j >= i1) {
                            this.relightBlock(i, j + 1, k);
                        }
                    } else if (j == i1 - 1) {
                        this.relightBlock(i, j, k);
                    }

                    if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(i, k);
                    }
                }

                // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
                if (!this.world.isRemote && block1 != block && (!this.world.captureBlockSnapshots || block.hasTileEntity(state))) {
                    block.onBlockAdded(this.world, pos, state);
                }

                if (block.hasTileEntity(state)) {
                    TileEntity tileentity1 = this.getTileEntity(pos, EnumCreateEntityType.CHECK);

                    if (tileentity1 == null) {
                        tileentity1 = block.createTileEntity(this.world, state);
                        this.world.setTileEntity(pos, tileentity1);
                    }

                    if (tileentity1 != null) {
                        tileentity1.updateContainingBlockInfo();
                    }
                }

                this.dirty = true;
                return iblockstate;
            }
        }
    }

    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            return this.canSeeSky(pos) ? type.defaultLightValue : 0;
        } else if (type == EnumSkyBlock.SKY) {
            return !this.world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(i, j & 15, k);
        } else {
            return type == EnumSkyBlock.BLOCK ? extendedblockstorage.getBlockLight(i, j & 15, k) : type.defaultLightValue;
        }
    }

    public void setLightFor(EnumSkyBlock type, BlockPos pos, int value) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.world.provider.hasSkyLight());
            this.storageArrays[j >> 4] = extendedblockstorage;
            this.generateSkylightMap();
        }

        this.dirty = true;

        if (type == EnumSkyBlock.SKY) {
            if (this.world.provider.hasSkyLight()) {
                extendedblockstorage.setSkyLight(i, j & 15, k, value);
            }
        } else if (type == EnumSkyBlock.BLOCK) {
            extendedblockstorage.setBlockLight(i, j & 15, k, value);
        }
    }

    public int getLightSubtracted(BlockPos pos, int amount) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];

        if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            return this.world.provider.hasSkyLight() && amount < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
        } else {
            int l = !this.world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(i, j & 15, k);
            l = l - amount;
            int i1 = extendedblockstorage.getBlockLight(i, j & 15, k);

            if (i1 > l) {
                l = i1;
            }

            return l;
        }
    }

    public void addEntity(Entity entityIn) {
        this.hasEntities = true;
        int i = MathHelper.floor(entityIn.posX / 16.0D);
        int j = MathHelper.floor(entityIn.posZ / 16.0D);

        if (i != this.x || j != this.z) {
            LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, this.x, this.z, entityIn);
            entityIn.setDead();
        }

        int k = MathHelper.floor(entityIn.posY / 16.0D);

        if (k < 0) {
            k = 0;
        }

        if (k >= this.entityLists.length) {
            k = this.entityLists.length - 1;
        }

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityEvent.EnteringChunk(entityIn, this.x, this.z, entityIn.chunkCoordX, entityIn.chunkCoordZ));
        entityIn.addedToChunk = true;
        entityIn.chunkCoordX = this.x;
        entityIn.chunkCoordY = k;
        entityIn.chunkCoordZ = this.z;
        this.entityLists[k].add(entityIn);
        this.markDirty(); // Forge - ensure chunks are marked to save after an entity add
    }

    public void removeEntity(Entity entityIn) {
        this.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
    }

    public void removeEntityAtIndex(Entity entityIn, int index) {
        if (index < 0) {
            index = 0;
        }

        if (index >= this.entityLists.length) {
            index = this.entityLists.length - 1;
        }

        this.entityLists[index].remove(entityIn);
        this.markDirty(); // Forge - ensure chunks are marked to save after entity removals
    }

    public boolean canSeeSky(BlockPos pos) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        return j >= this.heightMap[k << 4 | i];
    }

    @Nullable
    private TileEntity createNewTileEntity(BlockPos pos) {
        IBlockState iblockstate = this.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return !block.hasTileEntity(iblockstate) ? null : block.createTileEntity(this.world, iblockstate);
    }

    @Nullable
    public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType creationMode) {
        TileEntity tileentity = this.tileEntities.get(pos);

        if (tileentity != null && tileentity.isInvalid()) {
            tileEntities.remove(pos);
            tileentity = null;
        }

        if (tileentity == null) {
            if (creationMode == EnumCreateEntityType.IMMEDIATE) {
                tileentity = this.createNewTileEntity(pos);
                this.world.setTileEntity(pos, tileentity);
            } else if (creationMode == EnumCreateEntityType.QUEUED) {
                this.tileEntityPosQueue.add(pos.toImmutable());
            }
        }

        return tileentity;
    }

    public void addTileEntity(TileEntity tileEntityIn) {
        this.addTileEntity(tileEntityIn.getPos(), tileEntityIn);

        if (this.loaded) {
            this.world.addTileEntity(tileEntityIn);
        }
    }

    public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
        if (tileEntityIn.getWorld() != this.world) //Forge don't call unless it's changed, could screw up bad mods.
            tileEntityIn.setWorld(this.world);
        tileEntityIn.setPos(pos);

        if (this.getBlockState(pos).getBlock().hasTileEntity(this.getBlockState(pos))) {
            if (this.tileEntities.containsKey(pos)) {
                this.tileEntities.get(pos).invalidate();
            }

            tileEntityIn.validate();
            this.tileEntities.put(pos, tileEntityIn);
        }
    }

    public void removeTileEntity(BlockPos pos) {
        if (this.loaded) {
            TileEntity tileentity = this.tileEntities.remove(pos);

            if (tileentity != null) {
                tileentity.invalidate();
            }
        }
    }

    public void onLoad() {
        this.loaded = true;
        this.world.addTileEntities(this.tileEntities.values());

        for (ClassInheritanceMultiMap<Entity> classinheritancemultimap : this.entityLists) {
            this.world.loadEntities(com.google.common.collect.ImmutableList.copyOf(classinheritancemultimap));
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(this));
    }

    public void onUnload() {
        Arrays.stream(entityLists).forEach(multimap -> com.google.common.collect.Lists.newArrayList(multimap.getByClass(net.minecraft.entity.player.EntityPlayer.class)).forEach(player -> world.updateEntityWithOptionalForce(player, false))); // FORGE - Fix for MC-92916
        this.loaded = false;

        for (TileEntity tileentity : this.tileEntities.values()) {
            this.world.markTileEntityForRemoval(tileentity);
        }

        for (ClassInheritanceMultiMap<Entity> classinheritancemultimap : this.entityLists) {
            this.world.unloadEntities(classinheritancemultimap);
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(this));
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> filter) {
        int i = MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
        int j = MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
        i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
        j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

        for (int k = i; k <= j; ++k) {
            if (!this.entityLists[k].isEmpty()) {
                for (Entity entity : this.entityLists[k]) {
                    if (entity.getEntityBoundingBox().intersects(aabb) && entity != entityIn) {
                        if (filter == null || filter.apply(entity)) {
                            listToFill.add(entity);
                        }

                        Entity[] aentity = entity.getParts();

                        if (aentity != null) {
                            for (Entity entity1 : aentity) {
                                if (entity1 != entityIn && entity1.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(entity1))) {
                                    listToFill.add(entity1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
        int i = MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
        int j = MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
        i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
        j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

        for (int k = i; k <= j; ++k) {
            for (T t : this.entityLists[k].getByClass(entityClass)) {
                if (t.getEntityBoundingBox().intersects(aabb) && (filter == null || filter.apply(t))) {
                    listToFill.add(t);
                }
            }
        }
    }

    public boolean needsSaving(boolean p_76601_1_) {
        if (p_76601_1_) {
            if (this.hasEntities && this.world.getTotalWorldTime() != this.lastSaveTime || this.dirty) {
                return true;
            }
        } else if (this.hasEntities && this.world.getTotalWorldTime() >= this.lastSaveTime + 600L) {
            return true;
        }

        return this.dirty;
    }

    public Random getRandomWithSeed(long seed) {
        return new Random(this.world.getSeed() + (long) ((long) this.x * this.x * 4987142) + (long) (this.x * 5947611L) + (long) ((long) this.z * this.z) * 4392871L + (long) (this.z * 389711L) ^ seed);
    }

    public boolean isEmpty() {
        return false;
    }

    public void populate(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {
        Chunk chunk = chunkProvider.getLoadedChunk(this.x, this.z - 1);
        Chunk chunk1 = chunkProvider.getLoadedChunk(this.x + 1, this.z);
        Chunk chunk2 = chunkProvider.getLoadedChunk(this.x, this.z + 1);
        Chunk chunk3 = chunkProvider.getLoadedChunk(this.x - 1, this.z);

        if (chunk1 != null && chunk2 != null && chunkProvider.getLoadedChunk(this.x + 1, this.z + 1) != null) {
            this.populate(chunkGenrator);
        }

        if (chunk3 != null && chunk2 != null && chunkProvider.getLoadedChunk(this.x - 1, this.z + 1) != null) {
            chunk3.populate(chunkGenrator);
        }

        if (chunk != null && chunk1 != null && chunkProvider.getLoadedChunk(this.x + 1, this.z - 1) != null) {
            chunk.populate(chunkGenrator);
        }

        if (chunk != null && chunk3 != null) {
            Chunk chunk4 = chunkProvider.getLoadedChunk(this.x - 1, this.z - 1);

            if (chunk4 != null) {
                chunk4.populate(chunkGenrator);
            }
        }
    }

    protected void populate(IChunkGenerator generator) {
        if (populating != null && net.minecraftforge.common.ForgeModContainer.logCascadingWorldGeneration)
            logCascadingWorldGeneration();
        ChunkPos prev = populating;
        populating = this.getPos();
        if (this.isTerrainPopulated()) {
            if (generator.generateStructures(this, this.x, this.z)) {
                this.markDirty();
            }
        } else {
            this.checkLight();
            generator.populate(this.x, this.z);
            net.minecraftforge.fml.common.registry.GameRegistry.generateWorld(this.x, this.z, this.world, generator, this.world.getChunkProvider());
            this.markDirty();
        }
        populating = prev;
    }

    public BlockPos getPrecipitationHeight(BlockPos pos) {
        int i = pos.getX() & 15;
        int j = pos.getZ() & 15;
        int k = i | j << 4;
        BlockPos blockpos = new BlockPos(pos.getX(), this.precipitationHeightMap[k], pos.getZ());

        if (blockpos.getY() == -999) {
            int l = this.getTopFilledSegment() + 15;
            blockpos = new BlockPos(pos.getX(), l, pos.getZ());
            int i1 = -1;

            while (blockpos.getY() > 0 && i1 == -1) {
                IBlockState iblockstate = this.getBlockState(blockpos);
                Material material = iblockstate.getMaterial();

                if (!material.blocksMovement() && !material.isLiquid()) {
                    blockpos = blockpos.down();
                } else {
                    i1 = blockpos.getY() + 1;
                }
            }

            this.precipitationHeightMap[k] = i1;
        }

        return new BlockPos(pos.getX(), this.precipitationHeightMap[k], pos.getZ());
    }

    public void onTick(boolean skipRecheckGaps) {
        if (this.isGapLightingUpdated && this.world.provider.hasSkyLight() && !skipRecheckGaps) {
            this.recheckGaps(this.world.isRemote);
        }

        this.ticked = true;

        if (!this.isLightPopulated && this.isTerrainPopulated) {
            this.checkLight();
        }

        while (!this.tileEntityPosQueue.isEmpty()) {
            BlockPos blockpos = this.tileEntityPosQueue.poll();

            if (this.getTileEntity(blockpos, EnumCreateEntityType.CHECK) == null && this.getBlockState(blockpos).getBlock().hasTileEntity(this.getBlockState(blockpos))) {
                TileEntity tileentity = this.createNewTileEntity(blockpos);
                this.world.setTileEntity(blockpos, tileentity);
                this.world.markBlockRangeForRenderUpdate(blockpos, blockpos);
            }
        }
    }

    public boolean isPopulated() {
        return this.ticked && this.isTerrainPopulated && this.isLightPopulated;
    }

    public boolean wasTicked() {
        return this.ticked;
    }

    public ChunkPos getPos() {
        return new ChunkPos(this.x, this.z);
    }

    public boolean isEmptyBetween(int startY, int endY) {
        if (startY < 0) {
            startY = 0;
        }

        if (endY >= 256) {
            endY = 255;
        }

        for (int i = startY; i <= endY; i += 16) {
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[i >> 4];

            if (extendedblockstorage != NULL_BLOCK_STORAGE && !extendedblockstorage.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {
        if (this.storageArrays.length != newStorageArrays.length) {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", newStorageArrays.length, this.storageArrays.length);
        } else {
            System.arraycopy(newStorageArrays, 0, this.storageArrays, 0, this.storageArrays.length);
        }
    }

    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf, int availableSections, boolean groundUpContinuous) {
        for (TileEntity tileEntity : tileEntities.values()) {
            tileEntity.updateContainingBlockInfo();
            tileEntity.getBlockMetadata();
            tileEntity.getBlockType();
        }

        boolean flag = this.world.provider.hasSkyLight();

        for (int i = 0; i < this.storageArrays.length; ++i) {
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[i];

            if ((availableSections & 1 << i) == 0) {
                if (groundUpContinuous && extendedblockstorage != NULL_BLOCK_STORAGE) {
                    this.storageArrays[i] = NULL_BLOCK_STORAGE;
                }
            } else {
                if (extendedblockstorage == NULL_BLOCK_STORAGE) {
                    extendedblockstorage = new ExtendedBlockStorage(i << 4, flag);
                    this.storageArrays[i] = extendedblockstorage;
                }

                extendedblockstorage.getData().read(buf);
                buf.readBytes(extendedblockstorage.getBlockLight().getData());

                if (flag) {
                    buf.readBytes(extendedblockstorage.getSkyLight().getData());
                }
            }
        }

        if (groundUpContinuous) {
            buf.readBytes(this.blockBiomeArray);
        }

        for (int j = 0; j < this.storageArrays.length; ++j) {
            if (this.storageArrays[j] != NULL_BLOCK_STORAGE && (availableSections & 1 << j) != 0) {
                this.storageArrays[j].recalculateRefCounts();
            }
        }

        this.isLightPopulated = true;
        this.isTerrainPopulated = true;
        this.generateHeightMap();

        List<TileEntity> invalidList = new java.util.ArrayList<>();

        for (TileEntity tileentity : this.tileEntities.values()) {
            if (tileentity.shouldRefresh(this.world, tileentity.getPos(), tileentity.getBlockType().getStateFromMeta(tileentity.getBlockMetadata()), getBlockState(tileentity.getPos())))
                invalidList.add(tileentity);
            tileentity.updateContainingBlockInfo();
        }

        for (TileEntity te : invalidList) te.invalidate();
    }

    public Biome getBiome(BlockPos pos, BiomeProvider provider) {
        int i = pos.getX() & 15;
        int j = pos.getZ() & 15;
        int k = this.blockBiomeArray[j << 4 | i] & 255;

        if (k == 255) {
            // Forge: checking for client ensures that biomes are only generated on integrated server
            // in singleplayer. Generating biomes on the client may corrupt the biome ID arrays on
            // the server while they are being generated because IntCache can't be thread safe,
            // so client and server may end up filling the same array.
            // This is not necessary in 1.13 and newer versions.
            Biome biome = world.isRemote ? Biomes.PLAINS : provider.getBiome(pos, Biomes.PLAINS);
            k = Biome.getIdForBiome(biome);
            this.blockBiomeArray[j << 4 | i] = (byte) (k & 255);
        }

        Biome biome1 = Biome.getBiome(k);
        return biome1 == null ? Biomes.PLAINS : biome1;
    }

    public byte[] getBiomeArray() {
        return this.blockBiomeArray;
    }

    public void setBiomeArray(byte[] biomeArray) {
        if (this.blockBiomeArray.length != biomeArray.length) {
            LOGGER.warn("Could not set level chunk biomes, array length is {} instead of {}", biomeArray.length, this.blockBiomeArray.length);
        } else {
            System.arraycopy(biomeArray, 0, this.blockBiomeArray, 0, this.blockBiomeArray.length);
        }
    }

    public void resetRelightChecks() {
        this.queuedLightChecks = 0;
    }

    public void enqueueRelightChecks() {
        if (this.queuedLightChecks < 4096) {
            BlockPos blockpos = new BlockPos(this.x << 4, 0, this.z << 4);

            for (int i = 0; i < 8; ++i) {
                if (this.queuedLightChecks >= 4096) {
                    return;
                }

                int j = this.queuedLightChecks % 16;
                int k = this.queuedLightChecks / 16 % 16;
                int l = this.queuedLightChecks / 256;
                ++this.queuedLightChecks;

                for (int i1 = 0; i1 < 16; ++i1) {
                    BlockPos blockpos1 = blockpos.add(k, (j << 4) + i1, l);
                    boolean flag = i1 == 0 || i1 == 15 || k == 0 || k == 15 || l == 0 || l == 15;

                    if (this.storageArrays[j] == NULL_BLOCK_STORAGE && flag || this.storageArrays[j] != NULL_BLOCK_STORAGE && this.storageArrays[j].get(k, i1, l).getBlock().isAir(this.storageArrays[j].get(k, i1, l), this.world, blockpos1)) {
                        for (EnumFacing enumfacing : EnumFacing.values()) {
                            BlockPos blockpos2 = blockpos1.offset(enumfacing);

                            if (this.world.getBlockState(blockpos2).getLightValue(this.world, blockpos2) > 0) {
                                this.world.checkLight(blockpos2);
                            }
                        }

                        this.world.checkLight(blockpos1);
                    }
                }
            }
        }
    }

    public void checkLight() {
        this.isTerrainPopulated = true;
        this.isLightPopulated = true;
        BlockPos blockpos = new BlockPos(this.x << 4, 0, this.z << 4);

        if (this.world.provider.hasSkyLight()) {
            if (this.world.isAreaLoaded(blockpos.add(-1, 0, -1), blockpos.add(16, this.world.getSeaLevel(), 16))) {
                label44:

                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        if (!this.checkLight(i, j)) {
                            this.isLightPopulated = false;
                            break label44;
                        }
                    }
                }

                if (this.isLightPopulated) {
                    for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                        int k = enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16 : 1;
                        this.world.getChunk(blockpos.offset(enumfacing, k)).checkLightSide(enumfacing.getOpposite());
                    }

                    this.setSkylightUpdated();
                }
            } else {
                this.isLightPopulated = false;
            }
        }
    }

    private void setSkylightUpdated() {
        Arrays.fill(this.updateSkylightColumns, true);

        this.recheckGaps(false);
    }

    private void checkLightSide(EnumFacing facing) {
        if (this.isTerrainPopulated) {
            if (facing == EnumFacing.EAST) {
                for (int i = 0; i < 16; ++i) {
                    this.checkLight(15, i);
                }
            } else if (facing == EnumFacing.WEST) {
                for (int j = 0; j < 16; ++j) {
                    this.checkLight(0, j);
                }
            } else if (facing == EnumFacing.SOUTH) {
                for (int k = 0; k < 16; ++k) {
                    this.checkLight(k, 15);
                }
            } else if (facing == EnumFacing.NORTH) {
                for (int l = 0; l < 16; ++l) {
                    this.checkLight(l, 0);
                }
            }
        }
    }

    private boolean checkLight(int x, int z) {
        int i = this.getTopFilledSegment();
        boolean flag = false;
        boolean flag1 = false;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int j = i + 16 - 1; j > this.world.getSeaLevel() || j > 0 && !flag1; --j) {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
            int k = this.getBlockLightOpacity(blockpos$mutableblockpos);

            if (k == 255 && blockpos$mutableblockpos.getY() < this.world.getSeaLevel()) {
                flag1 = true;
            }

            if (!flag && k > 0) {
                flag = true;
            } else if (flag && k == 0 && !this.world.checkLight(blockpos$mutableblockpos)) {
                return false;
            }
        }

        for (int l = blockpos$mutableblockpos.getY(); l > 0; --l) {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

            if (this.getBlockState(blockpos$mutableblockpos).getLightValue(this.world, blockpos$mutableblockpos) > 0) {
                this.world.checkLight(blockpos$mutableblockpos);
            }
        }

        return true;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    @SideOnly(Side.CLIENT)
    public void markLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public World getWorld() {
        return this.world;
    }

    public int[] getHeightMap() {
        return this.heightMap;
    }

    public void setHeightMap(int[] newHeightMap) {
        if (this.heightMap.length != newHeightMap.length) {
            LOGGER.warn("Could not set level chunk heightmap, array length is {} instead of {}", newHeightMap.length, this.heightMap.length);
        } else {
            System.arraycopy(newHeightMap, 0, this.heightMap, 0, this.heightMap.length);
            this.heightMapMinimum = com.google.common.primitives.Ints.min(this.heightMap); // Forge: fix MC-117412
        }
    }

    public Map<BlockPos, TileEntity> getTileEntityMap() {
        return this.tileEntities;
    }

    public ClassInheritanceMultiMap<Entity>[] getEntityLists() {
        return this.entityLists;
    }

    public boolean isTerrainPopulated() {
        return this.isTerrainPopulated;
    }

    public void setTerrainPopulated(boolean terrainPopulated) {
        this.isTerrainPopulated = terrainPopulated;
    }

    public boolean isLightPopulated() {
        return this.isLightPopulated;
    }

    public void setLightPopulated(boolean lightPopulated) {
        this.isLightPopulated = lightPopulated;
    }

    public void setModified(boolean modified) {
        this.dirty = modified;
    }

    public void setHasEntities(boolean hasEntitiesIn) {
        this.hasEntities = hasEntitiesIn;
    }

    public void setLastSaveTime(long saveTime) {
        this.lastSaveTime = saveTime;
    }

    public int getLowestHeight() {
        return this.heightMapMinimum;
    }

    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    /* ======================================== FORGE START =====================================*/

    public void setInhabitedTime(long newInhabitedTime) {
        this.inhabitedTime = newInhabitedTime;
    }

    /**
     * Removes the tile entity at the specified position, only if it's
     * marked as invalid.
     */
    public void removeInvalidTileEntity(BlockPos pos) {
        if (loaded) {
            TileEntity entity = tileEntities.get(pos);
            if (entity != null && entity.isInvalid()) {
                tileEntities.remove(pos);
            }
        }
    }

    private void logCascadingWorldGeneration() {
        net.minecraftforge.fml.common.ModContainer activeModContainer = net.minecraftforge.fml.common.Loader.instance().activeModContainer();
        String format = "{} loaded a new chunk {} in dimension {} ({}) while populating chunk {}, causing cascading worldgen lag.";

        if (activeModContainer == null) { // vanilla minecraft has problems too (MC-114332), log it at a quieter level.
            net.minecraftforge.fml.common.FMLLog.log.debug(format, "Minecraft", this.getPos(), this.world.provider.getDimension(), this.world.provider.getDimensionType().getName(), populating);
            net.minecraftforge.fml.common.FMLLog.log.debug("Consider setting 'fixVanillaCascading' to 'true' in the Forge config to fix many cases where this occurs in the base game.");
        } else {
            net.minecraftforge.fml.common.FMLLog.log.warn(format, activeModContainer.getName(), this.getPos(), this.world.provider.getDimension(), this.world.provider.getDimensionType().getName(), populating);
            net.minecraftforge.fml.common.FMLLog.log.warn("Please report this to the mod's issue tracker. This log can be disabled in the Forge config.");
        }
    }

    @Nullable
    public net.minecraftforge.common.capabilities.CapabilityDispatcher getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable EnumFacing facing) {
        return capabilities != null && capabilities.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable EnumFacing facing) {
        return capabilities == null ? null : capabilities.getCapability(capability, facing);
    }

    public static enum EnumCreateEntityType {
        IMMEDIATE,
        QUEUED,
        CHECK
    }
}
