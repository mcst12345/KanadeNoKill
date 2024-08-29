package miku.lib.jvm.hotspot.oops;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;
import one.helfy.JVM;
import one.helfy.Type;

public class CheckedExceptionElement {
    private static final long class_cp_index_offset;

    private long handle;
    private long offset;

    static {
        Type type = JVM.type("CheckedExceptionElement");
        class_cp_index_offset = type.offset("class_cp_index");
    }

    public CheckedExceptionElement(long handle, long offset) {
        this.handle = handle;
        this.offset = offset;
    }

    public int getClassCPIndex() {
        return (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(handle + offset + class_cp_index_offset, 2), true);
    }
}
