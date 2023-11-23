package kanade.kill;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    private static Instrumentation inst;
    public static void agentmain(String args, Instrumentation instrumentation){
        inst = instrumentation;
    }

    public static Instrumentation getInstrumentation(){
        return inst;
    }
}
