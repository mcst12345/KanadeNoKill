package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.classfile.ClassLoaderData;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.oops.Metadata;
import miku.lib.jvm.hotspot.oops.Oop;
import miku.lib.jvm.hotspot.utilities.HashtableEntry;
import one.helfy.Type;

public class DictionaryEntry extends HashtableEntry {
    public DictionaryEntry(long address) {
        super(address);
    }

    public Klass klass() {
        if (literal() == 0) {
            return null;
        }
        return (Klass) Metadata.instantiateWrapperFor(literal());
    }

    public Oop loader() {
        return loaderData().getClassLoader();
    }

    public ClassLoaderData loaderData() {
        return new ClassLoaderData(getAddress() + jvm.type("DictionaryEntry").offset("_loader_data"));
    }
}
