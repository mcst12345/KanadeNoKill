package miku.lib.jvm.hotspot.runtime;

public class Bytes {
    public static short swapShort(short x) {
        return (short) (x >> 8 & 255 | x << 8);
    }

    public static int swapInt(int x) {
        return swapShort((short) x) << 16 | swapShort((short) (x >> 16)) & '\uffff';
    }

    public static long swapLong(long x) {
        return (long) swapInt((int) x) << 32 | swapInt((int) (x >> 32));
    }
}
