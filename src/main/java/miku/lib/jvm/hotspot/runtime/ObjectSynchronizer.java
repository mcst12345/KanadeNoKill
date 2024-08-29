package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Mark;
import miku.lib.jvm.hotspot.oops.Oop;
import one.helfy.JVM;
import one.helfy.Type;

public class ObjectSynchronizer {
    private static long gBlockListAddr;
    private static int blockSize;
    private static long objectMonitorTypeSize;

    static {
        JVM jvm = JVM.getInstance();
        Type type;
        try {
            type = jvm.type("ObjectSynchronizer");
            gBlockListAddr = type.global("gBlockList");
            //gBlockListAddr = blockListField.getValue();
            blockSize = jvm.intConstant("ObjectSynchronizer::_BLOCKSIZE");
        } catch (RuntimeException ignored) {
        }

        type = jvm.type("ObjectMonitor");
        objectMonitorTypeSize = type.size;
    }

    public static long identityHashValueFor(Oop obj) {
        Mark mark = obj.getMark();
        if (mark.isUnlocked()) {
            return mark.hash();
        } else if (mark.hasMonitor()) {
            ObjectMonitor monitor = mark.monitor();
            Mark temp = monitor.header();
            return temp.hash();
        } else {
            if (mark.hasDisplacedMarkHelper()) {
                Mark temp = mark.displacedMarkHelper();
                return temp.hash();
            } else {
                return 0L;
            }
        }
    }
}
