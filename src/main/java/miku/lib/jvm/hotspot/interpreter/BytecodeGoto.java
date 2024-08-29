package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeGoto extends BytecodeJmp {
    BytecodeGoto(Method method, int bci) {
        super(method, bci);
    }

    public int getTargetBCI() {
        return this.bci() + this.javaShortAt(1);
    }

    public boolean isValid() {
        return this.javaCode() == 167;
    }

    public static BytecodeGoto at(Method method, int bci) {
        return new BytecodeGoto(method, bci);
    }

    public static BytecodeGoto atCheck(Method method, int bci) {
        BytecodeGoto b = new BytecodeGoto(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeGoto at(BytecodeStream bcs) {
        return new BytecodeGoto(bcs.method(), bcs.bci());
    }
}
