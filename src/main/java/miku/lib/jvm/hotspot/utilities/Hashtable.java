package miku.lib.jvm.hotspot.utilities;

public class Hashtable extends BasicHashtable {
    protected Hashtable(long address) {
        super(address);
    }

    protected static long hashSymbol(byte[] buf) {
        long h = 0L;
        int s = 0;

        for (int len = buf.length; len-- > 0; ++s) {
            h = 31L * h + (4294967295L & (long) buf[s]);
        }

        return h & 4294967295L;
    }

    public int hashToIndex(long fullHash) {
        return (int) (fullHash % (long) this.size());
    }

    @Override
    protected Class<? extends BasicHashtableEntry> getHashtableEntryClass() {
        return HashtableEntry.class;
    }
}
