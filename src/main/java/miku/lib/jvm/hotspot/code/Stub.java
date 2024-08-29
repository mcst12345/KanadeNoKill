package miku.lib.jvm.hotspot.code;

import miku.lib.jvm.hotspot.runtime.VMObject;

public abstract class Stub extends VMObject {
    public Stub(long address) {
        super(address);
    }

    public abstract int getSize();

    public abstract long codeBegin();

    public abstract long codeEnd();

}
