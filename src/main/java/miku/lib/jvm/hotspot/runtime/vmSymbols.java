package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Symbol;
import one.helfy.JVM;
import one.helfy.Type;

public class vmSymbols {
    private static final long symbolsAddress;
    private static final int FIRST_SID;
    private static final int SID_LIMIT;

    static {
        JVM jvm = JVM.getInstance();
        Type type = jvm.type("vmSymbols");
        symbolsAddress = type.global("_symbols[0]");
        FIRST_SID = jvm.intConstant("vmSymbols::FIRST_SID");
        SID_LIMIT = jvm.intConstant("vmSymbols::SID_LIMIT");
    }

    public static Symbol symbolAt(int id) {
        if (id >= FIRST_SID && id < SID_LIMIT) {
            return new Symbol(symbolsAddress + (long) id * VM.AddressSize);
        } else {
            throw new IndexOutOfBoundsException("bad SID " + id);
        }
    }
}
