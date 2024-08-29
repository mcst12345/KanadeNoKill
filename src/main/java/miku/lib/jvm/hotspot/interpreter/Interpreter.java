package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.code.InterpreterCodelet;
import miku.lib.jvm.hotspot.code.StubQueue;
import one.helfy.JVM;
import one.helfy.Type;

public class Interpreter {

    private static final long _code;

    static {
        Type type = JVM.type("AbstractInterpreter");
        _code = type.global("_code");
    }

    public StubQueue getCode() {
        return _code == 0 ? null : new StubQueue(_code, InterpreterCodelet.class);
    }

    public boolean contains(long pc) {
        return this.getCode().contains(pc);
    }


    public InterpreterCodelet getCodeletContaining(long pc) {
        return (InterpreterCodelet) this.getCode().getStubContaining(pc);
    }
}
