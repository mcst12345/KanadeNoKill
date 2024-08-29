package miku.lib.jvm.hotspot.oops;

import one.helfy.JVM;
import one.helfy.Type;

public class Instance extends Oop {
    public Instance(long address) {
        super(address);
    }

    private static final long typeSize;

    static {
        Type type = JVM.type("instanceOopDesc");
        typeSize = type.size;
    }


}
