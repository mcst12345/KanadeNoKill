package miku.lib.jvm.hotspot.utilities;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

import java.lang.reflect.InvocationTargetException;

public class BasicHashtableEntry extends VMObject {
    private static final long _next_offset;
    private static final long _hash_offset;

    static {
        Type type = JVM.type("BasicHashtableEntry<mtInternal>");
        _next_offset = type.offset("_next");
        _hash_offset = type.offset("_hash");
    }


    private long _next;
    private long _hash;

    public BasicHashtableEntry(long address) {
        super(address);
        _next = unsafe.getAddress(address + _next_offset);
        _hash = NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + _hash_offset, VM.JIntSize), true);
    }

    public BasicHashtableEntry next() {
        if (_next == 0) {
            return null;
        }
        try {
            return getClass().getConstructor(long.class).newInstance(_next);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public long hash() {
        return _hash & 4294967295L;
    }
}
