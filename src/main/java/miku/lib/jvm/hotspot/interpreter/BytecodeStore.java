package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeStore extends BytecodeLoadStore {
    BytecodeStore(Method method, int bci) {
        super(method, bci);
    }

    public boolean isValid() {
        int jcode = this.javaCode();
        switch (jcode) {
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
                return true;
            default:
                return false;
        }
    }

    public static BytecodeStore at(Method method, int bci) {
        return new BytecodeStore(method, bci);
    }

    public static BytecodeStore atCheck(Method method, int bci) {
        BytecodeStore b = new BytecodeStore(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeStore at(BytecodeStream bcs) {
        return new BytecodeStore(bcs.method(), bcs.bci());
    }
}
