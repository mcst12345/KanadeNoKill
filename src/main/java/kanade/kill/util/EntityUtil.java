package kanade.kill.util;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.entity.EntityBeaconBeam;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.CoreDump;
import kanade.kill.network.packets.KillCurrentPlayer;
import kanade.kill.network.packets.KillEntity;
import kanade.kill.reflection.ReflectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.IEventListener;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.entity.Entity.FLAGS;

@SuppressWarnings("unused")
public class EntityUtil {

    public static final Set<Class<?>> banned = new HashSet<>();

    public static synchronized void kill(TileEntity tileEntity) {
        Util.killing = true;
        tileEntity.tileEntityInvalid = true;
        World world = tileEntity.getWorld();
        world.addedTileEntityList.remove(tileEntity);
        world.loadedTileEntityList.remove(tileEntity);
        if (tileEntity instanceof ITickable) {
            world.tickableTileEntities.remove(tileEntity);
        }
        Chunk chunk = world.getChunk(tileEntity.getPos());
        chunk.tileEntities.remove(tileEntity.getPos());
        chunk.markDirty();

        if (world.isRemote) {
            BlockPos blockpos1 = tileEntity.getPos();
            IBlockState iblockstate1 = world.getBlockState(blockpos1);
            world.notifyBlockUpdate(blockpos1, iblockstate1, iblockstate1, 2);
        }
        Util.killing = false;
    }
    public static final Set<UUID> blackHolePlayers = new HashSet<>();
    public static final Set<UUID> Dead = new HashSet<>();
    private static final Set<Class<?>> redefined = new HashSet<>();

    public static boolean holdKillItem(EntityPlayer player) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        return stack.getITEM() == ModMain.kill_item;
    }

    public static void summonThunder(Entity entity, int count) {
        World world = entity.WORLD;
        for (int i = 0; i < count; i++) {
            entity.WORLD.addWeatherEffect(new EntityLightningBolt(world, entity.X, entity.Y, entity.Z, true));
        }
    }

    public static synchronized void Kill(List<Entity> list) {
        Util.killing = true;
        for (Entity e : list) {
            if (KillItem.inList(e)) {
                continue;
            }
            Kill(e, false);
            if (e instanceof EntityLivingBase) {
                summonThunder(e, 4);
                Entity beacon_beam = new EntityBeaconBeam(e.WORLD);
                beacon_beam.X = e.X;
                beacon_beam.Y = 0;
                beacon_beam.Z = e.Z;
                beacon_beam.forceSpawn = true;
                e.WORLD.spawnEntity(beacon_beam);
            }
        }
        if (Config.fieldReset) {
            MinecraftForge.Event_bus.listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) ModMain.listeners;
            MinecraftForge.Event_bus.listenerOwners = (Map<Object, ModContainer>) ModMain.listenerOwners;
            ObjectUtil.ResetStatic();
            if (Config.SuperAttack) {
                if (Launch.Debug) {
                    Util.printStackTrace();
                }
                Launch.LOGGER.info("Fucking threads...");
                ThreadUtil.FuckThreads();
                Launch.LOGGER.info("Fucking objects...");
                NativeMethods.FuckObjects();
            }
        }
        if (Launch.client) {
            if (Minecraft.getMinecraft().toastGui != null && Minecraft.getMinecraft().toastGui.getClass() != GuiToast.class) {
                Minecraft.getMinecraft().toastGui = new GuiToast(Minecraft.getMinecraft());
            }
        }
        if (Launch.client) {
            for (WorldServer world : DimensionManager.getWorlds()) {
                for (Entity player : world.players) {
                    for (BlockPos pos : BlockPos.getAllInBox(player.getPosition().add(-10, -10, -10), player.getPosition().add(10, 10, 10))) {
                        if (!world.isAirBlock(pos)) {
                            IBlockState state = player.WORLD.getBlockState(pos);
                            if (ObjectUtil.FromModClass(state.getBlock())) {
                                String propertyString = (new DefaultStateMapper()).getPropertyString(state.getProperties());
                                ModelResourceLocation modelResourceLocation = new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), propertyString);
                                try {
                                    if (ModelLoaderRegistry.getModel(modelResourceLocation).getClass().getName().equals("net.minecraftforge.client.model.ModelLoader$WeightedRandomModel")) {
                                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            }
        }
        Util.killing = false;
    }

    //Thread safe :)
    //Maybe ......
    public static synchronized void SafeKill(Object entity, boolean reset) {
        if (!(entity instanceof Entity)) {
            return;
        }
        synchronized (kanade.kill.asm.hooks.MinecraftServer.futureTaskQueue) {
            kanade.kill.asm.hooks.MinecraftServer.AddTask(() -> Kill((Entity) entity, reset));
        }
    }


    @SuppressWarnings("unchecked")
    public static synchronized void Kill(Entity entity, boolean reset) {
        if (KillItem.inList(entity) || entity == null) return;
        try {
            MinecraftForge.Event_bus.unregister(entity);
        } catch (Throwable ignored) {
        }
        if (Launch.client) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.scheduledTasks.clear();
        }
        try {
            if (reset) {
                Util.killing = true;
            }
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
                for (WorldServer world : server.Worlds) {
                    if (world.customTeleporters != null) {
                        world.customTeleporters.clear();
                    }
                }
            }
            UUID uuid = entity.getUniqueID();
            if (uuid != null) {
                Dead.add(uuid);
                NativeMethods.DeadAdd(uuid.hashCode());
            }
            World world = entity.WORLD;
            if (world.entities.getClass() != KanadeArrayList.class) {
                world.entities = new KanadeArrayList<>(world.entities);
                world.loadedEntityList = world.entities;
            }
            world.entities.remove(entity);
            Chunk chunk = world.getChunk(entity.chunkCoordX, entity.chunkCoordZ);
            ClassInheritanceMultiMap<Entity>[] entityLists = chunk.entities;
            for (ClassInheritanceMultiMap<Entity> map : entityLists) {
                Map<Class<?>, List<Entity>> MAP = map.map;
                List<?> list = MAP.get(entity.getClass());
                if (list != null) {
                    list.remove(entity);
                }
            }
            chunk.markDirty();

            entity.isDead = true;
            entity.HatedByLife = true;
            entity.addedToChunk = false;
            if (entity instanceof EntityLivingBase) {
                DataParameter<Float> HEALTH = EntityLivingBase.HEALTH;
                entity.DataManager.set(HEALTH, 0.0f);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    player.Inventory = new InventoryPlayer(player);
                    player.enderChest = new InventoryEnderChest();
                    if (world.players.getClass() != KanadeArrayList.class) {
                        world.players = new KanadeArrayList<>(world.players);
                        world.playerEntities = world.players;
                    }
                    world.players.remove(player);
                    if (player instanceof EntityPlayerMP) {
                        NetworkHandler.INSTANCE.sendMessageToPlayer(new KillCurrentPlayer(), (EntityPlayerMP) player);
                        if (Config.coreDumpAttack) {
                            NetworkHandler.INSTANCE.sendMessageToPlayer(new CoreDump(), (EntityPlayerMP) player);
                        }
                    }
                }
            }
            for (IWorldEventListener listener : entity.WORLD.eventListeners) {
                if (entity instanceof EntityLiving && listener instanceof PathWorldListener) {
                    ((PathWorldListener) listener).navigations.remove(((EntityLiving) entity).getNavigator());
                }
            }
            if (world instanceof WorldServer) {
                EntityTracker tracker = ((WorldServer) entity.WORLD).getEntityTracker();
                tracker.untrack(entity);
            }
            if (!entity.WORLD.isRemote) {
                NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillEntity(entity.entityId, reset));
            }

            WorldInfo info = world.worldInfo;

            if (info != null) {
                if (info.playerTag != null) {
                    info.playerTag.tagMap.clear();
                }
                if (info.additionalProperties != null) {
                    info.additionalProperties.clear();
                }
                if (info.dimensionData != null) {
                    info.dimensionData.clear();
                }
            }
            if (reset) {
                if (Config.fieldReset) {
                    MinecraftForge.Event_bus.listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) ModMain.listeners;
                    MinecraftForge.Event_bus.listenerOwners = (Map<Object, ModContainer>) ModMain.listenerOwners;
                    ObjectUtil.ResetStatic();
                }
                if (Config.SuperAttack) {
                    if (Launch.Debug) {
                        Util.printStackTrace();
                    }
                    Launch.LOGGER.info("Fucking threads...");
                    ThreadUtil.FuckThreads();
                    Launch.LOGGER.info("Fucking objects...");
                    NativeMethods.FuckObjects();
                }
                Util.killing = false;
            }
            if (Config.redefineAttack) {
                ClassUtil.redefineClass(entity.getClass(), ClassUtil.generateClassBytes(((KanadeClassLoader) Launch.classLoader).untransformName(ReflectionUtil.getName(entity.getClass()))));
            }
        } catch (Throwable t) {
            Launch.LOGGER.fatal("The fuck?", t);
            if (reset) {
                Util.killing = false;
            }
        }
    }

    public static boolean isDead(Object obj) {
        if (obj == null) {
            return false;
        }
        if (NativeMethods.HaveDeadTag(obj)) {
            return true;
        }
        if (Launch.client) {
            if (obj instanceof Gui && Config.renderProtection) {
                return true;
            }
        }
        if (obj instanceof Entity) {
            if (banned.contains(obj.getClass())) {
                return true;
            }
            Entity entity = (Entity) obj;
            return Dead.contains(entity.getUniqueID()) || entity.getUniqueID() != null && NativeMethods.DeadContain(entity.getUniqueID().hashCode()) || NativeMethods.HaveDeadTag(entity);
        } else if (obj instanceof UUID) {
            return Dead.contains(obj) || NativeMethods.HaveDeadTag(obj.hashCode());
        }
        return false;
    }

    public static boolean invHaveKillItem(EntityPlayer player) {
        InventoryPlayer inventoryPlayer = player.Inventory;
        for (ItemStack stack : inventoryPlayer.armorInventory) {
            if (stack.getITEM() == ModMain.kill_item) {
                return true;
            }
        }
        for (ItemStack stack : inventoryPlayer.mainInventory) {
            if (stack.getITEM() == ModMain.kill_item) {
                return true;
            }
        }
        for (ItemStack stack : inventoryPlayer.offHandInventory) {
            if (stack.getITEM() == ModMain.kill_item) {
                return true;
            }
        }
        return false;
    }

    public static void updatePlayer(@Nonnull
                                    EntityPlayer player) {
        boolean blackhole = blackHolePlayers.contains(player.getUniqueID());
        player.getActivePotionEffects().clear();
        if (player.customEntityData != null) {
            player.customEntityData.setBoolean("isDead", false);
        }
        byte b0 = player.DataManager.get(FLAGS);
        player.DataManager.set(FLAGS, (byte) (b0 & ~(1 << 5)));
        player.hurtTime = 0;
        player.addedToChunk = true;
        player.capabilities.allowEdit = true;
        player.capabilities.allowFlying = true;
        player.setScore(Integer.MAX_VALUE);
        player.updateBlocked = false;
        player.isAddedToWorld = true;
        player.forceSpawn = true;
        World world = player.WORLD;
        //player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(5);
        player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE).setBaseValue(1024.D);
        if (world.players.getClass() != KanadeArrayList.class) {
            world.players = new KanadeArrayList<>(world.players);
        }
        if (!world.players.contains(player)) {
            world.players.add(player);
        }
        if (world.entities.getClass() != KanadeArrayList.class) {
            world.entities = new KanadeArrayList<>(world.entities);
        }
        if (!world.entities.contains(player)) {
            world.entities.add(player);
        }
        if (Config.particleEffect) {
            switch (Config.particleType){
                case 0: {
                    spawnParticle0(player,blackhole);
                    break;
                }
                case 1: {
                    spawnParticle1(player);
                    break;
                }
                case 2: {
                    spawnParticle2(player);
                    break;
                }
                case 3: {
                    spawnParticle3(player);
                    break;
                }
                case 4: {
                    spawnParticle4(player);
                    break;
                }
                case 5: {
                    ParticleUtil.drawWing(player);
                    break;
                }
            }
        }
        if (blackhole) {
            List<Entity> list = new ArrayList<>(world.entities);
            for (Entity e : list) {
                if (e != player) {
                    double dx = player.X - e.X;
                    double dy = player.Y - e.Y;
                    double dz = player.Z - e.Z;

                    double lensquared = dx * dx + dy * dy + dz * dz;
                    double len = Math.sqrt(lensquared);
                    double suckRange = Double.MAX_VALUE;
                    double lenn = len / suckRange;

                    if (len <= suckRange) {
                        double strength = (1 - lenn) * (1 - lenn);
                        double power = 0.5;

                        e.mX += (dx / len) * strength * power;
                        e.mY += (dy / len) * strength * power;
                        e.mZ += (dz / len) * strength * power;
                    }
                }
            }
        }
    }

    private static void spawnParticle0(EntityPlayer player, boolean blackhole){
        World world = player.WORLD;
        EnumParticleTypes type = blackhole ? EnumParticleTypes.PORTAL : EnumParticleTypes.ENCHANTMENT_TABLE;
        for (double x = player.X - 3d; x <= player.X + 3d; x += 0.1d) {
            world.spawnParticle(type, x, player.Y + 1d, player.Z + 4d, 0, 0, 0);
        }
        for (double x = player.X - 3d; x <= player.X + 3d; x += 0.1d) {
            world.spawnParticle(type, x, player.Y + 1d, player.Z - 4d, 0, 0, 0);
        }
        for (double z = player.Z - 3d; z <= player.Z + 3d; z += 0.1d) {
            world.spawnParticle(type, player.X + 4d, player.Y + 1d, z, 0, 0, 0);
        }
        for (double z = player.Z - 3d; z <= player.Z + 3d; z += 0.1d) {
            world.spawnParticle(type, player.X - 4d, player.Y + 1d, z, 0, 0, 0);
        }
        world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X + 4d, player.Y + 4d, player.Z + 4d, 0, 0, 0);
        world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X + 4d, player.Y + 4d, player.Z - 4d, 0, 0, 0);
        world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X - 4d, player.Y + 4d, player.Z + 4d, 0, 0, 0);
        world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X - 4d, player.Y + 4d, player.Z - 4d, 0, 0, 0);
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X + 1.5d, y, player.Z, 0, 0, 0);
        }
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X - 1.5d, y, player.Z, 0, 0, 0);
        }
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X + 1d, y, player.Z - 1d, 0, 0, 0);
        }
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X - 1d, y, player.Z + 1d, 0, 0, 0);
        }
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X + 1d, y, player.Z + 1d, 0, 0, 0);
        }
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X - 1d, y, player.Z - 1d, 0, 0, 0);
        }
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X, y, player.Z + 1.5d, 0, 0, 0);
        }
        for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
            world.spawnParticle(type, player.X, y, player.Z - 1.5d, 0, 0, 0);
        }
    }

    private static void spawnParticle1(EntityPlayer player){
        World world = player.WORLD;
        double X = player.X;
        double Z = player.Z;
        double y = player.Y;
        EnumParticleTypes type = EnumParticleTypes.END_ROD;
        for(int i = 0 ; i <= 360; i++){
            double tmp = i * 0.017453292519943295;
            double tmp1 = Math.cos(tmp)*Math.cos(tmp)*Math.cos(tmp);
            double tmp2 = Math.sin(tmp)*Math.sin(tmp)*Math.sin(tmp);
            world.spawnParticle(type,X + tmp1*2.5,y,Z + tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
            world.spawnParticle(type,X - tmp1*2.5,y,Z - tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
            world.spawnParticle(type,X + tmp1*2.5,y,Z - tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
            world.spawnParticle(type,X - tmp1*2.5,y,Z + tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
        }
        for(double d = y ; d <= y+3; d+=0.05D){
            world.spawnParticle(type,X+5,d,Z+5,0,1.0,0);
            world.spawnParticle(type,X+5,d,Z-5,0,1.0,0);
            world.spawnParticle(type,X-5,d,Z-5,0,1.0,0);
            world.spawnParticle(type,X-5,d,Z+5,0,1.0,0);
        }
    }

    private static void spawnParticle2(EntityPlayer player){
        World world = player.WORLD;
        double X = player.X;
        double Z = player.Z;
        double y = player.Y;
        EnumParticleTypes type = EnumParticleTypes.END_ROD;
        for(int i = 0 ; i <= 360; i++){
            double tmp = i * 0.017453292519943295;
            double tmp2 = Math.cosh(tmp)*Math.cosh(tmp)*Math.cosh(tmp);
            double tmp1 = Math.sinh(tmp)*Math.sinh(tmp)*Math.sinh(tmp);
            world.spawnParticle(type,X + tmp1*2.5,y,Z + tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
            world.spawnParticle(type,X + tmp2*2.5,y,Z + tmp1*2.5,tmp2*0.14514,0,tmp1*0.14514);
            world.spawnParticle(type,X - tmp1*2.5,y,Z - tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
            world.spawnParticle(type,X - tmp2*2.5,y,Z - tmp1*2.5,tmp2*0.14514,0,tmp1*0.14514);
            world.spawnParticle(type,X + tmp1*2.5,y,Z - tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
            world.spawnParticle(type,X + tmp2*2.5,y,Z - tmp1*2.5,tmp2*0.14514,0,tmp1*0.14514);
            world.spawnParticle(type,X - tmp1*2.5,y,Z + tmp2*2.5,tmp1*0.14514,0,tmp2*0.14514);
            world.spawnParticle(type,X - tmp2*2.5,y,Z + tmp1*2.5,tmp2*0.14514,0,tmp1*0.14514);
        }
        for(double d = y ; d <= y+3; d+=0.05D){
            world.spawnParticle(type,X+5,d,Z+5,0,1.0,0);
            world.spawnParticle(type,X+5,d,Z-5,0,1.0,0);
            world.spawnParticle(type,X-5,d,Z-5,0,1.0,0);
            world.spawnParticle(type,X-5,d,Z+5,0,1.0,0);
        }
    }

    private static void spawnParticle3(EntityPlayer player){
        World world = player.WORLD;
        double X = player.X;
        double Z = player.Z;
        double y = player.Y;
        EnumParticleTypes type = EnumParticleTypes.END_ROD;
        for(int i = 0 ; i <= 360; i++){
            double rad = i * 0.017453292519943295;
            double r = 3.0D;
            double x = r * Math.cos(rad);
            double z = r * Math.sin(rad);
            world.spawnParticle(type,X+x,y,Z+z,0,0,0);
            world.spawnParticle(type,X+x+3,y,Z+z+3,0,0,0);
            world.spawnParticle(type,X+x-3,y,Z+z-3,0,0,0);
            world.spawnParticle(type,X+x+3,y,Z+z-3,0,0,0);
            world.spawnParticle(type,X+x-3,y,Z+z+3,0,0,0);
        }
        for(double d = y ; d <= y+3; d+=0.05D){
            world.spawnParticle(type,X-5,d,Z-5,0,1.0,0);
            world.spawnParticle(type,X+5,d,Z+5,0,1.0,0);
            world.spawnParticle(type,X-5,d,Z+5,0,1.0,0);
            world.spawnParticle(type,X+5,d,Z-5,0,1.0,0);
        }
    }

    static int counter = 0;

    private static void spawnParticle4(EntityPlayer player){
        World world = player.WORLD;
        double X = player.X;
        double Z = player.Z;
        double y = player.Y + 0.1D;
        EnumParticleTypes type = EnumParticleTypes.ENCHANTMENT_TABLE;
        for(int i = 0 ; i <= 360; i++){
            double rad = i * 0.017453292519943295D;
            double r1 = 0.5D,r2 = 3.0D;
            double x1 = r1 * Math.cos(rad) , x2 = r2 * Math.cos(rad);
            double z1 = r1 * Math.sin(rad) , z2 = r2 * Math.sin(rad);

            double tmp = 6D/Math.sqrt(2D);

            world.spawnParticle(type,X+x1,y,Z+z1,0,0,0);
            world.spawnParticle(type,X+x2,y,Z+z2,0,0,0);

            world.spawnParticle(type,X+x1,y,Z+z1+6,0,0,0);
            world.spawnParticle(type,X+x2,y,Z+z2+6,0,0,0);

            world.spawnParticle(type,X+x1,y,Z+z1-6,0,0,0);
            world.spawnParticle(type,X+x2,y,Z+z2-6,0,0,0);

            world.spawnParticle(type,X+x1+6,y,Z+z1,0,0,0);
            world.spawnParticle(type,X+x2+6,y,Z+z2,0,0,0);

            world.spawnParticle(type,X+x1-6,y,Z+z1,0,0,0);
            world.spawnParticle(type,X+x2-6,y,Z+z2,0,0,0);

            world.spawnParticle(type,X+x1+tmp,y,Z+z1+tmp,0,0,0);
            world.spawnParticle(type,X+x2+tmp,y,Z+z2+tmp,0,0,0);

            world.spawnParticle(type,X+x1-tmp,y,Z+z1-tmp,0,0,0);
            world.spawnParticle(type,X+x2-tmp,y,Z+z2-tmp,0,0,0);

            world.spawnParticle(type,X+x1+tmp,y,Z+z1-tmp,0,0,0);
            world.spawnParticle(type,X+x2+tmp,y,Z+z2-tmp,0,0,0);

            world.spawnParticle(type,X+x1-tmp,y,Z+z1+tmp,0,0,0);
            world.spawnParticle(type,X+x2-tmp,y,Z+z2+tmp,0,0,0);
        }

        for(double i = 0.5; i <= 3; i+=0.05D){
            for(int d = 0; d <=360 ; d+=60){
                double rad = (d+counter) * 0.017453292519943295;
                double rad1 = (d+counter-30) * 0.017453292519943295;
                double rad2 = (d+counter+30) * 0.017453292519943295;
                double x = i * Math.cos(rad),x1 = i * Math.cos(rad1);
                double z = i * Math.sin(rad),z1 = i * Math.cos(rad2);
                double tmp = 6/Math.sqrt(2);
                world.spawnParticle(type,X+x,y,Z+z,0,0,0);
                world.spawnParticle(type,X+x+6,y,Z+z,0,0,0);
                world.spawnParticle(type,X+x-6,y,Z+z,0,0,0);
                world.spawnParticle(type,X+x,y,Z+z+6,0,0,0);
                world.spawnParticle(type,X+x,y,Z+z-6,0,0,0);
                world.spawnParticle(type,X+x+tmp,y,Z+z+tmp,0,0,0);
                world.spawnParticle(type,X+x-tmp,y,Z+z-tmp,0,0,0);
                world.spawnParticle(type,X+x+tmp,y,Z+z-tmp,0,0,0);
                world.spawnParticle(type,X+x-tmp,y,Z+z+tmp,0,0,0);
            }
        }
        counter++;
        if(counter >= 360){
            counter = 0;
        }
    }
}
