package miku.lib.jvm.hotspot.runtime;

import miku.lib.utils.AddressCalculator;
import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import sun.misc.Unsafe;

public abstract class VMObject {
    public static final Unsafe unsafe = InternalUtils.getUnsafe();
    public static final JVM jvm = JVM.getInstance();
    private final long address;

    public VMObject(long address) {
        this.address = address;
    }

    public String toString() {
        return this.getClass().getName() + "@" + this.address;
    }

    public final long getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        return (int) address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VMObject) {
            return ((VMObject) obj).getAddress() == getAddress();
        }
        return false;
    }

    public boolean lessThan(VMObject a) {
        return lessThan(a.getAddress());
    }

    public boolean lessThan(long a) {
        return AddressCalculator.lessThan(address, a);
    }

    public long minus(VMObject arg) {
        return minus(arg.address);
    }

    public long minus(long arg) {
        return AddressCalculator.minus(address, arg);
    }

    public boolean lessThanOrEqual(VMObject a) {
        return lessThanOrEqual(a.getAddress());
    }

    public boolean lessThanOrEqual(long a) {
        return AddressCalculator.lessThanOrEqual(address, a);
    }

    public boolean greaterThan(VMObject a) {
        return greaterThan(a.getAddress());
    }

    public boolean greaterThan(long a) {
        return AddressCalculator.greaterThan(address, a);
    }

    public boolean greaterThanOrEqual(VMObject a) {
        return greaterThanOrEqual(a.getAddress());
    }

    public boolean greaterThanOrEqual(long a) {
        return AddressCalculator.greaterThanOrEqual(address, a);
    }

    public long andWithMask(long mask) {
        return AddressCalculator.andWithMask(address, mask);
    }

    public long orWithMask(long mask) {
        return AddressCalculator.orWithMask(address, mask);
    }

    public long xorWithMask(long mask) {
        return AddressCalculator.xorWithMask(address, mask);
    }
}
