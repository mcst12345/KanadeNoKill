package net.minecraft.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class WorldProvider {
    public static final float[] MOON_PHASE_FACTORS = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public World world;
    private WorldType terrainType;
    private String generatorSettings;
    protected BiomeProvider biomeProvider;
    protected boolean doesWaterVaporize;
    protected boolean nether;
    protected boolean hasSkyLight;
    protected final float[] lightBrightnessTable = new float[16];
    private final float[] colorsSunriseSunset = new float[4];

    public final void setWorld(World worldIn) {
        this.world = worldIn;
        this.terrainType = worldIn.getWorldInfo().getTerrainType();
        this.generatorSettings = worldIn.getWorldInfo().getGeneratorOptions();
        this.init();
        this.generateLightBrightnessTable();
    }

    protected void generateLightBrightnessTable() {
        float f = 0.0F;

        for (int i = 0; i <= 15; ++i) {
            float f1 = 1.0F - (float) i / 15.0F;
            this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F + 0.0F;
        }
    }

    protected void init() {
        this.hasSkyLight = true;
        this.biomeProvider = this.terrainType.getBiomeProvider(world);
    }

    public IChunkGenerator createChunkGenerator() {
        return this.terrainType.getChunkGenerator(world, generatorSettings);
    }

    public boolean canCoordinateBeSpawn(int x, int z) {
        BlockPos blockpos = new BlockPos(x, 0, z);

        if (this.world.getBiome(blockpos).ignorePlayerSpawnSuitability()) {
            return true;
        } else {
            return this.world.getGroundAboveSeaLevel(blockpos).getBlock() == Blocks.GRASS;
        }
    }

    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        int i = (int) (worldTime % 24000L);
        float f = ((float) i + partialTicks) / 24000.0F - 0.25F;

        if (f < 0.0F) {
            ++f;
        }

        if (f > 1.0F) {
            --f;
        }

        float f1 = 1.0F - (float) ((Math.cos((double) f * Math.PI) + 1.0D) / 2.0D);
        f = f + (f1 - f) / 3.0F;
        return f;
    }

    public int getMoonPhase(long worldTime) {
        return (int) (worldTime / 24000L % 8L + 8L) % 8;
    }

    public boolean isSurfaceWorld() {
        return true;
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        float f = 0.4F;
        float f1 = MathHelper.cos(celestialAngle * ((float) Math.PI * 2F)) - 0.0F;
        float f2 = -0.0F;

        if (f1 >= -0.4F && f1 <= 0.4F) {
            float f3 = (f1 - -0.0F) / 0.4F * 0.5F + 0.5F;
            float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * (float) Math.PI)) * 0.99F;
            f4 = f4 * f4;
            this.colorsSunriseSunset[0] = f3 * 0.3F + 0.7F;
            this.colorsSunriseSunset[1] = f3 * f3 * 0.7F + 0.2F;
            this.colorsSunriseSunset[2] = f3 * f3 * 0.0F + 0.2F;
            this.colorsSunriseSunset[3] = f4;
            return this.colorsSunriseSunset;
        } else {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
        float f = MathHelper.cos(p_76562_1_ * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        float f1 = 0.7529412F;
        float f2 = 0.84705883F;
        float f3 = 1.0F;
        f1 = f1 * (f * 0.94F + 0.06F);
        f2 = f2 * (f * 0.94F + 0.06F);
        f3 = f3 * (f * 0.91F + 0.09F);
        return new Vec3d((double) f1, (double) f2, (double) f3);
    }

    public boolean canRespawnHere() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        return this.terrainType.getCloudHeight();
    }

    @SideOnly(Side.CLIENT)
    public boolean isSkyColored() {
        return true;
    }

    @Nullable
    public BlockPos getSpawnCoordinate() {
        return null;
    }

    public int getAverageGroundLevel() {
        return this.terrainType.getMinimumSpawnHeight(this.world);
    }

    @SideOnly(Side.CLIENT)
    public double getVoidFogYFactor() {
        return this.terrainType.voidFadeMagnitude();
    }

    @SideOnly(Side.CLIENT)
    public boolean doesXZShowFog(int x, int z) {
        return false;
    }

    public BiomeProvider getBiomeProvider() {
        return this.biomeProvider;
    }

    public boolean doesWaterVaporize() {
        return this.doesWaterVaporize;
    }

    public boolean hasSkyLight() {
        return this.hasSkyLight;
    }

    public boolean isNether() {
        return this.nether;
    }

    public float[] getLightBrightnessTable() {
        return this.lightBrightnessTable;
    }

    public WorldBorder createWorldBorder() {
        return new WorldBorder();
    }

    /*======================================= Forge Start =========================================*/
    private net.minecraftforge.client.IRenderHandler skyRenderer = null;
    private net.minecraftforge.client.IRenderHandler cloudRenderer = null;
    private net.minecraftforge.client.IRenderHandler weatherRenderer = null;
    private int dimensionId;

    /**
     * Sets the providers current dimension ID, used in default getSaveFolder()
     * Added to allow default providers to be registered for multiple dimensions.
     * This is to denote the exact dimension ID opposed to the 'type' in WorldType
     *
     * @param dim Dimension ID
     */
    public void setDimension(int dim) {
        this.dimensionId = dim;
    }

    public int getDimension() {
        return this.dimensionId;
    }

    /**
     * Returns the sub-folder of the world folder that this WorldProvider saves to.
     * EXA: DIM1, DIM-1
     *
     * @return The sub-folder name to save this world's chunks to.
     */
    @Nullable
    public String getSaveFolder() {
        return (dimensionId == 0 ? null : "DIM" + dimensionId);
    }

    /**
     * The dimension's movement factor.
     * Whenever a player or entity changes dimension from world A to world B, their coordinates are multiplied by
     * worldA.provider.getMovementFactor() / worldB.provider.getMovementFactor()
     * Example: Overworld factor is 1, nether factor is 8. Traveling from overworld to nether multiplies coordinates by 1/8.
     *
     * @return The movement factor
     */
    public double getMovementFactor() {
        if (this instanceof WorldProviderHell) {
            return 8.0;
        }
        return 1.0;
    }

    /**
     * If this method returns true, then chunks received by the client will
     * have {@link net.minecraft.world.chunk.Chunk#resetRelightChecks} called
     * on them, queuing lighting checks for all air blocks in the chunk (and
     * any adjacent light-emitting blocks).
     * <p>
     * Returning true here is recommended if the chunk generator used also
     * does this for newly generated chunks.
     *
     * @return true if lighting checks should be performed
     */
    public boolean shouldClientCheckLighting() {
        return !(this instanceof WorldProviderSurface);
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public net.minecraftforge.client.IRenderHandler getSkyRenderer() {
        return this.skyRenderer;
    }

    @SideOnly(Side.CLIENT)
    public void setSkyRenderer(net.minecraftforge.client.IRenderHandler skyRenderer) {
        this.skyRenderer = skyRenderer;
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public net.minecraftforge.client.IRenderHandler getCloudRenderer() {
        return cloudRenderer;
    }

    @SideOnly(Side.CLIENT)
    public void setCloudRenderer(net.minecraftforge.client.IRenderHandler renderer) {
        cloudRenderer = renderer;
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public net.minecraftforge.client.IRenderHandler getWeatherRenderer() {
        return weatherRenderer;
    }

    @SideOnly(Side.CLIENT)
    public void setWeatherRenderer(net.minecraftforge.client.IRenderHandler renderer) {
        weatherRenderer = renderer;
    }

    /**
     * Allows for manipulating the coloring of the lightmap texture.
     * Will be called for each 16*16 combination of sky/block light values.
     *
     * @param partialTicks  Progress between ticks.
     * @param sunBrightness Current sun brightness.
     * @param skyLight      Sky light brightness factor.
     * @param blockLight    Block light brightness factor.
     * @param colors        The color values that will be used: [r, g, b].
     * @see net.minecraft.client.renderer.EntityRenderer#updateLightmap(float)
     */
    public void getLightmapColors(float partialTicks, float sunBrightness, float skyLight, float blockLight, float[] colors) {
    }

    public BlockPos getRandomizedSpawnPoint() {
        BlockPos ret = this.world.getSpawnPoint();

        boolean isAdventure = world.getWorldInfo().getGameType() == GameType.ADVENTURE;
        int spawnFuzz = this.world instanceof WorldServer ? terrainType.getSpawnFuzz((WorldServer) this.world, this.world.getMinecraftServer()) : 1;
        int border = MathHelper.floor(world.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if (border < spawnFuzz) spawnFuzz = border;

        if (!isNether() && !isAdventure && spawnFuzz != 0) {
            if (spawnFuzz < 2) spawnFuzz = 2;
            int spawnFuzzHalf = spawnFuzz / 2;
            ret = world.getTopSolidOrLiquidBlock(ret.add(spawnFuzzHalf - world.rand.nextInt(spawnFuzz), 0, spawnFuzzHalf - world.rand.nextInt(spawnFuzz)));
        }

        return ret;
    }

    /**
     * Determine if the cursor on the map should 'spin' when rendered, like it does for the player in the nether.
     *
     * @param entity   The entity holding the map, playername, or frame-ENTITYID
     * @param x        X Position
     * @param z        Z Position
     * @param rotation the regular rotation of the marker
     * @return True to 'spin' the cursor
     */
    public boolean shouldMapSpin(String entity, double x, double z, double rotation) {
        return dimensionId < 0;
    }

    /**
     * Determines the dimension the player will be respawned in, typically this brings them back to the overworld.
     *
     * @param player The player that is respawning
     * @return The dimension to respawn the player in
     */
    public int getRespawnDimension(EntityPlayerMP player) {
        return player.getSpawnDimension();
    }

    /**
     * Called from {@link World#initCapabilities()}, to gather capabilities for this world.
     * It's safe to access world here since this is called after world is registered.
     * <p>
     * On server, called directly after mapStorage and world data such as Scoreboard and VillageCollection are initialized.
     * On client, called when world is constructed, just before world load event is called.
     * Note that this method is always called before the world load event.
     *
     * @return initial holder for capabilities on the world
     */
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities() {
        return null;
    }

    /**
     * Called on the client to get the music type to play when in this world type.
     * At the time of calling, the client player and world are guaranteed to be non-null
     *
     * @return null to use vanilla logic, otherwise a MusicType to play in this world
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    public net.minecraft.client.audio.MusicTicker.MusicType getMusicType() {
        return null;
    }

    /**
     * Determines if the player can sleep in this world (or if the bed should explode for example).
     *
     * @param player The player that is attempting to sleep
     * @param pos    The location where the player tries to sleep at (the position of the clicked on bed for example)
     * @return the result of a player trying to sleep at the given location
     */
    public WorldSleepResult canSleepAt(net.minecraft.entity.player.EntityPlayer player, BlockPos pos) {
        return (this.canRespawnHere() && this.world.getBiome(pos) != Biomes.HELL) ? WorldSleepResult.ALLOW : WorldSleepResult.BED_EXPLODES;
    }

    public Vec3d getSkyColor0(Entity cameraEntity, float partialTicks) {
        return null;
    }

    public static enum WorldSleepResult {
        ALLOW,
        DENY,
        BED_EXPLODES;
    }

    /*======================================= Start Moved From World =========================================*/

    public Biome getBiomeForCoords(BlockPos pos) {
        return world.getBiomeForCoordsBody(pos);
    }

    public boolean isDaytime() {
        return world.getSkylightSubtracted() < 4;
    }

    /**
     * The current sun brightness factor for this dimension.
     * 0.0f means no light at all, and 1.0f means maximum sunlight.
     * This will be used for the "calculateSkylightSubtracted"
     * which is for Sky light value calculation.
     *
     * @return The current brightness factor
     */
    public float getSunBrightnessFactor(float par1) {
        return world.getSunBrightnessFactor(par1);
    }

    /**
     * Calculates the current moon phase factor.
     * This factor is effective for slimes.
     * (This method do not affect the moon rendering)
     */
    public float getCurrentMoonPhaseFactor() {
        return world.getCurrentMoonPhaseFactorBody();
    }

    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        return world.getSkyColorBody(cameraEntity, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColor(float partialTicks) {
        return world.getCloudColorBody(partialTicks);
    }

    /**
     * Gets the Sun Brightness for rendering sky.
     */
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1) {
        return world.getSunBrightnessBody(par1);
    }

    /**
     * Gets the Star Brightness for rendering sky.
     */
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1) {
        return world.getStarBrightnessBody(par1);
    }

    public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
        world.spawnHostileMobs = allowHostile;
        world.spawnPeacefulMobs = allowPeaceful;
    }

    public void calculateInitialWeather() {
        world.calculateInitialWeatherBody();
    }

    public void updateWeather() {
        world.updateWeatherBody();
    }

    public boolean canBlockFreeze(BlockPos pos, boolean byWater) {
        return world.canBlockFreezeBody(pos, byWater);
    }

    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        return world.canSnowAtBody(pos, checkLight);
    }

    public void setWorldTime(long time) {
        world.worldInfo.setWorldTime(time);
    }

    public long getSeed() {
        return world.worldInfo.getSeed();
    }

    public long getWorldTime() {
        return world.worldInfo.getWorldTime();
    }

    public BlockPos getSpawnPoint() {
        net.minecraft.world.storage.WorldInfo info = world.worldInfo;
        return new BlockPos(info.getSpawnX(), info.getSpawnY(), info.getSpawnZ());
    }

    public void setSpawnPoint(BlockPos pos) {
        world.worldInfo.setSpawn(pos);
    }

    public boolean canMineBlock(net.minecraft.entity.player.EntityPlayer player, BlockPos pos) {
        return world.canMineBlockBody(player, pos);
    }

    public boolean isBlockHighHumidity(BlockPos pos) {
        return world.getBiome(pos).isHighHumidity();
    }

    public int getHeight() {
        return 256;
    }

    public int getActualHeight() {
        return nether ? 128 : 256;
    }

    public double getHorizon() {
        return world.worldInfo.getTerrainType().getHorizon(world);
    }

    public void resetRainAndThunder() {
        world.worldInfo.setRainTime(0);
        world.worldInfo.setRaining(false);
        world.worldInfo.setThunderTime(0);
        world.worldInfo.setThundering(false);
    }

    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) {
        return true;
    }

    public void onPlayerAdded(EntityPlayerMP player) {
    }

    public void onPlayerRemoved(EntityPlayerMP player) {
    }

    public abstract DimensionType getDimensionType();

    public void onWorldSave() {
    }

    public void onWorldUpdateEntities() {
    }

    public boolean canDropChunk(int x, int z) {
        return true;
    }
}
