package miku.lib.jvm.hotspot.compiler;

import miku.lib.jvm.hotspot.oops.Metadata;
import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

public class CompilerTask extends VMObject {
    private static final long _method_offset;
    private static final long _osr_bci_offset;
    private static final long _comp_level_offset;

    static {
        Type type = JVM.type("CompileTask");
        _method_offset = type.offset("_method");
        _osr_bci_offset = type.offset("_osr_bci");
        _comp_level_offset = type.offset("_comp_level");
    }

    public CompilerTask(long address) {
        super(address);
    }

    public Method method() {
        return (Method) Metadata.instantiateWrapperFor(unsafe.getAddress(getAddress() + _method_offset));
    }

    public int osrBci() {
        return unsafe.getInt(getAddress() + _osr_bci_offset);
    }

    public int compLevel() {
        return unsafe.getInt(getAddress() + _comp_level_offset);
    }
}
