package kanade.kill;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "raw"})
public class Util {
    public static final List<Runnable> tasks = new ArrayList<>();
    private static final Set<UUID> Dead = new HashSet<>();
    private static final Map<Integer, Object> cache = new HashMap<>();
    private static Object saved_listeners;
    private static Object saved_listeners_2;
    public static boolean killing;
    private static URLClassLoader modClassLoader;
    public static void Kill(List<Entity> list) {
        for (Entity e : list) {
            Kill(e);
        }
        reset();
    }

    @SuppressWarnings("unchecked")
    public static synchronized void Kill(Entity entity) {
        if (KillItem.inList(entity)) return;
        try {
            killing = true;
            Dead.add(entity.getUniqueID());
            World world = entity.world;
            if (world.loadedEntityList.getClass() != ArrayList.class) {
                Unsafe.instance.putObjectVolatile(world, LateFields.loadedEntityList_offset, new ArrayList<>(world.loadedEntityList));
            }
            world.loadedEntityList.remove(entity);
            Chunk chunk = world.getChunk(entity.chunkCoordX, entity.chunkCoordZ);
            ClassInheritanceMultiMap<Entity>[] entityLists = (ClassInheritanceMultiMap<Entity>[]) Unsafe.instance.getObjectVolatile(chunk, LateFields.entityLists_offset);
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
                    if (world.playerEntities.getClass() != ArrayList.class) {
                        Unsafe.instance.putObjectVolatile(world, LateFields.playerEntities_offset, new ArrayList<>(world.playerEntities));
                    }
                    world.playerEntities.remove(entity);

                }
            }
            reset();
            killing = false;
        } catch (Throwable t) {
            t.printStackTrace();
            killing = false;
            throw new RuntimeException(t);
        }
    }

    public static boolean isDead(Entity entity) {
        return entity == null || Dead.contains(entity.getUniqueID());
    }

    public static boolean NoRemove(Object item) {
        return item == ModMain.kill_item || item == ModMain.death_item || (item instanceof ItemStack && NoRemove(((ItemStack) item).getITEM()));
    }

    public static boolean invHaveKillItem(EntityPlayer player) {
        InventoryPlayer inventoryPlayer = player.inventory;
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

    @SuppressWarnings("ConstantValue")
    public static void updatePlayer(EntityPlayer player) {
        IAttributeInstance instance = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.setBaseValue(2.0d);
        }
        instance = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FLYING_SPEED);
        if (instance != null) {
            instance.setBaseValue(10.0d);
        }
        instance = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.LUCK);
        if (instance != null) {
            instance.setBaseValue(1024.0d);
        }
    }

    public static Object clone(Object o) {
        if (o == null) {
            System.out.println("Warn:object is null.");
            return null;
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
        for (Field field : getAllFields(o.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            offset = Unsafe.instance.objectFieldOffset(field);
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
                    System.out.println("Coping field:" + field.getType());
                    Object obj = Unsafe.instance.getObjectVolatile(o, offset);
                    Unsafe.instance.putObjectVolatile(copy, offset, clone(obj));
                }
            }
        }
        return copy;
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        try {
            List<Field> list = new ArrayList<>();
            if (clazz == null) {
                return list;
            }
            while (clazz != Object.class) {
                Collections.addAll(list, (Field[]) EarlyMethods.getDeclaredFields0.invoke(clazz, false));
                clazz = clazz.getSuperclass();
            }
            return list;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    public synchronized static void save() {
        if (modClassLoader == null) {
            modClassLoader = (URLClassLoader) Unsafe.instance.getObjectVolatile(Loader.instance(), LateFields.modClassLoader_offset);
        }
        System.out.println("Coping event listeners in EventBus.");
        saved_listeners = clone(Unsafe.instance.getObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset));
        System.out.println("Coping static fields in event listeners.");
        ConcurrentHashMap map = (ConcurrentHashMap) Unsafe.instance.getObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset);
        map.forEach((key, value) -> {
            Class<?> clazz = key.getClass();
            System.out.println("Listener:" + clazz.getName());
            for (Field field : getAllFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    System.out.println("Field:" + field.getName());
                    try {
                        Object o = getStatic(field);
                        cache.put(System.identityHashCode(o), clone(o));
                    } catch (Throwable t) {
                        if (t instanceof StackOverflowError) {
                            System.out.println("Too deep. Ignoring this field.");
                        } else {
                            throw new RuntimeException(t);
                        }
                    }
                }
            }
        });
        System.out.println("Coping event listeners in Event.");
        saved_listeners_2 = clone(Unsafe.instance.getObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2));
        System.out.println("Coping static fields in mod instances.");
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                System.out.println("Mod:" + container.getModId());
                Class<?> clazz = modClassLoader.loadClass(container.getMod().getClass().getName());
                for (Field field : getAllFields(clazz)) {
                    if (field.getType() == Item.class || field.getType() == Block.class || field.getType() == Potion.class || field.getType() == Enchantment.class || field.getType() == ItemBlock.class || field.getType() == BlockOre.class || field.getType() == ItemArmor.class || field.getType() == CreativeTabs.class || field.getType() == Logger.class) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        System.out.println("Field:" + field.getName());
                        try {
                            Object object = getStatic(field);
                            cache.put(System.identityHashCode(object), clone(object));
                        } catch (Throwable t) {
                            if (t instanceof StackOverflowError) {
                                System.out.println("Too deep. Ignoring this field.");
                            } else {
                                throw new RuntimeException(t);
                            }
                        }
                    }
                }
                System.gc();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public synchronized static Object getStatic(Field field) {
        Object base = Unsafe.instance.staticFieldBase(field);
        long offset = Unsafe.instance.staticFieldOffset(field);
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
        System.out.println(obj);
        Object base = Unsafe.instance.staticFieldBase(field);
        long offset = Unsafe.instance.staticFieldOffset(field);
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
                Unsafe.instance.putObjectVolatile(base, offset, clone(obj));
                break;
            }
        }
    }

    public synchronized static void reset() {
        System.out.println("Resetting cached fields.");
        System.out.println("Resetting event listeners in EventBus.");
        Unsafe.instance.putObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset, clone(saved_listeners));
        System.out.println("Resetting static fields in event listeners.");
        ConcurrentHashMap map = (ConcurrentHashMap) Unsafe.instance.getObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset);
        map.forEach((key, value) -> {
            Class<?> clazz = key.getClass();
            System.out.println("Listener:" + clazz.getName());
            for (Field field : getAllFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    System.out.println("Field:" + field.getName());
                    Object object = getStatic(field);
                    int hash = System.identityHashCode(object);
                    if (cache.containsKey(hash)) {
                        Object newObject = clone(cache.get(hash));
                        putStatic(field, newObject);
                        cache.put(System.identityHashCode(newObject), cache.get(hash));
                        cache.remove(hash);
                    }
                }
            }
        });
        System.out.println("Resetting event listeners in Event.");
        Unsafe.instance.putObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2, saved_listeners_2);
        System.out.println("Resetting mod static fields.");
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                System.out.println("Mod:" + container.getModId());
                Class<?> clazz = modClassLoader.loadClass(container.getMod().getClass().getName());
                for (Field field : getAllFields(clazz)) {
                    if (field.getType() == Item.class || field.getType() == Block.class || field.getType() == Potion.class || field.getType() == Enchantment.class || field.getType() == ItemBlock.class || field.getType() == BlockOre.class || field.getType() == ItemArmor.class || field.getType() == CreativeTabs.class || field.getType() == Logger.class) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        System.out.println("Field:" + field.getName());
                        Object object = getStatic(field);
                        int hash = System.identityHashCode(object);
                        if (cache.containsKey(hash)) {
                            Object newObject = clone(cache.get(hash));
                            putStatic(field, newObject);
                            cache.put(System.identityHashCode(newObject), cache.get(hash));
                            cache.remove(hash);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
