package one.helfy.vmstruct;

import one.helfy.JVM;

public class StubRoutines {
    public static final JVM jvm = JVM.getInstance();
    private static final long _call_stub_return_address = JVM.type("StubRoutines").field("_call_stub_return_address").offset;

    public static boolean returnsToCallStub(long pc) {
        return JVM.getAddress(_call_stub_return_address) == pc;
    }
}
