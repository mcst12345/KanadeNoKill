package one.helfy.vmstruct;

import one.helfy.JVM;

public class CodeCache {
    private static final long codeHeap = JVM.getAddress(JVM.type("CodeCache").global("_heap"));
    private static final long _memory = codeHeap + JVM.type("CodeHeap").offset("_memory");
    private static final long _segmap = codeHeap + JVM.type("CodeHeap").offset("_segmap");
    private static final long _log2_segment_size = codeHeap + JVM.type("CodeHeap").offset("_log2_segment_size");
    private static final long _low = JVM.type("VirtualSpace").offset("_low");
    private static final long _high = JVM.type("VirtualSpace").offset("_high");
    private static final long _heap_block_size = JVM.type("HeapBlock").size;
    private static final long _name = JVM.type("CodeBlob").offset("_name");
    private static final long _content_offset = JVM.type("CodeBlob").offset("_content_offset");
    private static final long _oop_maps = JVM.type("CodeBlob").offset("_oop_maps");

    public static boolean contains(long pc) {
        return JVM.getAddress(_memory + _low) <= pc && pc < JVM.getAddress(_memory + _high);
    }

    public static long findBlob(long pc) {
        if (!contains(pc)) {
            return 0;
        }

        long codeHeapStart = JVM.getAddress(_memory + _low);
        int log2SegmentSize = JVM.getInt(_log2_segment_size);

        long i = (pc - codeHeapStart) >>> log2SegmentSize;
        long b = JVM.getAddress(_segmap + _low);

        int v = JVM.getByte(b + i) & 0xff;
        if (v == 0xff) {
            return 0;
        }

        while (v > 0) {
            i -= v;
            v = JVM.getByte(b + i) & 0xff;
        }

        long heapBlock = codeHeapStart + (i << log2SegmentSize);
        return heapBlock + _heap_block_size;
    }

    public static long getOopMaps(long cb) {
        return JVM.getAddress(cb + _oop_maps);
    }

    public static long getOopMapForReturnAddress(long cb, long retAddr) {
        long oopMaps = getOopMaps(cb);
        if (oopMaps == 0) {
            return 0;
        }
        return OopMapSet.findMapAtOffset(oopMaps, retAddr - codeBegin(cb));
        //return this.getOopMaps().findMapAtOffset(returnAddress.minus(this.codeBegin()), debugging);
    }

    public static long codeBegin(long cb) {
        return cb + JVM.getInt(cb + _content_offset);
    }
}