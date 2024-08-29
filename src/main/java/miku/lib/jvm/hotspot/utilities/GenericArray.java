package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

public abstract class GenericArray extends VMObject {

    private static final long _length_offset;
    private static final long _data_offset;

    static {
        Type type = JVM.type("Array<int>");
        _length_offset = type.offset("_length");
        _data_offset = type.offset("_data");
    }

    private static long sizeOfArray;
    private int length;
    private long dataFieldOffset;

    protected GenericArray(long address, long dataFieldOffset) {
        super(address);
        Type type = JVM.type("Array<int>");
        length = unsafe.getInt(address + type.offset("_length"));
        this.dataFieldOffset = dataFieldOffset;
    }

    public int length() {
        return length;
    }

    public abstract Type getElemType();

    protected int getIntegerAt(int index) {
        if (index >= 0 && index < this.length()) {
            Type elemType = this.getElemType();
            long data = this.getAddress() + this.dataFieldOffset;
            long elemSize = elemType.size;
            return unsafe.getInt(data + (long) index * elemSize);
        } else {
            throw new ArrayIndexOutOfBoundsException(index + " " + this.length());
        }
    }

    private long byteSizeof(int length) {
        return sizeOfArray + (long) length * this.getElemType().size;
    }

    protected long getAddressAt(int index) {
        if (index >= 0 && index < this.length()) {
            Type elemType = this.getElemType();
            if (this.getElemType().name.equals("int")) {
                throw new RuntimeException("elemType must not be of CInteger type");
            } else {
                long data = this.getAddress() + this.dataFieldOffset;
                long elemSize = elemType.size;
                return unsafe.getAddress(data + (long) index * elemSize);
            }
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }
}
