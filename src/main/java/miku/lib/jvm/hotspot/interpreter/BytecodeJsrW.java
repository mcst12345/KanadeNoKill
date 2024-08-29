package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeJsrW extends BytecodeJmp {

    BytecodeJsrW(Method method, int bci) {
        super(method, bci);
    }

    public int getTargetBCI() {
        return this.bci() + this.javaSignedWordAt(1);
    }

    public boolean isValid() {
        return this.javaCode() == 201;
    }

    public static BytecodeJsrW at(Method method, int bci) {
        BytecodeJsrW b = new BytecodeJsrW(method, bci);

        return b;
    }

    public static BytecodeJsrW atCheck(Method method, int bci) {
        BytecodeJsrW b = new BytecodeJsrW(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeJsrW at(BytecodeStream bcs) {
        return new BytecodeJsrW(bcs.method(), bcs.bci());
    }
}
