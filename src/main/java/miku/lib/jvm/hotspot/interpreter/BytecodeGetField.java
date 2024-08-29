package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeGetField extends BytecodeGetPut {
    BytecodeGetField(Method method, int bci) {
        super(method, bci);
    }

    @Override
    public boolean isStatic() {
        return false;
    }


    public boolean isValid() {
        return this.javaCode() == 180;
    }

    public static BytecodeGetField at(Method method, int bci) {
        return new BytecodeGetField(method, bci);
    }

    public static BytecodeGetField atCheck(Method method, int bci) {
        BytecodeGetField b = new BytecodeGetField(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeGetField at(BytecodeStream bcs) {
        return new BytecodeGetField(bcs.method(), bcs.bci());
    }
}
