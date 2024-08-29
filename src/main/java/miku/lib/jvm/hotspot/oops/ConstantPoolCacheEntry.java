package miku.lib.jvm.hotspot.oops;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.InternalUtils;
import miku.lib.utils.NumberTransformer;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

//ConstantPoolCacheEntry @ 32
//  intx _indices @ 0
//  volatile Metadata* _f1 @ 8
//  intx _f2 @ 16
//  intx _flags @ 24

public class ConstantPoolCacheEntry {
    private static long size;
    private static long baseOffset;
    private ConstantPoolCache cp;
    private long offset;
    private static long indices_offset;
    private static long indices_size;
    private static boolean indices_unsigned;
    private static long f1_offset;
    private static long f2_offset;
    private static long flags_offset;

    static {
        JVM jvm = JVM.getInstance();
        Type type = JVM.type("ConstantPoolCacheEntry");
        size = type.size;
        f1_offset = type.offset("_f1");
        f2_offset = type.offset("_f2");
        flags_offset = type.offset("_flags");
        Type t = JVM.type(type.field("_indices").typeName);
        indices_offset = type.offset("_indices");
        indices_size = t.size;
        indices_unsigned = t.isUnsigned;

        type = JVM.type("ConstantPoolCache");
        baseOffset = type.size;
    }

    ConstantPoolCacheEntry(ConstantPoolCache cp, int index) {
        this.cp = cp;
        this.offset = baseOffset + (long) index * size;
    }

    public int getConstantPoolIndex() {
        if ((this.getIndices() & 65535L) == 0L) {
            throw new IllegalStateException("must be main entry");
        }

        return (int) (this.getIndices() & 65535L);
    }

    private long getIndices() {
        return NumberTransformer.dataToCInteger(JVMUtil.getBytes(cp.getAddress() + indices_offset + this.offset, (int) indices_size), indices_unsigned);
    }

    public static void main(String[] args) {
        JVM jvm = JVM.getInstance();
        Type type = jvm.type("ConstantPoolCacheEntry");
        System.out.println(type);
    }

    public Metadata getF1() {
        return Metadata.instantiateWrapperFor(unsafe.getAddress(this.cp.getAddress() + f1_offset + this.offset));
    }

    public int getF2() {
        return unsafe.getInt(cp.getAddress() + f1_offset + offset);
    }

    public int getFlags() {
        return unsafe.getInt(cp.getAddress() + flags_offset + offset);
    }

    private static final Unsafe unsafe = InternalUtils.getUnsafe();
}
