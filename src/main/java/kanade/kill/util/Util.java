package kanade.kill.util;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.asm.Transformer;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.CoreDump;
import kanade.kill.network.packets.KillCurrentPlayer;
import kanade.kill.network.packets.KillEntity;
import kanade.kill.reflection.LateFields;
import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.thread.DisplayGui;
import kanade.kill.thread.FieldSaveThread;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import scala.concurrent.util.Unsafe;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "raw"})
public class Util {
    public static final List<Runnable> tasks = new ArrayList<>();
    private static final Set<UUID> Dead = new HashSet<>();
    public static final Map<Field, Object> cache = new HashMap<>();
    public static final Map<Field, Object> cache2 = new HashMap<>();
    private static Object saved_listeners;
    private static Object saved_listeners_2;
    public static boolean killing;
    private static final Object2LongOpenHashMap<Field> offsetCache = new Object2LongOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Field, Object> baseCache = new Object2ObjectOpenHashMap<>();

    public static synchronized void Kill(List<Entity> list) {
        for (Entity e : list) {
            Kill(e);
        }
        reset();
    }

    @SuppressWarnings("unchecked")
    public static synchronized void Kill(Entity entity) {
        if (KillItem.inList(entity) || entity == null) return;
        try {
            killing = true;
            UUID uuid = entity.getUniqueID();
            if (uuid != null) {
                Dead.add(uuid);
                NativeMethods.DeadAdd(uuid.hashCode());
            }
            World world = entity.world;
            if (world.entities.getClass() != ArrayList.class) {
                Unsafe.instance.putObjectVolatile(world, LateFields.loadedEntityList_offset, new ArrayList<>(world.entities));
            }
            world.entities.remove(entity);
            Chunk chunk = world.getChunk(entity.chunkCoordX, entity.chunkCoordZ);
            ClassInheritanceMultiMap<Entity>[] entityLists = (ClassInheritanceMultiMap<Entity>[]) Unsafe.instance.getObjectVolatile(chunk, LateFields.entities_offset);
            for (ClassInheritanceMultiMap<Entity> map : entityLists) {
                map.remove(entity);
            }
            chunk.markDirty();

            entity.isDead = true;
            Unsafe.instance.putObjectVolatile(entity, LateFields.HatedByLife_offset, true);
            entity.addedToChunk = false;
            if (entity instanceof EntityLivingBase) {
                DataParameter<Float> HEALTH = (DataParameter<Float>) Unsafe.instance.getObjectVolatile(LateFields.HEALTH_base, LateFields.HEALTH_offset);
                ((EntityDataManager) Unsafe.instance.getObjectVolatile(entity, LateFields.dataManager_offset)).set(HEALTH, 0.0f);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    player.Inventory = new InventoryPlayer(player);
                    player.enderChest = new InventoryEnderChest();
                    if (world.players.getClass() != ArrayList.class) {
                        Unsafe.instance.putObjectVolatile(world, LateFields.playerEntities_offset, new ArrayList<>(world.players));
                    }
                    world.players.remove(player);
                    if (Launch.client) {
                        if (Objects.equals(player.getUniqueID(), Minecraft.getMinecraft().PLAYER.getUniqueID())) {
                            DisplayGui.display();
                        }
                    }
                    if (player instanceof EntityPlayerMP) {
                        NetworkHandler.INSTANCE.sendMessageToPlayer(new KillCurrentPlayer(), (EntityPlayerMP) player);
                        if (Config.coreDumpAttack) {
                            NetworkHandler.INSTANCE.sendMessageToPlayer(new CoreDump(), (EntityPlayerMP) player);
                        }
                    }
                }
            }
            for (IWorldEventListener listener : entity.world.eventListeners) {
                if (entity instanceof EntityLiving && listener instanceof PathWorldListener) {
                    ((PathWorldListener) listener).navigations.remove(((EntityLiving) entity).getNavigator());
                }
                if (world instanceof WorldServer && listener instanceof ServerWorldEventHandler) {
                    EntityTracker tracker = ((WorldServer) entity.world).getEntityTracker();
                    tracker.untrack(entity);
                }
            }
            if (!entity.world.isRemote) {
                NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillEntity(entity.entityId));
            }
            reset();
            killing = false;
        } catch (Throwable t) {
            Launch.LOGGER.fatal(t);
            killing = false;
            throw new RuntimeException(t);
        }
    }

    public static boolean isDead(Entity entity) {
        return entity == null || Dead.contains(entity.getUniqueID()) || (entity.getUniqueID() != null && NativeMethods.DeadContain(entity.getUniqueID().hashCode())) || NativeMethods.HaveDeadTag(entity);
    }

    public static boolean NoRemove(Object item) {
        return item == ModMain.kill_item || item == ModMain.death_item || (item instanceof ItemStack && NoRemove(((ItemStack) item).getITEM()));
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

    public static void updatePlayer(EntityPlayer player) {
        player.hurtTime = 0;
        player.addedToChunk = true;
        player.capabilities.allowEdit = true;
        player.capabilities.allowFlying = true;
        player.setScore(Integer.MAX_VALUE);
        player.updateBlocked = false;
        player.isAddedToWorld = true;
        player.forceSpawn = true;
        World world = player.world;
        if (world.players.getClass() != ArrayList.class) {
            world.players = new ArrayList<>(world.players);
        }
        if (!world.players.contains(player)) {
            world.players.add(player);
        }
        if (world.entities.getClass() != ArrayList.class) {
            world.entities = new ArrayList<>(world.entities);
        }
        if (!world.entities.contains(player)) {
            world.entities.add(player);
        }
    }


    public static Object clone(Object o, int depth) {
        if (o == null) {
            return null;
        }
        if (o instanceof Class) {
            return o;
        }
        if (depth > 20000) {
            Launch.LOGGER.info("Too deep.");
            return o;
        }
        Object copy;
        long offset;
        if (o instanceof int[]) {
            int length = Array.getLength(o);
            int base = Unsafe.instance.arrayBaseOffset(int[].class);
            int scale = Unsafe.instance.arrayIndexScale(int[].class);

            copy = new int[length];

            for (int i = 0; i < length; i++) {
                long address = ((long) i * scale) + base;
                Unsafe.instance.putIntVolatile(copy, address, Unsafe.instance.getIntVolatile(o, address));
            }
            return copy;
        } else {
            if (o instanceof float[]) {
                int length = Array.getLength(o);
                int base = Unsafe.instance.arrayBaseOffset(float[].class);
                int scale = Unsafe.instance.arrayIndexScale(float[].class);

                copy = new float[length];

                for (int i = 0; i < length; i++) {
                    long address = ((long) i * scale) + base;
                    Unsafe.instance.putFloatVolatile(copy, address, Unsafe.instance.getFloatVolatile(o, address));
                }
                return copy;
            } else {
                if (o instanceof double[]) {
                    int length = Array.getLength(o);
                    int base = Unsafe.instance.arrayBaseOffset(double[].class);
                    int scale = Unsafe.instance.arrayIndexScale(double[].class);

                    copy = new double[length];

                    for (int i = 0; i < length; i++) {
                        long address = ((long) i * scale) + base;
                        Unsafe.instance.putDoubleVolatile(copy, address, Unsafe.instance.getDoubleVolatile(o, address));
                    }
                    return copy;
                } else {
                    if (o instanceof long[]) {
                        int length = Array.getLength(o);
                        int base = Unsafe.instance.arrayBaseOffset(long[].class);
                        int scale = Unsafe.instance.arrayIndexScale(long[].class);

                        copy = new long[length];

                        for (int i = 0; i < length; i++) {
                            long address = ((long) i * scale) + base;
                            Unsafe.instance.putLongVolatile(copy, address, Unsafe.instance.getLongVolatile(o, address));
                        }
                        return copy;
                    } else {
                        if (o instanceof short[]) {
                            int length = Array.getLength(o);
                            int base = Unsafe.instance.arrayBaseOffset(short[].class);
                            int scale = Unsafe.instance.arrayIndexScale(short[].class);

                            copy = new short[length];

                            for (int i = 0; i < length; i++) {
                                long address = ((long) i * scale) + base;
                                Unsafe.instance.putShortVolatile(copy, address, Unsafe.instance.getShortVolatile(o, address));
                            }
                            return copy;
                        } else {
                            if (o instanceof boolean[]) {
                                int length = Array.getLength(o);
                                int base = Unsafe.instance.arrayBaseOffset(boolean[].class);
                                int scale = Unsafe.instance.arrayIndexScale(boolean[].class);

                                copy = new boolean[length];

                                for (int i = 0; i < length; i++) {
                                    long address = ((long) i * scale) + base;
                                    Unsafe.instance.putBooleanVolatile(copy, address, Unsafe.instance.getBooleanVolatile(o, address));
                                }
                                return copy;
                            } else {
                                if (o instanceof char[]) {
                                    int length = Array.getLength(o);
                                    int base = Unsafe.instance.arrayBaseOffset(char[].class);
                                    int scale = Unsafe.instance.arrayIndexScale(char[].class);

                                    copy = new char[length];

                                    for (int i = 0; i < length; i++) {
                                        long address = ((long) i * scale) + base;
                                        Unsafe.instance.putCharVolatile(copy, address, Unsafe.instance.getCharVolatile(o, address));
                                    }
                                    return copy;
                                } else {
                                    if (o instanceof byte[]) {
                                        int length = Array.getLength(o);
                                        int base = Unsafe.instance.arrayBaseOffset(byte[].class);
                                        int scale = Unsafe.instance.arrayIndexScale(byte[].class);

                                        copy = new byte[length];

                                        for (int i = 0; i < length; i++) {
                                            long address = ((long) i * scale) + base;
                                            Unsafe.instance.putByteVolatile(copy, address, Unsafe.instance.getByteVolatile(o, address));
                                        }
                                        return copy;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (o.getClass().isArray()) {
            try {
                int length = Array.getLength(o);
                int scale = Unsafe.instance.arrayIndexScale(o.getClass());
                int base = Unsafe.instance.arrayBaseOffset(o.getClass());
                copy = Array.newInstance(o.getClass().getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    long address = ((long) i * scale) + base;
                    Unsafe.instance.putObjectVolatile(copy, address, Unsafe.instance.getObjectVolatile(o, address));
                }
                return copy;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                copy = Unsafe.instance.allocateInstance(o.getClass());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        for (Field field : ReflectionUtil.getAllFields(o.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (offsetCache.containsKey(field)) {
                offset = offsetCache.getLong(field);
            } else {
                offset = Unsafe.instance.objectFieldOffset(field);
                offsetCache.put(field, offset);
            }
            switch (field.getType().getName()) {
                case "int": {
                    Unsafe.instance.putIntVolatile(copy, offset, Unsafe.instance.getIntVolatile(o, offset));
                    break;
                }
                case "float": {
                    Unsafe.instance.putFloatVolatile(copy, offset, Unsafe.instance.getFloatVolatile(o, offset));
                    break;
                }
                case "double": {
                    Unsafe.instance.putDoubleVolatile(copy, offset, Unsafe.instance.getDoubleVolatile(o, offset));
                    break;
                }
                case "long": {
                    Unsafe.instance.putLongVolatile(copy, offset, Unsafe.instance.getLongVolatile(o, offset));
                    break;
                }
                case "short": {
                    Unsafe.instance.putShortVolatile(copy, offset, Unsafe.instance.getShortVolatile(o, offset));
                    break;
                }
                case "boolean": {
                    Unsafe.instance.putBooleanVolatile(copy, offset, Unsafe.instance.getBooleanVolatile(o, offset));
                    break;
                }
                case "char": {
                    Unsafe.instance.putCharVolatile(copy, offset, Unsafe.instance.getCharVolatile(o, offset));
                    break;
                }
                case "byte": {
                    Unsafe.instance.putByteVolatile(copy, offset, Unsafe.instance.getByteVolatile(o, offset));
                    break;
                }
                default: {
                    Launch.LOGGER.info("Coping field:" + field.getName() + ":" + field.getType().getName());
                    Object obj = Unsafe.instance.getObjectVolatile(o, offset);
                    Unsafe.instance.putObjectVolatile(copy, offset, clone(obj, depth + 1));
                }
            }
        }
        return copy;
    }

    public synchronized static void save() {
        Launch.LOGGER.info("Coping event listeners in EventBus.");
        saved_listeners = clone(Unsafe.instance.getObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset), 0);
        Launch.LOGGER.info("Coping static fields in event listeners.");
        for (String s : Transformer.getEventListeners()) {
            try {
                Class<?> clazz = Class.forName(s);

                Launch.LOGGER.info("Listener:" + s);
                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + getStatic(field));
                        try {
                            Object o = getStatic(field);
                            cache.put(field, clone(o, 0));
                        } catch (Throwable t) {
                            if (t instanceof StackOverflowError) {
                                Launch.LOGGER.warn("Too deep. Ignoring this field.");
                            } else {
                                throw new RuntimeException(t);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        Launch.LOGGER.info("Coping event listeners in Event.");
        saved_listeners_2 = clone(Unsafe.instance.getObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2), 0);
        Launch.LOGGER.info("Coping static fields in mod instances.");
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                Launch.LOGGER.info("Mod:" + container.getModId());
                Class<?> clazz = container.getMod().getClass();
                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + getStatic(field));
                        try {
                            Object object = getStatic(field);

                            cache.put(field, clone(object, 0));
                        } catch (Throwable t) {
                            if (t instanceof StackOverflowError) {
                                Launch.LOGGER.warn("Too deep. Ignoring this field.");
                            } else {
                                throw new RuntimeException(t);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        Launch.LOGGER.info("Saving fields which the transformer found.");
        while (!Transformer.getFields().isEmpty()) {
            FieldInfo fieldinfo = (FieldInfo) Transformer.getFields().poll();
            Field field = fieldinfo.toField();
            if (field == null || cache.containsKey(field)) {
                continue;
            }
            Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + getStatic(field));
            try {
                cache2.put(field, clone(getStatic(field), 0));
            } catch (Throwable t) {
                if (t instanceof StackOverflowError) {
                    Launch.LOGGER.warn("Too deep. Ignoring this field.");
                } else {
                    throw new RuntimeException(t);
                }
            }
        }

        FieldSaveThread thread = new FieldSaveThread();
        thread.start();
    }

    public synchronized static Object getStatic(Field field) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = Unsafe.instance.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.getLong(field);
        } else {
            offset = Unsafe.instance.staticFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                return Unsafe.instance.getIntVolatile(base, offset);
            }
            case "float": {
                return Unsafe.instance.getFloatVolatile(base, offset);
            }
            case "double": {
                return Unsafe.instance.getDoubleVolatile(base, offset);
            }
            case "long": {
                return Unsafe.instance.getLongVolatile(base, offset);
            }
            case "short": {
                return Unsafe.instance.getShortVolatile(base, offset);
            }
            case "boolean": {
                return Unsafe.instance.getBooleanVolatile(base, offset);
            }
            case "char": {
                return Unsafe.instance.getCharVolatile(base, offset);
            }
            case "byte": {
                return Unsafe.instance.getByteVolatile(base, offset);
            }
            default: {
                return Unsafe.instance.getObjectVolatile(base, Unsafe.instance.staticFieldOffset(field));
            }
        }

    }

    public synchronized static void putStatic(Field field, Object obj) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = Unsafe.instance.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.getLong(field);
        } else {
            offset = Unsafe.instance.staticFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                Unsafe.instance.putIntVolatile(base, offset, (int) obj);
                break;
            }
            case "float": {
                Unsafe.instance.putFloatVolatile(base, offset, (float) obj);
                break;
            }
            case "double": {
                Unsafe.instance.putDoubleVolatile(base, offset, (double) obj);
                break;
            }
            case "long": {
                Unsafe.instance.putLongVolatile(base, offset, (long) obj);
                break;
            }
            case "short": {
                Unsafe.instance.putShortVolatile(base, offset, (short) obj);
                break;
            }
            case "boolean": {
                Unsafe.instance.putBooleanVolatile(base, offset, (boolean) obj);
                break;
            }
            case "char": {
                Unsafe.instance.putCharVolatile(base, offset, (char) obj);
                break;
            }
            case "byte": {
                Unsafe.instance.putByteVolatile(base, offset, (byte) obj);
                break;
            }
            default: {
                Unsafe.instance.putObjectVolatile(base, offset, clone(obj, 0));
                break;
            }
        }
    }

    private static boolean shouldIgnore(Field field) {
        boolean result = field.getType() == CreativeTabs.class || field.getType() == RegistryNamespaced.class || field.getType() == SimpleNetworkWrapper.class;
        Object object = getStatic(field);
        return result || object instanceof Item || object instanceof Block || object instanceof Potion || object instanceof Enchantment || object instanceof Logger || object instanceof MinecraftServer || (Launch.client && (object instanceof Minecraft));
    }

    private static boolean shouldIgnore(Class<?> clazz) {
        return clazz == ForgeInternalHandler.class || clazz == ForgeModContainer.class;
    }

    public synchronized static void reset() {
        Launch.LOGGER.info("Resetting cached fields.");
        Launch.LOGGER.info("Resetting event listeners in EventBus.");
        Unsafe.instance.putObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset, clone(saved_listeners, 0));
        Launch.LOGGER.info("Resetting static fields in event listeners.");

        for (String s : Transformer.getEventListeners()) {
            try {
                Class<?> clazz = Class.forName(s);

                Launch.LOGGER.info("Listener:" + s);
                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + getStatic(field));
                        Object object = getStatic(field);
                        if (cache.containsKey(field)) {
                            Launch.LOGGER.info("Replacing.");
                            putStatic(field, clone(cache.get(field), 0));
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        Launch.LOGGER.info("Resetting event listeners in Event.");
        Unsafe.instance.putObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2, saved_listeners_2);
        Launch.LOGGER.info("Resetting mod static fields.");
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                Launch.LOGGER.info("Mod:" + container.getModId());
                Class<?> clazz = container.getMod().getClass();
                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + getStatic(field));
                        Object object = getStatic(field);
                        if (cache.containsKey(field)) {
                            Launch.LOGGER.info("Replacing.");
                            putStatic(field, clone(cache.get(field), 0));
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        Launch.LOGGER.info("Resetting fields which the transformer found.");
        cache2.forEach((field, object) -> {
            Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + getStatic(field));
            Launch.LOGGER.info("Replacing.");
            putStatic(field, clone(object, 0));
        });
    }

    public static void CoreDump() {
        Unsafe.instance.freeMemory(114514);
    }

    public static boolean BadGui(net.minecraft.client.gui.Gui gui) {
        if (gui == null) {
            return false;
        }
        String name = ReflectionUtil.getName(gui.getClass());
        String l = name.toLowerCase();
        return (Config.guiProtect && (Transformer.isModClass(name) || gui.getClass().getProtectionDomain() == null || gui.getClass().getProtectionDomain().getCodeSource() == null)) || gui.getClass() == ModMain.GUI || l.contains("death") || l.contains("over") || l.contains("die") || l.contains("dead");
    }

    public static void DisplayDeathGui() {

    }

    public static boolean isKanadeDeathGui(Object o) {
        return o != null && (o.getClass() == ModMain.GUI || (o instanceof Minecraft && Unsafe.instance.getObjectVolatile(o, LateFields.currentScreen_offset).getClass() == ModMain.GUI));
    }

    public static boolean isKanadeDeathGuiClosed(Object o) {
        if (ModMain.GUI.isInstance(o)) {
            return Unsafe.instance.getBooleanVolatile(o, LateFields.close_offset);
        }
        return false;
    }


    public static boolean shouldPostEvent(Event event) {
        if (Launch.client) {
            if (event instanceof GuiOpenEvent) {
                GuiOpenEvent guiOpenEvent = (GuiOpenEvent) event;
                GuiScreen gui = guiOpenEvent.getGui();
                return !ModMain.GUI.isInstance(gui) && !(gui instanceof GuiGameOver) && !(gui instanceof GuiChat) && !(gui instanceof GuiIngameMenu) && !(gui instanceof GuiMainMenu);
            }
        }
        return true;
    }

    public static boolean FromModClass(Object obj) {
        String name = ReflectionUtil.getName(obj.getClass());
        Launch.LOGGER.info("class:" + name);
        return ModClass(name);
    }

    public static boolean ModClass(String name) {
        name = ((KanadeClassLoader) Launch.classLoader).untransformName(name);
        final URL res = Launch.classLoader.findResource(name.replace('.', '/').concat(".class"));
        if (res != null) {
            String path = res.getPath();

            if (path.contains("!")) {
                path = path.substring(0, path.indexOf("!"));
            }
            if (path.contains("file:/")) {
                path = path.replace("file:/", "");
            }

            return path.startsWith("mods", path.lastIndexOf(File.separator) - 4);
        }
        return false;
    }
}