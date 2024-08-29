package miku.lib.jvm.hotspot.utilities;

import one.helfy.JVM;
import one.helfy.Type;

public class U1Array extends GenericArray {

    protected static final Type elemType = JVM.type("u1");
    private static final long dataFieldOffset = JVM.type("Array<u1>").offset("_data");

    public U1Array(long address) {
        super(address, dataFieldOffset);
    }

    @Override
    public Type getElemType() {
        return elemType;
    }

    public byte at(int i) {
        if (i >= 0 && i < this.length()) {
            Type elemType = this.getElemType();
            long data = this.getAddress() + dataFieldOffset;
            long elemSize = elemType.size;
            return unsafe.getByte(data + (long) i * elemSize);
        } else {
            throw new ArrayIndexOutOfBoundsException(i + " " + this.length());
        }
    }
}
