package miku.lib.utils;

public class AddressCalculator {

    public static boolean greaterThan(long address, long a) {
        if (a == 0L) {
            return true;
        } else {
            if (address >= 0L && a < 0L) {
                return false;
            } else if (address < 0L && a >= 0L) {
                return true;
            } else {
                return address > a;
            }
        }
    }

    public static boolean greaterThanOrEqual(long address, long a) {
        if (a == 0L) {
            return true;
        } else {
            if (address >= 0L && a < 0L) {
                return false;
            } else if (address < 0L && a >= 0L) {
                return true;
            } else {
                return address >= a;
            }
        }
    }

    public static boolean lessThanOrEqual(long address, long a) {
        if (a == 0) {
            return false;
        } else {
            if (address >= 0L && a < 0L) {
                return true;
            } else if (address < 0L && a >= 0L) {
                return false;
            } else {
                return address <= a;
            }
        }
    }

    public static boolean lessThan(long address, long a) {
        if (a == 0) {
            return false;
        } else {
            if (address >= 0L && a < 0L) {
                return true;
            } else if (address < 0L && a >= 0L) {
                return false;
            } else {
                return address < a;
            }
        }
    }

    public static long minus(long address, long arg) {
        return arg == 0 ? address : address - arg;
    }

    public static long andWithMask(long address, long mask) {
        return address & mask;
    }

    public static long orWithMask(long address, long mask) {
        return address | mask;
    }

    public static long xorWithMask(long address, long mask) {
        return address ^ mask;
    }
}
