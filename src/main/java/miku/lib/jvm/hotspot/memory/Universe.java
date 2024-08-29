package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.BasicType;

public class Universe {
    public static boolean elementTypeShouldBeAligned(BasicType type) {
        return type == BasicType.T_DOUBLE || type == BasicType.T_LONG;
    }
}
