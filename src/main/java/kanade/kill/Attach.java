package kanade.kill;

import com.sun.jna.Function;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import sun.instrument.InstrumentationImpl;

public class Attach {
    static {
        try {
            System.loadLibrary("instrument");
        } catch (Throwable ignored) {
        }
    }
    public static void run() {
        InstrumentationImpl.class.getDeclaredConstructors();
        String path = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("!/kanade/kill/Empty.class", "").replace("file:", "");
        if (Platform.isWindows()) {
            path = path.substring(1);
        }
        Core.LOGGER.info("Jar path:" + path);
        Function JNI_GetCreatedJavaVMs = Function.getFunction("jvm", "JNI_GetCreatedJavaVMs");
        Pointer[] pJavaVMs = new Pointer[1];
        JNI_GetCreatedJavaVMs.invokeInt(new Object[]{pJavaVMs, 1, (new IntByReference(1)).getPointer()});
        Function Agent_OnAttach = Function.getFunction("instrument", "Agent_OnAttach");
        Agent_OnAttach.invokeInt(new Object[]{pJavaVMs[0], path, null});
    }
}
