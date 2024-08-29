package miku.lib.utils;

public class NumberTransformer {
    public static long dataToCInteger(byte[] data, boolean isUnsigned) {
        byteSwap(data);

        if (data.length < 8 && !isUnsigned && (data[0] & 128) != 0) {
            byte[] newData = new byte[8];

            for (int i = 0; i < 8; ++i) {
                if (7 - i < data.length) {
                    newData[i] = data[i + data.length - 8];
                } else {
                    newData[i] = -1;
                }
            }

            data = newData;
        }

        return rawDataToJLong(data);
    }

    private static void byteSwap(byte[] data) {
        for (int i = 0; i < data.length / 2; ++i) {
            int altIndex = data.length - i - 1;
            byte t = data[altIndex];
            data[altIndex] = data[i];
            data[i] = t;
        }

    }

    private static long rawDataToJLong(byte[] data) {
        long addr = 0L;

        for (byte datum : data) {
            addr <<= 8;
            addr |= datum & 255;
        }

        return addr;
    }
}
