package miku.lib.jvm.hotspot.oops;

//ExceptionTableElement @ 8
//  u2 start_pc @ 0
//  u2 end_pc @ 2
//  u2 handler_pc @ 4
//  u2 catch_type_index @ 6

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;
import one.helfy.JVM;
import one.helfy.Type;

public class ExceptionTableElement {
    private static final long start_pc_offset;
    private static final long end_pc_offset;
    private static final long handler_pc_offset;
    private static final long catch_type_index_offset;

    static {
        Type type = JVM.type("ExceptionTableElement");
        start_pc_offset = type.offset("start_pc");
        end_pc_offset = type.offset("end_pc");
        handler_pc_offset = type.offset("handler_pc");
        catch_type_index_offset = type.offset("catch_type_index");
    }

    private long address, offset;

    public ExceptionTableElement(long address, long offset) {
        this.address = address;
        this.offset = offset;
    }

    public int getStartPC() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(address + offset + start_pc_offset, 2), true);
    }

    public int getEndPC() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(address + offset + end_pc_offset, 2), true);
    }

    public int getHandlerPC() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(address + offset + handler_pc_offset, 2), true);
    }

    public int getCatchTypeIndex() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(address + offset + catch_type_index_offset, 2), true);
    }
}
