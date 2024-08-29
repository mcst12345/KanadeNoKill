package miku.lib.jvm.hotspot.memory;

import one.helfy.JVM;
import one.helfy.Type;

public abstract class CompactibleSpace extends Space {

    private static final long _compaction_top_offset;

    static {
        Type type = JVM.type("CompactibleSpace");
        _compaction_top_offset = type.offset("_compaction_top");
    }

    public CompactibleSpace(long address) {
        super(address);
    }

    public long compactionTop() {
        return unsafe.getAddress(getAddress() + _compaction_top_offset);
    }
}
