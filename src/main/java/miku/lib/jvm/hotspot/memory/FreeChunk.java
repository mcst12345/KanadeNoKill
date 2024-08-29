package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.oops.Mark;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;

public class FreeChunk extends VMObject {

    private static final long _next_offset;
    private static final long _prev_offset;
    private static final long _size_offset;

    static {
        Type type = JVM.type("FreeChunk");
        _next_offset = type.offset("_next");
        _prev_offset = type.offset("_prev");
        _size_offset = type.offset("_size");
    }

    public FreeChunk(long address) {
        super(address);
    }

    public FreeChunk next() {
        return new FreeChunk(unsafe.getAddress(getAddress() + _next_offset));
    }

    public FreeChunk prev() {
        long address = unsafe.getAddress(getAddress() + _prev_offset);
        address = AddressCalculator.andWithMask(address, -4L);
        return new FreeChunk(address);
    }

    public long size() {
        if (VM.compressedOopsEnabled) {
            Mark mark = new Mark(getAddress() + _size_offset);
            return mark.getSize();
        } else {
            return unsafe.getAddress(getAddress() + _size_offset);
        }
    }

    public static boolean indicatesFreeChunk(long cur) {
        FreeChunk fc = new FreeChunk(cur);
        return fc.isFree();
    }

    public boolean isFree() {
        if (VM.compressedOopsEnabled) {
            Mark mark = new Mark(getAddress() + _size_offset);
            return mark.isCmsFreeChunk();
        } else {
            long prev = unsafe.getAddress(getAddress() + _prev_offset);
            long word = unsafe.getAddress(prev);
            return (word & 1L) == 1L;
        }
    }
}
