package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.oops.Oop;
import miku.lib.jvm.hotspot.oops.Symbol;

public class TwoOopHashtable extends Hashtable {
    protected TwoOopHashtable(long address) {
        super(address);
    }

    public long computeHash(Symbol name, Oop loader) {
        return (long) (name.identityHash() ^ (int) (loader == null ? 0L : loader.identityHash())) & 4294967295L;
    }

    public int indexFor(Symbol name, Oop loader) {
        return this.hashToIndex(this.computeHash(name, loader));
    }
}
