package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.Type;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class HashtableBucket extends VMObject {
    private long _entry;

    protected HashtableBucket(long address) {
        super(address);
        //System.out.println("a."+address);
        Type type = jvm.type("HashtableBucket<mtInternal>");
        _entry = unsafe.getAddress(address + type.offset("_entry"));
        //System.out.println("e."+_entry);

    }

    public BasicHashtableEntry getEntry(Class<? extends VMObject> clazz) {
        if (_entry == 0) {
            return null;
        }
        try {
            return (BasicHashtableEntry) clazz.getConstructor(long.class).newInstance(_entry);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
