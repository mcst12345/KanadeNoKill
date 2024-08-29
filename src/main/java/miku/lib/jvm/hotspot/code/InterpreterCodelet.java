package miku.lib.jvm.hotspot.code;

import miku.lib.jvm.hotspot.utilities.CStringUtilities;
import one.helfy.JVM;
import one.helfy.Type;

public class InterpreterCodelet extends Stub {

    private static final long _size_offset;
    private static final long _description_offset;
    private static final long _bytecode_offset;
    private static final long instanceSize;

    static {
        Type type = JVM.type("InterpreterCodelet");
        _size_offset = type.offset("_size");
        _description_offset = type.offset("_description");
        _bytecode_offset = type.offset("_bytecode");
        instanceSize = type.size;
    }

    public InterpreterCodelet(long address) {
        super(address);
    }


    public int getSize() {
        return unsafe.getInt(getAddress() + _size_offset);
    }

    public long codeBegin() {
        return getAddress() + instanceSize;
    }

    public long codeEnd() {
        return getAddress() + getSize();
    }

    public long codeSize() {
        return codeEnd() - codeBegin();
    }

    public String getDescription() {
        return CStringUtilities.getString(unsafe.getAddress(getAddress() + _description_offset


        ));
    }

}
