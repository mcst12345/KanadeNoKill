package miku.lib.utils;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class InternalUtils {
    private static final MethodHandles.Lookup lookup;
    private static Unsafe unsafe = null;

    static {
        Unsafe tmp = null;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            tmp = (Unsafe) f.get(null);
        } catch (Throwable t) {
            String name = "duihqwuidhqiadewdfwqio";
            ClassWriter cw = new ClassWriter(3);
            cw.visit(52, Opcodes.ACC_PUBLIC, name, null, "sun/reflect/MagicAccessorImpl", null);
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitFieldInsn(Opcodes.GETSTATIC, "sun/misc/Unsafe", "theUnsafe", "Lsun/misc/Unsafe;");
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "miku/lain/InternalUtils", "unsafe", "Lsun/misc/Unsafe;");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
            cw.visitEnd();
            byte[] cls = cw.toByteArray();
            ClassLoader cl = new ClassLoader(ClassLoader.getSystemClassLoader()) {
                @Override
                public void clearAssertionStatus() {
                    Class<?> clz = super.defineClass(name, cls, 0, cls.length);
                    try {
                        clz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException("Failed to get unsafe!", e);
                    }
                }
            };
            cl.clearAssertionStatus();
        }
        if (unsafe == null) {
            if (tmp == null) {
                throw new RuntimeException("Failed to get unsafe!");
            }
            unsafe = tmp;
        }

        try {
            Class<?> lk = MethodHandles.Lookup.class;
            lookup = (MethodHandles.Lookup) unsafe.allocateInstance(lk);
            Field f = lk.getDeclaredField("allowedModes");
            long offset = unsafe.objectFieldOffset(f);
            unsafe.putIntVolatile(lookup, offset, 15);
            f = lk.getDeclaredField("lookupClass");
            offset = unsafe.objectFieldOffset(f);
            unsafe.putObjectVolatile(lookup, offset, Class.class);
        } catch (InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException("Failed to init MethodLookup!", e);
        }
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }

    public static MethodHandles.Lookup getLookup() {
        return lookup;
    }
}
