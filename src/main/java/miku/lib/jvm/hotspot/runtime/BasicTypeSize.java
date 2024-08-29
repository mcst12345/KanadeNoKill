package miku.lib.jvm.hotspot.runtime;

public class BasicTypeSize {
    private static final boolean initialized = false;
    private static final int tBooleanSize = 1;
    private static final int tCharSize = 1;
    private static final int tFloatSize = 1;
    private static final int tDoubleSize = 2;
    private static final int tByteSize = 1;
    private static final int tShortSize = 1;
    private static final int tIntSize = 1;
    private static final int tLongSize = 2;
    private static final int tObjectSize = 1;
    private static final int tArraySize = 1;
    private static final int tVoidSize = 0;

    public BasicTypeSize() {
    }

    public static int getTBooleanSize() {
        return tBooleanSize;
    }

    public static int getTCharSize() {
        return tCharSize;
    }

    public static int getTFloatSize() {
        return tFloatSize;
    }

    public static int getTDoubleSize() {
        return tDoubleSize;
    }

    public static int getTByteSize() {
        return tByteSize;
    }

    public static int getTShortSize() {
        return tShortSize;
    }

    public static int getTIntSize() {
        return tIntSize;
    }

    public static int getTLongSize() {
        return tLongSize;
    }

    public static int getTObjectSize() {
        return tObjectSize;
    }

    public static int getTArraySize() {
        return tArraySize;
    }

    public static int getTVoidSize() {
        return tVoidSize;
    }
}
