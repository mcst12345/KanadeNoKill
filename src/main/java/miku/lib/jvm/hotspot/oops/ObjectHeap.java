package miku.lib.jvm.hotspot.oops;


import one.helfy.JVM;
import one.helfy.Type;

public class ObjectHeap {
    private static final int oopSize;
    private static final int byteSize;
    private static final int charSize;
    private static final int booleanSize;
    private static final int intSize;
    private static final int shortSize;
    private static final int longSize;
    private static final int floatSize;
    private static final int doubleSize;

    static {
        oopSize = JVM.intConstant("oopSize");
        byteSize = JVM.type("jbyte").size;
        charSize = JVM.type("jchar").size;
        booleanSize = JVM.type("jboolean").size;
        intSize = JVM.type("jint").size;
        shortSize = JVM.type("jshort").size;
        longSize = JVM.type("jlong").size;
        floatSize = JVM.type("jfloat").size;
        doubleSize = JVM.type("jdouble").size;
    }

    private static final long boolArrayKlassAddress;
    private static final long byteArrayKlassAddress;
    private static final long charArrayKlassAddress;
    private static final long intArrayKlassAddress;
    private static final long shortArrayKlassAddress;
    private static final long longArrayKlassAddress;
    private static final long singleArrayKlassAddress;
    private static final long doubleArrayKlassAddress;

    static {
        Type universeType = JVM.type("Universe");
        boolArrayKlassAddress = universeType.global("_boolArrayKlassObj");
        byteArrayKlassAddress = universeType.global("_byteArrayKlassObj");
        charArrayKlassAddress = universeType.global("_charArrayKlassObj");
        intArrayKlassAddress = universeType.global("_intArrayKlassObj");
        shortArrayKlassAddress = universeType.global("_shortArrayKlassObj");
        longArrayKlassAddress = universeType.global("_longArrayKlassObj");
        singleArrayKlassAddress = universeType.global("_singleArrayKlassObj");
        doubleArrayKlassAddress = universeType.global("_doubleArrayKlassObj");
    }

    private class LiveRegionsCollector {
    }
}
