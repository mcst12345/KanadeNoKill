package kanade.kill.util;

import kanade.kill.Launch;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.ReflectionUtil;
import scala.concurrent.util.Unsafe;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ObjectUtil {
    static final Map<Field, Long> offsetCache = new ConcurrentHashMap<>();
    static final Map<Field, Object> baseCache = new ConcurrentHashMap<>();

    public static void ResetStatic() {
        if (Launch.Debug) {
            Util.printStackTrace();
        }
        Launch.LOGGER.info("Resetting static fields...");
        Map<String, Field> cache = new HashMap<>();
        for (Class<?> clazz : Launch.INSTRUMENTATION.getAllLoadedClasses()) {
            if (Modifier.isInterface(clazz.getModifiers())) {
                continue;
            }
            cache.clear();
            String cls_name = ReflectionUtil.getName(clazz);
            if (ModClass(cls_name) && !cls_name.startsWith("kanade.kill") && !KanadeClassLoader.exclusions.contains(cls_name)) {
                Launch.LOGGER.info("Class:" + cls_name);
                for (Field f : ReflectionUtil.getAllFields(clazz)) {
                    if (Modifier.isStatic(f.getModifiers())) {
                        cache.put(f.getName() + f.getType().getTypeName(), f);
                    }
                }
                Class<?> shadow = ClassUtil.shadowClass(clazz);
                if (shadow == null) {
                    continue;
                }
                try {
                    Unsafe.instance.ensureClassInitialized(shadow);
                    for (Field f : ReflectionUtil.getAllFields(shadow)) {
                        String name = f.getName() + f.getType().getTypeName();
                        if (!cache.containsKey(name)) {
                            continue;//The fuck? This shouldn't happen!
                        }
                        try {
                            Object o = getStatic(f);
                            Launch.LOGGER.info("Field:" + f + ":" + o);
                            putStatic(cache.get(name), o);
                        } catch (Throwable t) {
                            Launch.LOGGER.error("The fuck?", t);
                        }
                    }
                } catch (Throwable t) {
                    Launch.LOGGER.error("The fuck?", t);
                }
            }
        }
    }
    public static void Fuck(Object o) {
        {
            if (o == null) {
                return;
            }
            NativeMethods.SetTag(o, 25L);
            Class<?> clazz = o.getClass();
            Field[] fields = ReflectionUtil.getAllFields(clazz);
            for (Field f : fields) {
                boolean STATIC = Modifier.isStatic(f.getModifiers());
                Object obj = STATIC ? getStatic(f) : getField(f, o);
                if (obj instanceof int[]) {
                    if (STATIC) {
                        putStatic(f, new int[((int[]) obj).length]);
                    } else {
                        putField(f, o, new int[((int[]) obj).length]);
                    }
                } else if (obj instanceof short[]) {
                    if (STATIC) {
                        putStatic(f, new int[((short[]) obj).length]);
                    } else {
                        putField(f, o, new int[((short[]) obj).length]);
                    }
                } else if (obj instanceof char[]) {
                    if (STATIC) {
                        putStatic(f, new int[((char[]) obj).length]);
                    } else {
                        putField(f, o, new int[((char[]) obj).length]);
                    }
                } else if (obj instanceof long[]) {
                    if (STATIC) {
                        putStatic(f, new int[((long[]) obj).length]);
                    } else {
                        putField(f, o, new int[((long[]) obj).length]);
                    }
                } else if (obj instanceof float[]) {
                    if (STATIC) {
                        putStatic(f, new int[((float[]) obj).length]);
                    } else {
                        putField(f, o, new int[((float[]) obj).length]);
                    }
                } else if (obj instanceof double[]) {
                    if (STATIC) {
                        putStatic(f, new int[((double[]) obj).length]);
                    } else {
                        putField(f, o, new int[((double[]) obj).length]);
                    }
                } else if (obj instanceof byte[]) {
                    if (STATIC) {
                        putStatic(f, new int[((byte[]) obj).length]);
                    } else {
                        putField(f, o, new int[((byte[]) obj).length]);
                    }
                } else if (obj instanceof boolean[]) {
                    if (STATIC) {
                        putStatic(f, new int[((boolean[]) obj).length]);
                    } else {
                        putField(f, o, new int[((boolean[]) obj).length]);
                    }
                }
                if (f.getType().getName().equals("int")) {
                    if (STATIC) {
                        putStatic(f, 0);
                    } else {
                        putField(f, o, 0);
                    }
                } else if (f.getType().getName().equals("short")) {
                    if (STATIC) {
                        putStatic(f, (short) 0);
                    } else {
                        putField(f, o, (short) 0);
                    }
                } else if (f.getType().getName().equals("char")) {
                    if (STATIC) {
                        putStatic(f, ' ');
                    } else {
                        putField(f, o, ' ');
                    }
                } else if (f.getType().getName().equals("long")) {
                    if (STATIC) {
                        putStatic(f, 0L);
                    } else {
                        putField(f, o, 0L);
                    }
                } else if (f.getType().getName().equals("float")) {
                    if (STATIC) {
                        putStatic(f, 0.0f);
                    } else {
                        putField(f, o, 0.0f);
                    }
                } else if (f.getType().getName().equals("double")) {
                    if (STATIC) {
                        putStatic(f, 0.0d);
                    } else {
                        putField(f, o, 0.0d);
                    }
                } else if (f.getType().getName().equals("byte")) {
                    byte b = 0;
                    if (STATIC) {
                        putStatic(f, b);
                    } else {
                        putField(f, o, b);
                    }
                } else if (f.getType().getName().equals("boolean")) {
                    if (STATIC) {
                        putStatic(f, false);
                    } else {
                        putField(f, o, false);
                    }
                } else if (obj instanceof List) {
                    Launch.LOGGER.info("Fucking list.");
                    try {
                        ((List) obj).clear();
                    } catch (Throwable ignored) {
                    }
                } else if (obj instanceof Set) {
                    Launch.LOGGER.info("Fucking set.");
                    try {
                        ((Set) obj).clear();
                    } catch (Throwable ignored) {
                    }
                } else if (obj instanceof Map) {
                    Launch.LOGGER.info("Fucking map.");
                    try {
                        ((Map) obj).clear();
                    } catch (Throwable ignored) {
                    }
                } else if (obj instanceof Collection) {
                    Launch.LOGGER.info("Fucking collection.");
                    try {
                        ((Collection) obj).clear();
                    } catch (Throwable ignored) {
                    }
                } else if (obj instanceof TimerTask) {
                    Launch.LOGGER.info("Fucking TimerTask.");
                    try {
                        ((TimerTask) obj).cancel();
                    } catch (Throwable t) {
                        Launch.LOGGER.error("Failed to cancel TimerTask:", t);
                    }
                } else if (obj instanceof Thread) {
                    Launch.LOGGER.info("Fucking thread.");
                    ThreadUtil.StopThread((Thread) obj);
                } else if (obj instanceof Timer) {
                    Launch.LOGGER.info("Fucking timer.");
                    try {
                        ((Timer) obj).cancel();
                    } catch (Throwable t) {
                        Launch.LOGGER.error("Failed to cancel timer:", t);
                    }
                }
            }
        }
    }

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
            int abo = Unsafe.instance.arrayBaseOffset(cls);
            int ais = Unsafe.instance.arrayIndexScale(cls);
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
                        Unsafe.instance.putObjectVolatile(array, address, Unsafe.instance.getObjectVolatile(o, address));
                    }
                    return array;
                }
            }
        }
        {
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
                offset = offsetCache.get(field);
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
                    Object obj = Unsafe.instance.getObjectVolatile(o, offset);
                    Unsafe.instance.putObjectVolatile(copy, offset, clone(obj));
                }
            }
        }
        return copy;
    }

    public static Object getField(Field field, Object base) {
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = Unsafe.instance.objectFieldOffset(field);
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
                return Unsafe.instance.getObjectVolatile(base, Unsafe.instance.objectFieldOffset(field));
            }
        }
    }

    public static Object getStatic(Field field) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = Unsafe.instance.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
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

    public static void putField(Field field, Object base, Object obj) {
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = Unsafe.instance.objectFieldOffset(field);
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
                Unsafe.instance.putObjectVolatile(base, offset, obj);
                break;
            }
        }
    }

    public static void putStatic(Field field, Object obj) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = Unsafe.instance.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
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
                Unsafe.instance.putObjectVolatile(base, offset, obj);
                break;
            }
        }
    }

    public static boolean FromModClass(Object obj) {
        String name = ReflectionUtil.getName(obj.getClass());
        return ModClass(name);
    }

    public static boolean ModClass(String name) {
        if(Launch.classes.contains(name) || Launch.late_classes.contains(name)){
            return false;
        }
        name = ((KanadeClassLoader) Launch.classLoader).untransformName(name);
        final URL res = Launch.classLoader.findResource(name.replace('.', '/').concat(".class"));
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
            if (Launch.win) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
            }
            return path.contains("/mods/") || path.contains("\\mods\\");
        }
        return false;
    }

    public static void printAddresses(Object... objects) {
        System.out.print("0x");
        long last;
        int offset = Unsafe.instance.arrayBaseOffset(objects.getClass());
        int scale = Unsafe.instance.arrayIndexScale(objects.getClass());
        switch (scale) {
            case 4:
                long factor = Unsafe.instance.addressSize() == 8 ? 8 : 1;
                final long i1 = (Unsafe.instance.getIntVolatile(objects, offset) & 0xFFFFFFFFL) * factor;
                System.out.print(Long.toHexString(i1));
                last = i1;
                for (int i = 1; i < objects.length; i++) {
                    final long i2 = (Unsafe.instance.getIntVolatile(objects, offset + i * 4L) & 0xFFFFFFFFL) * factor;
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
    public static Object generateObject(Class<?> cls) {
        if (cls == Class.class) {
            throw new IllegalArgumentException("Cannot construct class instance!");
        }
        if (Modifier.isInterface(cls.getModifiers())) {
            if (cls == List.class || cls == Collection.class || cls == Iterable.class) {
                return Collections.EMPTY_LIST;
            } else if (cls == Set.class) {
                return Collections.EMPTY_SET;
            } else if (cls == Map.class) {
                return Collections.EMPTY_MAP;
            } else if (cls == Runnable.class) {
                return (Runnable) () -> {
                };
            } else if (cls == Callable.class) {
                return (Callable<?>) () -> null;
            } else {
                cls = ClassUtil.findAlternative(cls);
                if (cls == null) {
                    throw new UnsupportedOperationException();
                }
            }
        } else if (Modifier.isAbstract(cls.getModifiers())) {
            if (cls == AbstractCollection.class) {
                return Collections.EMPTY_LIST;
            }
            cls = ClassUtil.findAlternative(cls);
            if (cls == null) {
                throw new UnsupportedOperationException();
            }
        }
        {
            try {
                Object o = Unsafe.instance.allocateInstance(cls);
                for (Field field : ReflectionUtil.getAllFields(cls)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    fillValue(field, o, null);
                }
                return o;
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void fillValue(Field field, Object o, Object obj) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        Object base = null;
        if (isStatic) {
            if (baseCache.containsKey(field)) {
                base = baseCache.get(field);
            } else {
                base = Unsafe.instance.staticFieldBase(field);
                baseCache.put(field, base);
            }
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = isStatic ? Unsafe.instance.staticFieldOffset(field) : Unsafe.instance.objectFieldOffset(field);
            offsetCache.put(field, offset);
        }
        base = base == null ? o : base;
        if (base == null) {
            throw new IllegalArgumentException("The fuck?");
        }
        if (field.getType().getName().startsWith("[")) {
            int abo = Unsafe.instance.arrayBaseOffset(field.getType());
            int ais = Unsafe.instance.arrayIndexScale(field.getType());
            switch (field.getType().getName()) {
                case "[I": {
                    int[] array = new int[obj == null ? 0 : ((int[]) obj).length];
                    if (obj != null) {
                        int[] old = (int[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[J": {
                    long[] array = new long[obj == null ? 0 : ((long[]) obj).length];
                    if (obj != null) {
                        long[] old = (long[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[S": {
                    short[] array = new short[obj == null ? 0 : ((short[]) obj).length];
                    if (obj != null) {
                        short[] old = (short[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[Z": {
                    boolean[] array = new boolean[obj == null ? 0 : ((boolean[]) obj).length];
                    if (obj != null) {
                        boolean[] old = (boolean[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[F": {
                    float[] array = new float[obj == null ? 0 : ((float[]) obj).length];
                    if (obj != null) {
                        float[] old = (float[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[D": {
                    double[] array = new double[obj == null ? 0 : ((double[]) obj).length];
                    if (obj != null) {
                        double[] old = (double[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[C": {
                    char[] array = new char[obj == null ? 0 : ((char[]) obj).length];
                    if (obj != null) {
                        char[] old = (char[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[B": {
                    byte[] array = new byte[obj == null ? 0 : ((byte[]) obj).length];
                    if (obj != null) {
                        byte[] old = (byte[]) obj;
                        System.arraycopy(old, 0, array, 0, old.length);
                    }
                    Unsafe.instance.putObjectVolatile(base, offset, array);
                    break;
                }
                default: {
                    Object array = Array.newInstance(field.getType().getComponentType(), obj == null ? 0 : Array.getLength(obj));
                    if (obj != null) {
                        int length = Array.getLength(obj);
                        for (int i = 0; i < length; i++) {
                            long address = ((long) i * ais) + abo;
                            Unsafe.instance.putObjectVolatile(array, address, Unsafe.instance.getObjectVolatile(obj, address));
                        }
                    }
                    break;
                }
            }
            return;
        }
        switch (field.getType().getName()) {
            case "int": {
                Unsafe.instance.putIntVolatile(base, offset, obj != null ? (int) obj : 0);
                break;
            }
            case "float": {
                Unsafe.instance.putFloatVolatile(base, offset, obj != null ? (float) obj : 0.0f);
                break;
            }
            case "double": {
                Unsafe.instance.putDoubleVolatile(base, offset, obj != null ? (double) obj : 0.0D);
                break;
            }
            case "long": {
                Unsafe.instance.putLongVolatile(base, offset, obj != null ? (long) obj : 0L);
                break;
            }
            case "short": {
                Unsafe.instance.putShortVolatile(base, offset, obj != null ? (short) obj : 0);
                break;
            }
            case "boolean": {
                Unsafe.instance.putBooleanVolatile(base, offset, obj != null && (boolean) obj);
                break;
            }
            case "char": {
                Unsafe.instance.putCharVolatile(base, offset, obj != null ? (char) obj : 0);
                break;
            }
            case "byte": {
                Unsafe.instance.putByteVolatile(base, offset, obj != null ? (byte) obj : 0);
                break;
            }
            default: {
                Unsafe.instance.putObjectVolatile(base, offset, obj != null ? clone(obj) : generateObject(field.getType()));
                break;
            }
        }
    }
}
