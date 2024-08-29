package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeLoad extends BytecodeLoadStore {
    BytecodeLoad(Method method, int bci) {
        super(method, bci);
    }

    public boolean isValid() {
        int jcode = this.javaCode();
        switch (jcode) {
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
                return true;
            default:
                return false;
        }
    }

    public static BytecodeLoad at(Method method, int bci) {
        return new BytecodeLoad(method, bci);
    }

    public static BytecodeLoad atCheck(Method method, int bci) {
        BytecodeLoad b = new BytecodeLoad(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeLoad at(BytecodeStream bcs) {
        return new BytecodeLoad(bcs.method(), bcs.bci());
    }
}
