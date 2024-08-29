package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeJsr extends BytecodeJmp {

    BytecodeJsr(Method method, int bci) {
        super(method, bci);
    }

    public int getTargetBCI() {
        return this.bci() + this.javaShortAt(1);
    }

    public boolean isValid() {
        return this.javaCode() == 168;
    }

    public static BytecodeJsr at(Method method, int bci) {
        BytecodeJsr b = new BytecodeJsr(method, bci);

        return b;
    }

    public static BytecodeJsr atCheck(Method method, int bci) {
        BytecodeJsr b = new BytecodeJsr(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeJsr at(BytecodeStream bcs) {
        return new BytecodeJsr(bcs.method(), bcs.bci());
    }
}
