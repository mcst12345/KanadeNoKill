package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.gc_interface.CollectedHeapName;

public class SharedHeap extends CollectedHeap {
    public SharedHeap(long address) {
        super(address);
    }


    public CollectedHeapName kind() {
        return CollectedHeapName.SHARED_HEAP;
    }
}
