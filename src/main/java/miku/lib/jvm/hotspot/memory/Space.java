package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;

import java.util.List;

//Space @ 56
//  HeapWord* _bottom @ 8
//  HeapWord* _end @ 16

public abstract class Space extends VMObject {

    private static final long _bottom_offset;
    private static final long _end_offset;

    static {
        Type type = JVM.type("Space");
        _bottom_offset = type.offset("_bottom");
        _end_offset = type.offset("_end");
    }

    public Space(long address) {
        super(address);
    }

    public long bottom() {
        return unsafe.getAddress(getAddress() + _bottom_offset);
    }

    public long end() {
        return unsafe.getAddress(getAddress() + _end_offset);
    }

    public MemRegion usedRegion() {
        return new MemRegion(this.bottom(), this.end(), false);
    }

    public abstract List<MemRegion> getLiveRegions();

    public long capacity() {//this.end().minus(this.bottom())
        return AddressCalculator.minus(end(), bottom());
    }

    public abstract long used();

    public abstract long free();

    public boolean contains(long p) {
        return AddressCalculator.lessThanOrEqual(this.bottom(), p) && AddressCalculator.greaterThan(this.end(), p);
    }


}
