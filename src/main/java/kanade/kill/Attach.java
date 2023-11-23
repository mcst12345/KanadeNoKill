package kanade.kill;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import sun.instrument.InstrumentationImpl;

public class Attach {
    public static void run() {
	String path = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1).replace("!/kanade/kill/Empty.class","");
        //InstrumentationImpl.class.getDeclaredConstructors();
        Function JNI_GetCreatedJavaVMs = Function.getFunction("jvm", "JNI_GetCreatedJavaVMs");
        Pointer[] pJavaVMs = new Pointer[1];
        JNI_GetCreatedJavaVMs.invokeInt(new Object[]{pJavaVMs, 1, (new IntByReference(1)).getPointer()});
        Function Agent_OnAttach = Function.getFunction("instrument", "Agent_OnAttach");
        Agent_OnAttach.invokeInt(new Object[]{pJavaVMs[0], path, null});
    }
}
