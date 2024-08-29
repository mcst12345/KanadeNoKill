package miku.lib.jvm.hotspot.oops;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;
import one.helfy.JVM;
import one.helfy.Type;

public class LocalVariableTableElement {
    private static final long start_bci_offset;
    private static final long length_offset;
    private static final long name_cp_index_offset;
    private static final long descriptor_cp_index_offset;
    private static final long signature_cp_index_offset;
    private static final long slot_offset;

    static {
        Type type = JVM.type("LocalVariableTableElement");
        start_bci_offset = type.offset("start_bci");
        length_offset = type.offset("length");
        name_cp_index_offset = type.offset("name_cp_index");
        descriptor_cp_index_offset = type.offset("descriptor_cp_index");
        signature_cp_index_offset = type.offset("signature_cp_index");
        slot_offset = type.offset("slot");
    }

    private long handle;
    private long offset;

    public LocalVariableTableElement(long handle, long offset) {
        this.handle = handle;
        this.offset = offset;
    }

    public int getStartBCI() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(handle + offset + start_bci_offset, 2), true);
    }

    public int getLength() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(handle + offset + length_offset, 2), true);
    }

    public int getNameCPIndex() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(handle + offset + name_cp_index_offset, 2), true);
    }

    public int getDescriptorCPIndex() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(handle + offset + descriptor_cp_index_offset, 2), true);
    }

    public int getSignatureCPIndex() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(handle + offset + signature_cp_index_offset, 2), true);
    }

    public int getSlot() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(handle + offset + slot_offset, 2), true);
    }

}
