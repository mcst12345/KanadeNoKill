package kanade.kill.util;

import kanade.kill.asm.Transformer;
import scala.concurrent.util.Unsafe;

import java.security.ProtectionDomain;

@SuppressWarnings("unused")
public class UnsafeFucker {
    public static void putObjectFuck(Object u, Object o1, long offset, Object o2) {
    }

    public static void putIntFuck(Object u, Object o1, long offset, int i) {
    }

    public static void putBooleanFuck(Object u, Object o1, long offset, boolean i) {
    }

    public static void putByteFuck(Object u, Object o1, long offset, byte i) {
    }

    public static void putCharFuck(Object u, Object o1, long offset, char i) {
    }

    public static void putFloatFuck(Object u, Object o1, long offset, float i) {
    }

    public static void putShortFuck(Object u, Object o1, long offset, short i) {
    }

    public static void putLongFuck(Object u, Object o1, long offset, long i) {
    }

    public static void putDoubleFuck(Object u, Object o1, long offset, double i) {
    }


    public static void putObjectFuck(Object u, Object o1, int offset, Object o2) {
    }

    public static void putIntFuck(Object u, Object o1, int offset, int i) {
    }

    public static void putIntVolatileFuck(Object u, Object o1, long offset, int i) {
    }

    public static void putBooleanFuck(Object u, Object o1, int offset, boolean i) {
    }

    public static void putBooleanVolatileFuck(Object u, Object o1, long offset, boolean i) {
    }


    public static void putByteFuck(Object u, Object o1, int offset, byte i) {
    }

    public static void putByteVolatileFuck(Object u, Object o1, long offset, byte i) {
    }

    public static void putCharFuck(Object u, Object o1, int offset, char i) {
    }

    public static void putCharVolatileFuck(Object u, Object o1, long offset, char i) {
    }

    public static void putFloatFuck(Object u, Object o1, int offset, float i) {
    }

    public static void putFloatVolatileFuck(Object u, Object o1, long offset, float i) {
    }

    public static void putShortFuck(Object u, Object o1, int offset, short i) {
    }

    public static void putShortVolatileFuck(Object u, Object o1, long offset, short i) {
    }

    public static void putObjectVolatileFuck(Object u, Object o1, long offset, Object i) {
    }

    public static void putLongFuck(Object u, Object o1, int offset, long i) {
    }

    public static void putLongVolatileFuck(Object u, Object o1, long offset, long i) {
    }

    public static void putDoubleFuck(Object u, Object o1, int offset, double i) {
    }

    public static void putDoubleVolatileFuck(Object u, Object o1, long offset, double i) {
    }


    public static boolean getBooleanFuck(Object u, Object o, long offset) {
        return true;
    }

    public static int getIntFuck(Object u, Object o, long offset) {
        return 0;
    }

    public static char getCharFuck(Object u, Object o, long offset) {
        return 0;
    }

    public static long getLongFuck(Object u, Object o, long offset) {
        return 0L;
    }

    public static double getDoubleFuck(Object u, Object o, long offset) {
        return 0.0D;
    }

    public static float getFloatFuck(Object u, Object o, long offset) {
        return 0.0f;
    }

    public static short getShortFuck(Object u, Object o, long offset) {
        return 0;
    }

    public static byte getByteFuck(Object u, Object o, long offset) {
        return 0;
    }

    public static boolean getBooleanVolatileFuck(Object u, Object o, long offset) {
        return true;
    }

    public static int getIntVolatileFuck(Object u, Object o, long offset) {
        return 0;
    }

    public static char getCharVolatileFuck(Object u, Object o, long offset) {
        return 0;
    }

    public static long getLongVolatileFuck(Object u, Object o, long offset) {
        return 0L;
    }

    public static double getDoubleVolatileFuck(Object u, Object o, long offset) {
        return 0.0D;
    }

    public static float getFloatVolatileFuck(Object u, Object o, long offset) {
        return 0.0f;
    }

    public static short getShortVolatileFuck(Object u, Object o, long offset) {
        return 0;
    }

    public static byte getByteVolatileFuck(Object u, Object o, long offset) {
        return 0;
    }


    public static boolean getBooleanFuck(Object u, Object o, int offset) {
        return true;
    }

    public static int getIntFuck(Object u, Object o, int offset) {
        return 0;
    }

    public static char getCharFuck(Object u, Object o, int offset) {
        return 0;
    }

    public static long getLongFuck(Object u, Object o, int offset) {
        return 0L;
    }

    public static double getDoubleFuck(Object u, Object o, int offset) {
        return 0.0D;
    }

    public static float getFloatFuck(Object u, Object o, int offset) {
        return 0.0f;
    }

    public static short getShortFuck(Object u, Object o, int offset) {
        return 0;
    }

    public static byte getByteFuck(Object u, Object o, int offset) {
        return 0;
    }


    public static Class<?> defineClass(Object o, String name, byte[] b, int off, int len,
                                       ClassLoader loader,
                                       ProtectionDomain protectionDomain) {
        b = Transformer.instance.transform(name, name, b, null);
        return Unsafe.instance.defineClass(name, b, 0, b.length, loader, protectionDomain);
    }

    public static Class<?> defineAnonymousClass(Object o, Class<?> hostClass, byte[] data, Object[] cpPatches) {
        data = Transformer.instance.transform("", "", data, null);
        return Unsafe.instance.defineAnonymousClass(hostClass, data, cpPatches);
    }

    public static void putAddressFuck(Object u,long address, long x){}

    public static byte getByteFuck(Object o1, long address) {
        return 0;
    }

    public static char getCharFuck(Object o1, long address) {
        return 0;
    }

    public static int getIntFuck(Object o1, long address) {
        return 0;
    }

    public static short getShortFuck(Object o1, long address) {
        return 0;
    }

    public static float getFloatFuck(Object o1, long address) {
        return 0;
    }

    public static double getDoubleFuck(Object o1, long address) {
        return 0;
    }

    public static long getLongFuck(Object o1, long address) {
        return 0;
    }

    public static boolean getBooleanFuck(Object o1, long address) {
        return false;
    }


    public static void putByteFuck(Object o1, long address, byte b) {
    }

    public static void putCharFuck(Object o1, long address, char c) {
    }

    public static void putIntFuck(Object o1, long address, int i) {
    }

    public static void putShortFuck(Object o1, long address, short s) {
    }

    public static void putFloatFuck(Object o1, long address, float f) {
    }

    public static void putDoubleFuck(Object o1, long address, double d) {
    }

    public static void putLongFuck(Object o1, long address, long l) {
    }

    public static void putBooleanFuck(Object o1, long address, boolean b) {
    }

    public static long getAddressFuck(Object u,long address){
        return 0L;
    }

}
