package miku.lib.jvm.hotspot.utilities;

public class HashtableEntry extends BasicHashtableEntry {
    private long _literal;

    public HashtableEntry(long address) {
        super(address);
        _literal = unsafe.getAddress(address + jvm.type("IntptrHashtableEntry").offset("_literal"));
    }

    public long literal() {
        return _literal;
    }
}
