package miku.lib.utils;

import one.helfy.JVM;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.lang.reflect.Modifier;

public class NoPrivateOrProtected {

    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static void FuckAccess(Class<?> cls) {
        InternalUtils.getUnsafe().ensureClassInitialized(cls);
        long _fields = JVM.type("InstanceKlass").offset("_fields");
        long _java_fields_count = JVM.type("InstanceKlass").offset("_java_fields_count");
        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long _access_flags_offset = JVM.type("Method").offset("_access_flags");
        long klass = oopSize == 8 ? unsafe.getLong(cls, klassOffset) : unsafe.getInt(cls, klassOffset) & 0xffffffffL;

        unsafe.putInt(klass + JVM.type("Klass").offset("_access_flags"), Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER);
        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");
        for (int i = 0; i < methodCount; i++) {
            long method = JVM.getAddress(methods + (long) i * oopSize);
            int old = unsafe.getShort(method + _access_flags_offset);
            int neo = Opcodes.ACC_PUBLIC;
            if (Modifier.isStatic(old)) {
                neo |= Opcodes.ACC_STATIC;
            }
            if (Modifier.isSynchronized(old)) {
                neo |= Opcodes.ACC_SYNCHRONIZED;
            }
            if (Modifier.isAbstract(old)) {
                neo |= Opcodes.ACC_ABSTRACT;
            }
            if (Modifier.isStrict(old)) {
                neo |= Opcodes.ACC_STRICT;
            }
            if (Modifier.isNative(old)) {
                neo |= Opcodes.ACC_NATIVE;
            }
            unsafe.putInt(method + _access_flags_offset, neo);
        }
        long fields = unsafe.getAddress(klass + _fields);
        int fieldCount = unsafe.getShort(klass + _java_fields_count);
        for (int i = 0; i < fieldCount; i++) {
            long field = fields + 4 + i * 2 * 6;
            int old = unsafe.getShort(field);
            int neo = Opcodes.ACC_PUBLIC;
            if (Modifier.isStatic(old)) {
                neo |= Opcodes.ACC_STATIC;
            }
            if (Modifier.isVolatile(old)) {
                neo |= Opcodes.ACC_VOLATILE;
            }
            unsafe.putShort(field, (short) neo);
        }
    }

    public static void main(String[] args) {
        JVM jvm = JVM.getInstance();
        System.out.println(JVM.type("InstanceKlass"));
    }
}
