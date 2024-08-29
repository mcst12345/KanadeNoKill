package one.helfy;

import miku.lib.utils.InternalUtils;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class FieldInfo {
    static final JVM jvm = JVM.getInstance();
    static final int oopSize = JVM.intConstant("oopSize");

    public static void main(String[] args) {
        System.out.println("--- Fields visible through Reflection ---");

        for (java.lang.reflect.Field f : Throwable.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            System.out.println(f.getName());
        }

        System.out.println("--- Fields visible through VM Structs ---");

        InstanceKlass.printFields(Throwable.class);
    }

    static class Symbol {
        static final long _length = JVM.type("Symbol").offset("_length");
        static final long _body = JVM.type("Symbol").offset("_body");

        static String asString(long symbol) {
            int length = jvm.getShort(symbol + _length) & 0xffff;

            byte[] data = new byte[length];
            for (int i = 0; i < data.length; i++) {
                data[i] = JVM.getByte(symbol + _body + i);
            }

            return new String(data, StandardCharsets.UTF_8);
        }
    }

    static class ConstantPool {
        static final long _header_size = JVM.type("ConstantPool").size;

        static long at(long cpool, int index) {
            return JVM.getAddress(cpool + _header_size + (long) index * oopSize);
        }
    }

    static class InstanceKlass {
        static final long _klass_offset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        static final long _constants = JVM.type("InstanceKlass").offset("_constants");
        static final long _fields = JVM.type("InstanceKlass").offset("_fields");
        static final long _java_fields_count = JVM.type("InstanceKlass").offset("_java_fields_count");
        static final long _fields_data = JVM.type("Array<u2>").offset("_data");

        static long fromJavaClass(Class cls) {
            return InternalUtils.getUnsafe().getLong(cls, _klass_offset);
        }

        static void printFields(Class cls) {
            long klass = fromJavaClass(cls);
            long cpool = JVM.getAddress(klass + _constants);
            long fields = JVM.getAddress(klass + _fields);

            int fieldCount = jvm.getShort(klass + _java_fields_count) & 0xffff;

            int accessFlagsOffset = JVM.intConstant("FieldInfo::access_flags_offset");
            int nameIndexOffset = JVM.intConstant("FieldInfo::name_index_offset");
            int lowPackedOffset = JVM.intConstant("FieldInfo::low_packed_offset");
            int fieldSlots = JVM.intConstant("FieldInfo::field_slots");
            int tagSize = JVM.intConstant("FIELDINFO_TAG_SIZE");

            for (int i = 0; i < fieldCount; i++) {
                long f = fields + _fields_data + (long) i * fieldSlots * 2;

                short accessFlags = jvm.getShort(f + accessFlagsOffset * 2L);
                if (Modifier.isStatic(accessFlags)) {
                    continue;
                }

                int nameIndex = jvm.getShort(f + nameIndexOffset * 2L) & 0xffff;
                String name = Symbol.asString(ConstantPool.at(cpool, nameIndex));
                int offset = (jvm.getShort(f + lowPackedOffset * 2L) & 0xffff) >>> tagSize;

                System.out.printf("  %2d  %s\n", offset, name);
            }
        }
    }
}
