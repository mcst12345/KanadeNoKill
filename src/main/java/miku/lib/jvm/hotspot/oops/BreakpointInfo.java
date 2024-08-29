package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

public class BreakpointInfo extends VMObject {
    private static final long _orig_bytecode_offset;
    private static final long _bci_offset;
    private static final long _name_index;
    private static final long _signature_index_offset;
    private static final long _next_offset;

    static {
        Type type = JVM.type("BreakpointInfo");
        _orig_bytecode_offset = type.offset("_orig_bytecode");
        _bci_offset = type.offset("_bci");
        _name_index = type.offset("_name_index");
        _signature_index_offset = type.offset("_signature_index");
        _next_offset = type.offset("_next");
    }

    public BreakpointInfo(long address) {
        super(address);
    }

    public int getOrigBytecode() {
        return unsafe.getInt(getAddress() + _orig_bytecode_offset);
    }

    public int getBCI() {
        return unsafe.getInt(getAddress() + _bci_offset);
    }

    public short getNameIndex() {
        return unsafe.getShort(getAddress() + _name_index);
    }

    public short getSignatureIndex() {
        return unsafe.getShort(getAddress() + _signature_index_offset);
    }

    public BreakpointInfo getNext() {
        return new BreakpointInfo(unsafe.getAddress(getAddress() + _next_offset));
    }

    public boolean match(Method m, int bci) {
        return bci == this.getBCI() && this.match(m);
    }

    public boolean match(Method m) {
        return this.getNameIndex() == m.getNameIndex() && this.getSignatureIndex() == m.getSignatureIndex();
    }
}
