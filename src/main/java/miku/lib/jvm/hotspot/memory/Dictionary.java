package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.utilities.BasicHashtableEntry;
import miku.lib.jvm.hotspot.utilities.TwoOopHashtable;

import java.util.Objects;

public class Dictionary extends TwoOopHashtable {
    public Dictionary(long address) {
        super(address);
    }

    @Override
    protected Class<? extends BasicHashtableEntry> getHashtableEntryClass() {
        return DictionaryEntry.class;
    }

    public void classesDo(SystemDictionary.ClassVisitor v) {
        int tblSize = this.size();
        for (int index = 0; index < tblSize; ++index) {
            for (DictionaryEntry probe = (DictionaryEntry) this.bucket(index); probe != null; probe = (DictionaryEntry) probe.next()) {
                Klass k = probe.klass();
                v.visit(k);
            }
        }

    }
}
