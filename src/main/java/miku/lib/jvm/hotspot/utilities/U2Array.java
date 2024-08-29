package miku.lib.jvm.hotspot.utilities;

import one.helfy.JVM;
import one.helfy.Type;

public class U2Array extends GenericArray {
    protected static Type elemType = JVM.type("u2");
    private static final long dataFieldOffset = JVM.type("Array<u2>").offset("_data");

    public U2Array(long address) {
        super(address, dataFieldOffset);
    }

    @Override
    public Type getElemType() {
        return elemType;
    }

    public short at(int i) {
        return (short) this.getIntegerAt(i);
    }
}
