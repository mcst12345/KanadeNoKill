package kanade.kill.util;

import kanade.kill.Launch;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.ReflectionUtil;
import scala.concurrent.util.Unsafe;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;

public class ObjectUtil {
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
            if (Util.offsetCache.containsKey(field)) {
                offset = Util.offsetCache.getLong(field);
            } else {
                offset = Unsafe.instance.objectFieldOffset(field);
                Util.offsetCache.put(field, offset);
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

    public synchronized static Object getStatic(Field field) {
        Object base;
        if (Util.baseCache.containsKey(field)) {
            base = Util.baseCache.get(field);
        } else {
            base = Unsafe.instance.staticFieldBase(field);
            Util.baseCache.put(field, base);
        }
        long offset;
        if (Util.offsetCache.containsKey(field)) {
            offset = Util.offsetCache.getLong(field);
        } else {
            offset = Unsafe.instance.staticFieldOffset(field);
            Util.offsetCache.put(field, offset);
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
        if (Util.baseCache.containsKey(field)) {
            base = Util.baseCache.get(field);
        } else {
            base = Unsafe.instance.staticFieldBase(field);
            Util.baseCache.put(field, base);
        }
        long offset;
        if (Util.offsetCache.containsKey(field)) {
            offset = Util.offsetCache.getLong(field);
        } else {
            offset = Unsafe.instance.staticFieldOffset(field);
            Util.offsetCache.put(field, offset);
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
