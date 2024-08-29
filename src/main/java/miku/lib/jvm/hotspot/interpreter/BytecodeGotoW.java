package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeGotoW extends BytecodeJmp {
    BytecodeGotoW(Method method, int bci) {
        super(method, bci);
    }

    public int getTargetBCI() {
        return this.bci() + this.javaSignedWordAt(1);
    }


    public boolean isValid() {
        return this.javaCode() == 200;
    }

    public static BytecodeGotoW at(Method method, int bci) {
        BytecodeGotoW b = new BytecodeGotoW(method, bci);

        return b;
    }

    public static BytecodeGotoW atCheck(Method method, int bci) {
        BytecodeGotoW b = new BytecodeGotoW(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeGotoW at(BytecodeStream bcs) {
        return new BytecodeGotoW(bcs.method(), bcs.bci());
    }
}
