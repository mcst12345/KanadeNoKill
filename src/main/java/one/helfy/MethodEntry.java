package one.helfy;

import miku.lib.utils.InternalUtils;

public class MethodEntry {
    static final JVM jvm = JVM.getInstance();
    static final int oopSize = JVM.intConstant("oopSize");

    private static long benchmark() {
        long sum = 0;
        for (int i = 0; i < 1000000; i++) {
            sum += rdtsc();
        }
        return sum;
    }

    private static void runBenchmark() {
        long startTime = System.nanoTime();
        long sum = benchmark();
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e6 + "  " + sum);
    }

    public static void main(String[] args) throws Exception {
        System.loadLibrary("MethodEntry");

        // Force compilation
        benchmark();

        long method = Method.fromJavaMethod(MethodEntry.class, "rdtsc");
        Method.optimizeEntry(method, "0f3148c1e220480bc2c3cccc");

        for (int i = 0; i < 1000; i++) {
            runBenchmark();
            System.gc();
        }
    }

    private static native long rdtsc();

    static class InstanceKlass {
        static final long _klass_offset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        static final long _methods = JVM.type("InstanceKlass").offset("_methods");
        static final long _methods_data = JVM.type("Array<Method*>").offset("_data");

        static long fromJavaClass(Class cls) {
            return InternalUtils.getUnsafe().getLong(cls, _klass_offset);
        }

        static long methodAt(long klass, int slot) {
            long methods = JVM.getAddress(klass + _methods);
            int length = JVM.getInt(methods);
            if (slot < 0 || slot >= length) {
                throw new IndexOutOfBoundsException("Invalid method slot: " + slot);
            }
            return JVM.getAddress(methods + _methods_data + (long) slot * oopSize);
        }
    }

    static class Method {
        static final long _i2i_entry = JVM.type("Method").offset("_i2i_entry");
        static final long _from_compiled_entry = JVM.type("Method").offset("_from_compiled_entry");
        static final long _from_interpreted_entry = JVM.type("Method").offset("_from_interpreted_entry");
        static final long _native_entry = JVM.type("Method").size;
        static final long _code = JVM.type("Method").offset("_code");
        static final long _verified_entry_point = JVM.type("nmethod").offset("_verified_entry_point");

        static long fromJavaMethod(Class<?> cls, String name, Class<?>... parameterTypes) throws ReflectiveOperationException {
            return fromJavaMethod(cls.getDeclaredMethod(name, parameterTypes));
        }

        static long fromJavaMethod(java.lang.reflect.Method m) throws ReflectiveOperationException {
            long klass = InstanceKlass.fromJavaClass(m.getDeclaringClass());
            java.lang.reflect.Field f = m.getClass().getDeclaredField("slot");
            f.setAccessible(true);
            int slot = f.getInt(m);
            return InstanceKlass.methodAt(klass, slot);
        }

        static void optimizeEntry(long method, String asm) {
            long code = JVM.getAddress(method + _code);
            long entry = JVM.getAddress(code + _verified_entry_point);

            JVM.putAddress(method + _i2i_entry, entry);
            JVM.putAddress(method + _from_compiled_entry, entry);
            JVM.putAddress(method + _from_interpreted_entry, entry);

            int length = asm.length();
            for (int i = 0; i < length; i += 2) {
                byte b = (byte) Integer.parseInt(asm.substring(i, i + 2), 16);
                jvm.putByte(entry++, b);
            }
        }
    }
}
