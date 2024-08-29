package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeGetStatic extends BytecodeGetPut {
    BytecodeGetStatic(Method method, int bci) {
        super(method, bci);
    }

    public boolean isStatic() {
        return true;
    }


    public boolean isValid() {
        return this.javaCode() == 178;
    }

    public static BytecodeGetStatic at(Method method, int bci) {
        return new BytecodeGetStatic(method, bci);
    }

    public static BytecodeGetStatic atCheck(Method method, int bci) {
        BytecodeGetStatic b = new BytecodeGetStatic(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeGetStatic at(BytecodeStream bcs) {
        return new BytecodeGetStatic(bcs.method(), bcs.bci());
    }
}
