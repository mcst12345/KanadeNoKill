package kanade.kill.util.memory;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import kanade.kill.util.InternalUtils;
import me.xdark.shell.ShellcodeRunner;
import one.helfy.JVM;
import scala.concurrent.util.Unsafe;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MemoryHelper {
    private static final long klassOffset;
    private static final int oopSize;
    private static final sun.misc.Unsafe unsafe = InternalUtils.getUnsafe();

    static {
        JVM jvm = ShellcodeRunner.jvm;
        klassOffset = jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"));
        oopSize = jvm.intConstant("oopSize");

    }
    private static final LongSet AllocatedMemories = new LongOpenHashSet();

    public static long getKlass(Class<?> clazz) {
        return oopSize == 8
                ? unsafe.getLong(clazz, klassOffset)
                : unsafe.getInt(clazz, klassOffset) & 0xffffffffL;
    }

    public static void setSuperClass(long a, long b) {
        Unsafe.instance.putAddress(a + 112L, b);
    }

    public static long getSuperClass(long a) {
        return Unsafe.instance.getAddress(a + 112L);
    }

    public static long getSuperClass(Class<?> clazz) {
        return Unsafe.instance.getAddress(getKlass(clazz) + 112L);
    }

    public static void setSuperClass(Class<?> ca, Class<?> cb) {
        setSuperClass(getKlass(ca), getKlass(cb));
    }

    public static void setModifier(long a, int m) {
        Unsafe.instance.putInt(a + 152, m);
    }

    public static void setModifier(Class<?> c, int m) {
        setModifier(getKlass(c), m);
    }

    private static String readModifiedUTF8(byte[] buf) throws IOException {
        int len = buf.length;
        byte[] tmp = new byte[len + 2];
        tmp[0] = (byte) (len >>> 8 & 255);
        tmp[1] = (byte) (len & 255);
        System.arraycopy(buf, 0, tmp, 2, len);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmp));
        return dis.readUTF();
    }

    private static byte[] String2Bytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static long getClassSymbol(long c) {
        return Unsafe.instance.getAddress(c + 16L);
    }

    public static long getClassSymbol(Class<?> clazz) {
        return getClassSymbol(getKlass(clazz));
    }

    public static long location(Object object) {

        sun.misc.Unsafe unsafe = Unsafe.instance;


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

    public static String getClassName(Class<?> clz) {
        return getClassName(getKlass(clz));
    }

    public static long getMethods(long klass) {
        return Unsafe.instance.getAddress(klass + 384L);
    }

    public static long getMethods(Class<?> clz) {
        return getMethods(getKlass(clz));
    }

    public static long getInnerClasses(long klass) {
        return Unsafe.instance.getAddress(klass + 224L);
    }

    public static long getInnerClasses(Class<?> clz) {
        return getInnerClasses(getKlass(clz));
    }

    public static long MethodOrdering(Class<?> clz) {
        return MethodOrdering(getKlass(clz));
    }

    public static long MethodOrdering(long cls) {
        return Unsafe.instance.getAddress(cls + 416L);
    }

    public static void setClassName(long cls, String str) {
        long oldSym = getClassSymbol(cls);
        short old_length = Unsafe.instance.getShort(oldSym);
        byte[] bytes = String2Bytes(str);
        short neo_length = (short) bytes.length;
        if (old_length > neo_length) {
            System.out.println("Over limit.Allocating memory.");
            long neoSym = Unsafe.instance.allocateMemory(neo_length + 8);
            AllocatedMemories.add(neoSym);
            Unsafe.instance.putShort(neoSym, neo_length);
            for (int i = 0; i < neo_length; i++) {
                Unsafe.instance.putByte(neoSym + 8 + i, bytes[i]);
            }
            Unsafe.instance.putAddress(cls + 16L, neoSym);
            if (AllocatedMemories.contains(oldSym)) {
                System.out.println("We allocated memories before. Freeing them.");
                Unsafe.instance.freeMemory(oldSym);
                AllocatedMemories.remove(oldSym);
            }
        } else {
            Unsafe.instance.putShort(oldSym, neo_length);
            for (int i = 0; i < neo_length; i++) {
                Unsafe.instance.putByte(oldSym + 8 + i, bytes[i]);
            }
        }
    }

    public static void setClassName(Class<?> cls, String str) {
        setClassName(getKlass(cls), str);
    }

    public static String getClassName(long cls) {
        if (cls == 0) {
            return "error:cls == 0!";
        }
        long symbol = getClassSymbol(cls);
        short length = Unsafe.instance.getShort(symbol);
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = Unsafe.instance.getByte(symbol + 8 + i);
        }
        try {
            return readModifiedUTF8(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getClassLoaderDATA(long cls) {
        return Unsafe.instance.getAddress(cls + 144L);
    }

    public static long getClassLoaderDATA(Class<?> cls) {
        return getClassLoaderDATA(getKlass(cls));
    }

    public static void putClassLoaderDATA(long cls, long data) {
        Unsafe.instance.putAddress(cls + 144L, data);
    }

    public static void putClassLoaderDATA(Class<?> cls, long data) {
        Unsafe.instance.putAddress(getKlass(cls) + 144L, data);
    }

    public static void putClassLoaderDATA(Class<?> cls, Class<?> a) {
        putClassLoaderDATA(cls, getClassLoaderDATA(a));
    }

    public static long getConstantPool(long cls) {
        return Unsafe.instance.getAddress(cls + 216L);
    }

    public static long getConstantPool(Class<?> cls) {
        return getConstantPool(getKlass(cls));
    }

    public static long getLocalInterfaces(long klass) {//32L or 408L
        return Unsafe.instance.getAddress(klass + 32L);
    }

    public static long getLocalInterfaces(Class<?> klass) {
        return getLocalInterfaces(getKlass(klass));
    }

    public static long[] getInterfaces(long klass) {
        long base = getLocalInterfaces(klass);
        int length = Unsafe.instance.getInt(base);
        final long[] ret = new long[length];
        for (int i = 0; i < length; i++) {
            ret[i] = Unsafe.instance.getAddress(base + (i + 1) * 8L);
        }
        return ret;
    }

    public static long[] getInterfaces(Class<?> cls) {
        return getInterfaces(getKlass(cls));
    }

    public static long getFields(long klass) {
        return Unsafe.instance.getAddress(klass + 432L);
    }

    public static long getFields(Class<?> cls) {
        return getFields(getKlass(cls));
    }

    public static void clearInterfaces(long cls) {
        Unsafe.instance.putInt(getLocalInterfaces(cls), 0);
        //Unsafe.instance.putInt(cls+408L,0);
    }

    public static void clearInterfaces(Class<?> cls) {
        Unsafe.instance.putInt(getLocalInterfaces(cls), 0);
    }

    public static void setInterfaces(long klass, Class<?>... interfaces) {
        System.out.println("Overwriting interfaces of klass at" + klass);
        long old = getLocalInterfaces(klass);
        int old_count = Unsafe.instance.getInt(old);
        int needed = interfaces.length;
        System.out.println("We have " + old_count + ",and we need " + needed);
        if (needed > old_count) {
            System.out.println("Over limit. Allocating memory.");
            if (AllocatedMemories.contains(old)) {
                System.out.println("We have allocated memory before. Freeing them.");
                Unsafe.instance.freeMemory(old);
                AllocatedMemories.remove(old);
            } else {
                AllocatedMemories.add(old);
            }
            long addr = Unsafe.instance.allocateMemory((needed + 1) * 8L);
            System.out.println("New:" + Long.toHexString(addr));
            Unsafe.instance.putAddress(klass + 32L, addr);
            Unsafe.instance.putAddress(klass + 408L, addr);
            Unsafe.instance.putInt(addr, needed);
            for (int i = 0; i < needed; i++) {
                Unsafe.instance.putAddress(addr + (i + 1) * 8L, getKlass(interfaces[i]));
            }
        } else {
            System.out.println("Using old.");
            Unsafe.instance.putInt(old, needed);
            for (int i = 0; i < needed; i++) {
                Unsafe.instance.putAddress(old + (i + 1) * 8L, getKlass(interfaces[i]));
            }
        }

    }

    public static void setInterfaces(long klass, long... interfaces) {
        System.out.println("Overwriting interfaces of klass at:" + Long.toHexString(klass));
        long old = getLocalInterfaces(klass);
        int old_count = Unsafe.instance.getInt(old);
        int needed = interfaces.length;
        System.out.println("We have " + old_count + ",and we need " + needed);
        if (needed > old_count) {
            System.out.println("Over limit. Allocating memory.");
            if (AllocatedMemories.contains(old)) {
                System.out.println("We have allocated memory before. Freeing them.");
                Unsafe.instance.freeMemory(old);
                AllocatedMemories.remove(old);
            } else {
                AllocatedMemories.add(old);
            }
            long addr = Unsafe.instance.allocateMemory((needed + 1) * 8L);
            System.out.println("New:" + Long.toHexString(addr));
            Unsafe.instance.putAddress(klass + 32L, addr);
            Unsafe.instance.putAddress(klass + 408L, addr);
            Unsafe.instance.putInt(addr, needed);
            for (int i = 0; i < needed; i++) {
                Unsafe.instance.putAddress(addr + (i + 1) * 8L, interfaces[i]);
            }
        } else {
            System.out.println("Using old.");
            Unsafe.instance.putInt(old, needed);
            for (int i = 0; i < needed; i++) {
                Unsafe.instance.putAddress(old + (i + 1) * 8L, interfaces[i]);
            }
        }

    }

    public static void setInterfaces(Class<?> klass, long... interfaces) {
        setInterfaces(getKlass(klass), interfaces);
    }

    public static void setInterfaces(Class<?> klass, Class<?>... interfaces) {
        setInterfaces(getKlass(klass), interfaces);
    }

    public static long getNextSibling(long klass) {
        return Unsafe.instance.getAddress(klass + 128L);
    }

    public static long getNextSibling(Class<?> klass) {
        return getNextSibling(getKlass(klass));
    }

    public static void printClass(Class<?> clazz) {
        System.out.println("Name:" + getClassName(clazz) + "@" + Long.toHexString(getKlass(clazz)));
        System.out.println("Super:" + getClassName(getSuperClass(clazz)) + "@" + Long.toHexString(getSuperClass(clazz)));
        long[] interfaces = getInterfaces(clazz);
        if (interfaces.length != 0) {
            System.out.println("Interfaces:");
            for (long l : interfaces) {
                System.out.println("----" + getClassName(l) + "@" + Long.toHexString(l));
            }
        }
        System.out.println("ClassloaderData@" + Long.toHexString(getClassLoaderDATA(clazz)));
        System.out.println("ConstantPool@" + Long.toHexString(getConstantPool(clazz)));
        System.out.println();
        System.out.println();
    }

    public static long getConstantPoolTagsByCP(long cp) {
        return Unsafe.instance.getAddress(cp + 8L);
    }


    public static long getConstantPoolTagsByKlass(Class<?> clazz) {
        return getConstantPoolTagsByCP(getConstantPool(clazz));
    }

    public static long getConstantPoolTagsByKlass(long clazz) {
        return getConstantPoolTagsByCP(getConstantPool(clazz));
    }
}
