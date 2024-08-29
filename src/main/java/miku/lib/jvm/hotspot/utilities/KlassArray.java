package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.oops.Metadata;
import one.helfy.JVM;
import one.helfy.Type;

public class KlassArray extends GenericArray {
    private static final long _data_offset;
    protected static final Type elemType;

    static {
        elemType = JVM.type("address");
        Type type = JVM.type("Array<Klass*>");
        _data_offset = type.offset("_data[0]");
    }

    public KlassArray(long address) {
        super(address, _data_offset);
    }

    @Override
    public Type getElemType() {
        return elemType;
    }

    public Klass getAt(int i) {
        return (Klass) Metadata.instantiateWrapperFor(this.getAddressAt(i));
    }
}
