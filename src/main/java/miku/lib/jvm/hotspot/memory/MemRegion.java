package miku.lib.jvm.hotspot.memory;

import miku.lib.utils.AddressCalculator;
import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

//MemRegion @ 16
//  HeapWord* _start @ 0
//  size_t _word_size @ 8

public class MemRegion implements Cloneable {

    private static final long _start_offset;
    private static final long _word_size_offset;

    static {
        Type type = JVM.type("MemRegion");
        _start_offset = type.offset("_start");
        _word_size_offset = type.offset("_word_size");
    }

    private long byteSize;
    private long start;

    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public long start() {
        return this.start;
    }

    public long end() {
        return this.start + byteSize;
    }

    public MemRegion() {
    }

    public MemRegion(long memRegionAddr) {
        this(InternalUtils.getUnsafe().getAddress(memRegionAddr + _start_offset), unsafe
                .getLong(memRegionAddr + _word_size_offset), true);
    }

    public MemRegion(long start, long arg2, boolean isWordSize) {
        this.setStart(start);
        if (!isWordSize) {
            this.byteSize = AddressCalculator.minus(arg2, start);
        } else {
            this.setWordSize(arg2);
        }
    }

    public Object clone() {
        return new MemRegion(this.start, this.byteSize, true);
    }

    public MemRegion copy() {
        return (MemRegion) this.clone();
    }

    public MemRegion intersection(MemRegion mr2) {
        MemRegion res = new MemRegion();
        if (AddressCalculator.greaterThan(mr2.start(), this.start())) {
            res.setStart(mr2.start());
        } else {
            res.setStart(this.start());
        }

        long end = this.end();
        long mr2End = mr2.end();
        long resEnd;
        if (AddressCalculator.lessThan(end, mr2End)) {
            resEnd = end;
        } else {
            resEnd = mr2End;
        }

        if (AddressCalculator.lessThan(resEnd, res.start())) {
            res.setStart(0);
            res.setWordSize(0L);
        } else {
            res.setEnd(resEnd);
        }

        return res;
    }

    public MemRegion union(MemRegion mr2) {
        MemRegion res = new MemRegion();
        if (AddressCalculator.lessThan(mr2.start(), this.start())) {
            res.setStart(mr2.start());
        } else {
            res.setStart(this.start());
        }

        long end = this.end();
        long mr2End = mr2.end();
        long resEnd;
        if (AddressCalculator.greaterThan(end, mr2End)) {
            resEnd = end;
        } else {
            resEnd = mr2End;
        }

        res.setEnd(resEnd);
        return res;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {//end.minus(this.start)
        this.byteSize = AddressCalculator.minus(end, this.start);
    }

    public void setWordSize(long wordSize) {
        this.byteSize = InternalUtils.getUnsafe().addressSize() * wordSize;
    }


    public boolean contains(MemRegion mr2) {
        return AddressCalculator.lessThanOrEqual(this.start, mr2.start) && AddressCalculator.greaterThanOrEqual(this.end(), mr2.end());
    }

    public boolean contains(long addr) {
        return AddressCalculator.greaterThanOrEqual(addr, this.start) && AddressCalculator.lessThan(addr, this.end());
    }

    public long byteSize() {
        return this.byteSize;
    }

    public long wordSize() {
        return this.byteSize / InternalUtils.getUnsafe().addressSize();
    }


}
