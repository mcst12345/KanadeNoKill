package miku.lib.jvm.hotspot.utilities;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CStringUtilities {
    private static final String encoding = System.getProperty("file.encoding", "US-ASCII");

    public static int getStringLength(long addr) {
        int i;
        for (i = 0; NumberTransformer.dataToCInteger(JVMUtil.getBytes(addr + i, 1), false) != 0L; ++i) {
        }
        return i;
    }

    public static String getString(long addr) {
        if (addr == 0) {
            return null;
        } else {
            List<Byte> data = new ArrayList<>();
            byte val;
            long i = 0L;
            do {
                val = (byte) ((int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(addr + i, 1), false));
                if (val != 0) {
                    data.add(val);
                }

                ++i;
            } while (val != 0);

            byte[] bytes = new byte[data.size()];

            for (i = 0L; i < (long) data.size(); ++i) {
                bytes[(int) i] = data.get((int) i);
            }

            try {
                return new String(bytes, encoding);
            } catch (UnsupportedEncodingException var7) {
                throw new RuntimeException("Error converting bytes to String using " + encoding + " encoding", var7);
            }
        }
    }
}
