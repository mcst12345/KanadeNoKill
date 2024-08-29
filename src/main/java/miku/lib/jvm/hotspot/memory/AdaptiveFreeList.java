package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

public class AdaptiveFreeList extends VMObject {

    private static final long _size_offset;
    private static final long _count_offset;
    private static final long headerSize;

    static {
        Type type = JVM.type("AdaptiveFreeList<FreeChunk>");
        _size_offset = type.offset("_size");
        _count_offset = type.offset("_count");
        headerSize = type.size;
    }

    public AdaptiveFreeList(long address) {
        super(address);
    }
}
