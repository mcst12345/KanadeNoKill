package one.helfy.vmstruct;

import one.helfy.JVM;

public class Interpreter {
    private static final JVM jvm = JVM.getInstance();
    private static final long code = JVM.getAddress(JVM.type("AbstractInterpreter").global("_code"));
    private static final long _stub_buffer = code + JVM.type("StubQueue").offset("_stub_buffer");
    private static final long _buffer_limit = code + JVM.type("StubQueue").offset("_buffer_limit");

    public static boolean contains(long pc) {
        long offset = pc - JVM.getAddress(_stub_buffer);
        return 0 <= offset && offset < JVM.getInt(_buffer_limit);
    }
}