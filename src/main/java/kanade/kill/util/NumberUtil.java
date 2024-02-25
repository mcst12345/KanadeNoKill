package kanade.kill.util;

@SuppressWarnings("unused")
public class NumberUtil {
    public static int checkDivisor(int divisor) {
        return divisor != 0 ? divisor : 1;
    }

    public static float checkDivisor(float divisor) {
        return divisor != 0.0f ? divisor : 1.0f;
    }

    public static long checkDivisor(long divisor) {
        return divisor != 0 ? divisor : 1L;
    }

    public static double checkDivisor(double divisor) {
        return divisor != 0.0D ? divisor : 1.0d;
    }
}
