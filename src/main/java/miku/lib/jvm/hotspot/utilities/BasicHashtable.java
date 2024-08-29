package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.Type;

public class BasicHashtable extends VMObject {
    private static long bucketSize;
    private int _table_size;
    private int _entry_size;
    private BasicHashtableEntry _free_list;
    private long _buckets;

    protected BasicHashtable(long address) {
        super(address);
        Type type = jvm.type("BasicHashtable<mtInternal>");
        //System.out.println("0x"+Long.toHexString(address));
        //System.out.println(unsafe.getShort(address + type.offset("_table_size")));
        _table_size = unsafe.getInt(address + type.offset("_table_size"));
        _entry_size = unsafe.getInt(address + type.offset("_entry_size"));
        _free_list = new BasicHashtableEntry(address + type.offset("_free_list"));
        _buckets = (address + type.offset("_buckets"));
        bucketSize = jvm.type("HashtableBucket<mtInternal>").size;

    }


    public BasicHashtableEntry bucket(int i) {
        assert i >= 0 && i < _table_size;
        HashtableBucket bucket = new HashtableBucket(unsafe.getAddress(_buckets) + (long) i * bucketSize);
        return bucket.getEntry(getHashtableEntryClass());
    }

    protected Class<? extends BasicHashtableEntry> getHashtableEntryClass() {
        return BasicHashtableEntry.class;
    }

    public int size() {
        return _table_size;
    }
}
