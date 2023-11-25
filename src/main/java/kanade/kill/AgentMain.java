package kanade.kill;

import kanade.kill.reflection.EarlyFields;
import scala.concurrent.util.Unsafe;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    private static Instrumentation inst;
    private static long nativeAgent;
    public static void agentmain(String args, Instrumentation instrumentation){
        inst = instrumentation;
        nativeAgent = Unsafe.instance.getLongVolatile(instrumentation, EarlyFields.mNativeAgent_offset);
    }

    public static Instrumentation getInstrumentation(){
        return inst;
    }

    public static long getNativeAgent() {
        return nativeAgent;
    }
}
