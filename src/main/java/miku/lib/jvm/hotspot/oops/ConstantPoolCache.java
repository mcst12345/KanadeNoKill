package miku.lib.jvm.hotspot.oops;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;
import miku.lib.jvm.hotspot.runtime.VM;
import one.helfy.JVM;
import one.helfy.Type;

public class ConstantPoolCache extends Metadata {
    public ConstantPoolCache(long address) {
        super(address);
    }

    private static final long intSize = VM.JIntSize;
    private static long length_offset;
    private static long baseOffset;
    private static long constants_offset;
    private static long elementSize;

    static {
        Type type = JVM.type("ConstantPoolCache");
        baseOffset = type.size;
        length_offset = type.offset("_length");
        constants_offset = type.offset("_constant_pool");
        Type elType = JVM.type("ConstantPoolCacheEntry");
        elementSize = elType.size;
    }

    public ConstantPool getConstants() {
        return new ConstantPool(unsafe.getAddress(getAddress() + constants_offset));
    }

    public long getSize() {
        return Oop.alignObjectSize(baseOffset + (long) this.getLength() * elementSize);
    }

    public int getLength() {
        return unsafe.getInt(getAddress() + length_offset);
    }

    public ConstantPoolCacheEntry getEntryAt(int i) {
        if (i >= 0 && i < this.getLength()) {
            return new ConstantPoolCacheEntry(this, i);
        } else {
            throw new IndexOutOfBoundsException(i + " " + this.getLength());
        }
    }

    public int getIntAt(int entry, int fld) {
        long offset = baseOffset + (long) entry * elementSize + (long) fld * intSize;
        //return (int)this.getAddress().getCIntegerAt(offset, intSize, true);
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + offset, (int) intSize), true);
    }
}
