package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Mark;

public class ObjectMonitor extends VMObject {
    public ObjectMonitor(long address) {
        super(address);
    }

    public Mark header() {
        return new Mark(getAddress() + jvm.type("ObjectMonitor").offset("_header"));
    }
}
