package miku.lib.utils;

import miku.lib.reflection.ReflectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLContainer;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ObjectUtils {
    static final Map<Field, Long> offsetCache = new ConcurrentHashMap<>();
    static final Map<Field, Object> baseCache = new ConcurrentHashMap<>();
    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static Object clone(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Class) {
            return o;
        }
        Object copy;
        long offset;
        if (o.getClass().getName().startsWith("[")) {
            Class<?> cls = o.getClass();
            int abo = unsafe.arrayBaseOffset(cls);
            int ais = unsafe.arrayIndexScale(cls);
            switch (cls.getName()) {
                case "[I": {
                    int[] array = new int[((int[]) o).length];
                    int[] old = (int[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[J": {
                    long[] array = new long[((long[]) o).length];
                    long[] old = (long[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[S": {
                    short[] array = new short[((short[]) o).length];
                    short[] old = (short[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[Z": {
                    boolean[] array = new boolean[((boolean[]) o).length];
                    boolean[] old = (boolean[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[F": {
                    float[] array = new float[((float[]) o).length];
                    float[] old = (float[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[D": {
                    double[] array = new double[((double[]) o).length];
                    double[] old = (double[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[C": {
                    char[] array = new char[((char[]) o).length];
                    char[] old = (char[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[B": {
                    byte[] array = new byte[((byte[]) o).length];
                    byte[] old = (byte[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                default: {
                    Object array = Array.newInstance(o.getClass().getComponentType(), Array.getLength(o));
                    int length = Array.getLength(o);
                    for (int i = 0; i < length; i++) {
                        long address = ((long) i * ais) + abo;
                        unsafe.putObjectVolatile(array, address, unsafe.getObjectVolatile(o, address));
                    }
                    return array;
                }
            }
        }
        {
            try {
                copy = unsafe.allocateInstance(o.getClass());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        for (Field field : ReflectionHelper.getAllFields(o.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (offsetCache.containsKey(field)) {
                offset = offsetCache.get(field);
            } else {
                offset = unsafe.objectFieldOffset(field);
                offsetCache.put(field, offset);
            }
            switch (field.getType().getName()) {
                case "int": {
                    unsafe.putIntVolatile(copy, offset, unsafe.getIntVolatile(o, offset));
                    break;
                }
                case "float": {
                    unsafe.putFloatVolatile(copy, offset, unsafe.getFloatVolatile(o, offset));
                    break;
                }
                case "double": {
                    unsafe.putDoubleVolatile(copy, offset, unsafe.getDoubleVolatile(o, offset));
                    break;
                }
                case "long": {
                    unsafe.putLongVolatile(copy, offset, unsafe.getLongVolatile(o, offset));
                    break;
                }
                case "short": {
                    unsafe.putShortVolatile(copy, offset, unsafe.getShortVolatile(o, offset));
                    break;
                }
                case "boolean": {
                    unsafe.putBooleanVolatile(copy, offset, unsafe.getBooleanVolatile(o, offset));
                    break;
                }
                case "char": {
                    unsafe.putCharVolatile(copy, offset, unsafe.getCharVolatile(o, offset));
                    break;
                }
                case "byte": {
                    unsafe.putByteVolatile(copy, offset, unsafe.getByteVolatile(o, offset));
                    break;
                }
                default: {
                    Object obj = unsafe.getObjectVolatile(o, offset);
                    unsafe.putObjectVolatile(copy, offset, clone(obj));
                }
            }
        }
        return copy;
    }

    public static String getProcessId() {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            throw new UnsupportedOperationException();
        }
    }


    public static Object getField(Field field, Object base) {
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.objectFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                return unsafe.getIntVolatile(base, offset);
            }
            case "float": {
                return unsafe.getFloatVolatile(base, offset);
            }
            case "double": {
                return unsafe.getDoubleVolatile(base, offset);
            }
            case "long": {
                return unsafe.getLongVolatile(base, offset);
            }
            case "short": {
                return unsafe.getShortVolatile(base, offset);
            }
            case "boolean": {
                return unsafe.getBooleanVolatile(base, offset);
            }
            case "char": {
                return unsafe.getCharVolatile(base, offset);
            }
            case "byte": {
                return unsafe.getByteVolatile(base, offset);
            }
            default: {
                return unsafe.getObjectVolatile(base, unsafe.objectFieldOffset(field));
            }
        }
    }

    public static Object getStatic(Field field) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = unsafe.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.staticFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                return unsafe.getIntVolatile(base, offset);
            }
            case "float": {
                return unsafe.getFloatVolatile(base, offset);
            }
            case "double": {
                return unsafe.getDoubleVolatile(base, offset);
            }
            case "long": {
                return unsafe.getLongVolatile(base, offset);
            }
            case "short": {
                return unsafe.getShortVolatile(base, offset);
            }
            case "boolean": {
                return unsafe.getBooleanVolatile(base, offset);
            }
            case "char": {
                return unsafe.getCharVolatile(base, offset);
            }
            case "byte": {
                return unsafe.getByteVolatile(base, offset);
            }
            default: {
                return unsafe.getObjectVolatile(base, unsafe.staticFieldOffset(field));
            }
        }

    }

    public static void putField(Field field, Object base, Object obj) {
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.objectFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                unsafe.putIntVolatile(base, offset, (int) obj);
                break;
            }
            case "float": {
                unsafe.putFloatVolatile(base, offset, (float) obj);
                break;
            }
            case "double": {
                unsafe.putDoubleVolatile(base, offset, (double) obj);
                break;
            }
            case "long": {
                unsafe.putLongVolatile(base, offset, (long) obj);
                break;
            }
            case "short": {
                unsafe.putShortVolatile(base, offset, (short) obj);
                break;
            }
            case "boolean": {
                unsafe.putBooleanVolatile(base, offset, (boolean) obj);
                break;
            }
            case "char": {
                unsafe.putCharVolatile(base, offset, (char) obj);
                break;
            }
            case "byte": {
                unsafe.putByteVolatile(base, offset, (byte) obj);
                break;
            }
            default: {
                unsafe.putObjectVolatile(base, offset, obj);
                break;
            }
        }
    }

    public static void putStatic(Field field, Object obj) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = unsafe.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.staticFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                unsafe.putIntVolatile(base, offset, (int) obj);
                break;
            }
            case "float": {
                unsafe.putFloatVolatile(base, offset, (float) obj);
                break;
            }
            case "double": {
                unsafe.putDoubleVolatile(base, offset, (double) obj);
                break;
            }
            case "long": {
                unsafe.putLongVolatile(base, offset, (long) obj);
                break;
            }
            case "short": {
                unsafe.putShortVolatile(base, offset, (short) obj);
                break;
            }
            case "boolean": {
                unsafe.putBooleanVolatile(base, offset, (boolean) obj);
                break;
            }
            case "char": {
                unsafe.putCharVolatile(base, offset, (char) obj);
                break;
            }
            case "byte": {
                unsafe.putByteVolatile(base, offset, (byte) obj);
                break;
            }
            default: {
                unsafe.putObjectVolatile(base, offset, obj);
                break;
            }
        }
    }

    public static void printAddresses(Object... objects) {
        System.out.print("0x");
        long last;
        int offset = unsafe.arrayBaseOffset(objects.getClass());
        int scale = unsafe.arrayIndexScale(objects.getClass());
        switch (scale) {
            case 4:
                long factor = unsafe.addressSize() == 8 ? 8 : 1;
                final long i1 = (unsafe.getIntVolatile(objects, offset) & 0xFFFFFFFFL) * factor;
                System.out.print(Long.toHexString(i1));
                last = i1;
                for (int i = 1; i < objects.length; i++) {
                    final long i2 = (unsafe.getIntVolatile(objects, offset + i * 4L) & 0xFFFFFFFFL) * factor;
                    if (i2 > last)
                        System.out.print(", +" + Long.toHexString(i2 - last));
                    else
                        System.out.print(", -" + Long.toHexString(last - i2));
                    last = i2;
                }
                break;
            case 8:
                throw new AssertionError("Not supported");
        }
        System.out.println();
    }

    public static void fillValue(Field field, Object o, Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        Object base = null;
        if (isStatic) {
            if (baseCache.containsKey(field)) {
                base = baseCache.get(field);
            } else {
                base = unsafe.staticFieldBase(field);
                baseCache.put(field, base);
            }
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = isStatic ? unsafe.staticFieldOffset(field) : unsafe.objectFieldOffset(field);
            offsetCache.put(field, offset);
        }
        base = base == null ? o : base;
        if (base == null) {
            throw new IllegalArgumentException("The fuck?");
        }
        if (field.getType().getName().startsWith("[")) {
            int abo = unsafe.arrayBaseOffset(field.getType());
            int ais = unsafe.arrayIndexScale(field.getType());
            switch (field.getType().getName()) {
                case "[I": {
                    int[] array = new int[((int[]) obj).length];
                    int[] old = (int[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[J": {
                    long[] array = new long[((long[]) obj).length];
                    long[] old = (long[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[S": {
                    short[] array = new short[((short[]) obj).length];
                    short[] old = (short[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[Z": {
                    boolean[] array = new boolean[((boolean[]) obj).length];
                    boolean[] old = (boolean[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[F": {
                    float[] array = new float[((float[]) obj).length];
                    float[] old = (float[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[D": {
                    double[] array = new double[((double[]) obj).length];
                    double[] old = (double[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[C": {
                    char[] array = new char[((char[]) obj).length];
                    char[] old = (char[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[B": {
                    byte[] array = new byte[((byte[]) obj).length];
                    byte[] old = (byte[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                default: {
                    Object array = Array.newInstance(field.getType().getComponentType(), Array.getLength(obj));
                    int length = Array.getLength(obj);
                    for (int i = 0; i < length; i++) {
                        long address = ((long) i * ais) + abo;
                        unsafe.putObjectVolatile(array, address, unsafe.getObjectVolatile(obj, address));
                    }
                    break;
                }
            }
            return;
        }
        switch (field.getType().getName()) {
            case "int": {
                unsafe.putIntVolatile(base, offset, (int) obj);
                break;
            }
            case "float": {
                unsafe.putFloatVolatile(base, offset, (float) obj);
                break;
            }
            case "double": {
                unsafe.putDoubleVolatile(base, offset, (double) obj);
                break;
            }
            case "long": {
                unsafe.putLongVolatile(base, offset, (long) obj);
                break;
            }
            case "short": {
                unsafe.putShortVolatile(base, offset, (short) obj);
                break;
            }
            case "boolean": {
                unsafe.putBooleanVolatile(base, offset, (boolean) obj);
                break;
            }
            case "char": {
                unsafe.putCharVolatile(base, offset, (char) obj);
                break;
            }
            case "byte": {
                unsafe.putByteVolatile(base, offset, (byte) obj);
                break;
            }
            default: {
                unsafe.putObjectVolatile(base, offset, clone(obj));
                break;
            }
        }
    }

    public static long location(Object object) {

        Object[] array = new Object[]{object};


        long baseOffset = unsafe.arrayBaseOffset(Object[].class);

        int addressSize = unsafe.addressSize();

        long location;

        switch (addressSize) {

            case 4:

                location = unsafe.getInt(array, baseOffset);

                break;

            case 8:

                location = unsafe.getLong(array, baseOffset);

                break;

            default:

                throw new Error("unsupported address size: " + addressSize);

        }

        return (location) * 8L;

    }


    public static void replaceAbyB(Object a, Object b) {
        Type type = JVM.type("instanceOopDesc");
        long a1 = ObjectUtils.location(a);
        long a2 = ObjectUtils.location(b);
        for (int i = 0; i < type.size; i++) {
            unsafe.putByte(a1 + i, unsafe.getByte(a2 + i));
        }
        copyAFields2B(b, a, b.getClass());
    }

    public static void copyAFields2B(Object a, Object b, Class<?> c) {
        for (Field f : ReflectionHelper.getAllFields(c)) {
            if (!Modifier.isStatic(f.getModifiers())) {
                Object o = getField(f, a);
                putField(f, b, o);
            }
        }
    }


    public static boolean ModClass(String name) {
        if (name.startsWith("openeye.") || name.startsWith("mcst.") || name.startsWith("miku.lib") || name.startsWith("one.helfy") || name.startsWith("me.xdark")) {
            return false;
        }
        /*if (CommandSekaiConfig.Configs.betterCompatible) {
            if (name.startsWith("morph.avaritia") || name.startsWith("codechicken.lib") ||
                    name.startsWith("mezz.jei") || name.startsWith("com.dhanantry.") ||
                    name.startsWith("com.github.alexthe666") || name.startsWith("net.minecraft.AgeOfMinecraft") ||
                    name.startsWith("net.ilexiconn") || name.startsWith("com.bobmowzie") ||
                    name.startsWith("ic2.") || name.startsWith("org.ejml") ||
                    name.startsWith("org.spongepowered") || name.startsWith("io.github.notstirred") ||
                    name.startsWith("com.shinoow") || name.startsWith("de.ellpeck") ||
                    name.startsWith("invtweaks.api") || name.startsWith("me.jacky1356400") ||
                    name.startsWith("com.tmtravlr") || name.startsWith("com.endertech") ||
                    name.startsWith("nightkosh.") || name.startsWith("xyz.phanta") ||
                    name.startsWith("net.bdew") || name.startsWith("p455w0rd.") ||
                    name.startsWith("com.suicidarker") || name.startsWith("com.gildedgames") ||
                    name.startsWith("com.builtbroken") || name.startsWith("vazkii.") ||
                    name.startsWith("com.suppergerrie2") || name.startsWith("org.jaudiotagger") ||
                    name.startsWith("vaskii.") || name.startsWith("vazkii.") ||
                    name.startsWith("com.creativemd.") || name.startsWith("com.teamdman") ||
                    name.startsWith("lumberwizard.") || name.startsWith("squeek.") ||
                    name.startsWith("betterwithmods.") || name.startsWith("appeng.") ||
                    name.startsWith("com.fuzs.") || name.startsWith("com.jackiecrazi.") ||
                    name.startsWith("com.buuz135.") || name.startsWith("org.softc.") ||
                    name.startsWith("aroma1997.") || name.startsWith("hellfirepvp.") ||
                    name.startsWith("com.codetaylor.") || name.startsWith("net.darkhax.") ||
                    name.startsWith("party.") || name.startsWith("com.tfar") ||
                    name.startsWith("jackyy.avaritiatweaks.") || name.startsWith("baconetworks.") ||
                    name.startsWith("com.kreezcraft.") || name.startsWith("com.teamacronymcoders.") ||
                    name.startsWith("atomicstryker.") || name.startsWith("baubles") ||
                    name.startsWith("net.bdew") || name.startsWith("com.mordenkainen.") ||
                    name.startsWith("betteradvancements.") || name.startsWith("net.darkhax.") ||
                    name.startsWith("com.yungnickyoung.") || name.startsWith("bettercombat.") ||
                    name.startsWith("mods.betterfoliage.") || name.startsWith("mods.octarinecore.") ||
                    name.startsWith("guichaguri.") || name.startsWith("arekkuusu.") ||
                    name.startsWith("com.judge40.") || name.startsWith("paulevs.") ||
                    name.startsWith("com.tterrag.") || name.startsWith("angercraft.") ||
                    name.startsWith("com.mic") || name.startsWith("jds.") ||
                    name.startsWith("de.keksuccino.") || name.startsWith("net.elnounch.") ||
                    name.startsWith("binnie.") || name.startsWith("pl.asie.") ||
                    name.startsWith("shadows.")
            ) {
                return false;
            }
        }

         */
        String original_name = FMLDeobfuscatingRemapper.INSTANCE.unmap(name.replace('.', '/')).replace('/', '.');
        if (!name.equals(original_name)) {
            return false;
        }
        final URL res = Launch.classLoader.findResource(original_name.replace('.', '/').concat(".class"));
        if (res != null) {
            String path = res.getPath();
            try {
                path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ignored) {
            }

            if (path.contains("!")) {
                path = path.substring(0, path.lastIndexOf("!"));
            }
            if (path.contains("file:/")) {
                path = path.replace("file:/", "");
            }
            if (win) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
            }
            return path.contains("/mods/") || path.contains("\\mods\\");
        }
        return false;
    }

    static boolean win = System.getProperty("os.name").startsWith("Windows");

    public static boolean FromModClass(Object obj) {
        String name = ReflectionHelper.getName(obj.getClass());
        return ModClass(name);
    }

    public static void resetStatic() {
        kanade.kill.Launch.LOGGER.info("Reset static entered.");
        Vector<Class<?>> targets = new Vector<>(Launch.classLoader.classes);
        targets.removeIf(c -> !ModClass(ReflectionHelper.getName(c)));
        for (Class<?> clazz : targets) {
            for (Field field : ReflectionHelper.getFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers()) || shouldIgnore(field)) {
                    Object obj = getStatic(field);
                    if (obj instanceof List) {
                        try {
                            ((List<?>) obj).clear();
                        } catch (Throwable ignored) {
                        }
                    } else if (obj instanceof Map) {
                        try {
                            ((Map<?, ?>) obj).clear();
                        } catch (Throwable ignored) {
                        }
                    } else if (obj instanceof Set) {
                        try {
                            ((Set<?>) obj).clear();
                        } catch (Throwable ignored) {
                        }
                    } else if (obj instanceof Entity || obj instanceof NBTTagCompound || obj instanceof World) {
                        if (obj instanceof Entity) {
                            //RenderFucker.FuckRenderer((Entity) obj);
                            ((Entity) obj).width = 0;
                            ((Entity) obj).height = 0;
                            //EntityUtils.addDead(obj);
                        }
                        putStatic(field, null);
                    } else if (field.getType() == boolean.class) {
                        ObjectUtils.putStatic(field, false);
                    } else if (field.getType() == int.class) {
                        putStatic(field, 0);
                    } else if (field.getType() == short.class) {
                        putStatic(field, (short) 0);
                    } else if (field.getType() == float.class) {
                        putStatic(field, (float) 0);
                    } else if (field.getType() == long.class) {
                        putStatic(field, (long) 0);
                    } else if (field.getType() == char.class) {
                        putStatic(field, (char) 0);
                    } else if (field.getType() == double.class) {
                        putStatic(field, (double) 0);
                    } else if (field.getType() == byte.class) {
                        putStatic(field, (byte) 0);
                    }
                }
            }
        }
        EventBus eventBus = MinecraftForge.Event_bus;
        eventBus.listenerOwners.forEach((k, v) -> {
            if (v.getModId() != null && !v.getModId().equals("forge") && !v.getModId().equals("sekai") && !v.getModId().equals("openeye") && !v.getModId().equals("jei") && !(v instanceof FMLContainer)) {
                resetObjectFields(k);
                resetObjectFields(v);
            }
        });
        kanade.kill.Launch.LOGGER.info("Reset static exited.");
    }

    private static boolean shouldIgnore(Field field) {
        return field.getName().equals("descriptor") && ReflectionHelper.getName(field.getDeclaringClass()).equals("net.minecraftforge.fml.common.FMLModContainer");
    }

    private static boolean shouldIgnore(Object object) {
        return object.getClass() == FMLModContainer.class;
    }

    private static void resetObjectFields(Object target) {
        if (shouldIgnore(target)) {
            return;
        }
        Class<?> clazz = target.getClass();
        for (Field field : ReflectionHelper.getFields(clazz)) {
            if ((!Modifier.isStatic(field.getModifiers())) || shouldIgnore(field)) {
                if (field.getType() == boolean.class) {
                    putField(field, target, false);
                } else if (field.getType() == int.class) {
                    putField(field, target, 0);
                } else if (field.getType() == short.class) {
                    putField(field, target, (short) 0);
                } else if (field.getType() == float.class) {
                    putField(field, target, (float) 0);
                } else if (field.getType() == long.class) {
                    putField(field, target, (long) 0);
                } else if (field.getType() == char.class) {
                    putField(field, target, (char) 0);
                } else if (field.getType() == double.class) {
                    putField(field, target, (double) 0);
                } else if (field.getType() == byte.class) {
                    putField(field, target, (byte) 0);
                } else {
                    Object obj = getField(field, target);
                    if (obj instanceof List) {
                        try {
                            ((List<?>) obj).clear();
                        } catch (Throwable ignored) {
                        }
                    } else if (obj instanceof Map) {
                        try {
                            ((Map<?, ?>) obj).clear();
                        } catch (Throwable ignored) {
                        }
                    } else if (obj instanceof Set) {
                        try {
                            ((Set<?>) obj).clear();
                        } catch (Throwable ignored) {
                        }
                    } else if (obj instanceof Entity || obj instanceof NBTTagCompound || obj instanceof World) {
                        if (obj instanceof Entity) {
                            //RenderFucker.FuckRenderer((Entity) obj);
                            ((Entity) obj).width = 0;
                            ((Entity) obj).height = 0;
                            //EntityUtils.addDead(obj);
                        }
                        putField(field, obj, null);
                    }
                }
            }
        }
    }


}
