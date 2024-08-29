package one.helfy.vmstruct;

import one.helfy.JVM;

public class ConstantPool {
    private static final JVM jvm = JVM.getInstance();
    private static final int wordSize = JVM.intConstant("oopSize");
    private static final long _header_size = JVM.type("ConstantPool").size;
    private static final long _pool_holder = JVM.type("ConstantPool").offset("_pool_holder");

    public static long holder(long cpool) {
        return JVM.getAddress(cpool + _pool_holder);
    }

    public static long at(long cpool, int index) {
        return JVM.getAddress(cpool + _header_size + (long) index * wordSize);
    }
}

