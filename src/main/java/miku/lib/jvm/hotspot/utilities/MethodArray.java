package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.oops.Metadata;
import miku.lib.jvm.hotspot.oops.Method;
import one.helfy.JVM;
import one.helfy.Type;

public class MethodArray extends GenericArray {
    private static final long _data_offset;
    protected static final Type elemType;

    static {
        elemType = JVM.type("address");
        Type type = JVM.type("Array<Method*>");
        _data_offset = type.offset("_data");
    }

    public MethodArray(long address) {
        super(address, _data_offset);
    }

    public Method at(int id) {
        return (Method) Metadata.instantiateWrapperFor(this.getAddressAt(id));
    }

    @Override
    public Type getElemType() {
        return elemType;
    }
}
