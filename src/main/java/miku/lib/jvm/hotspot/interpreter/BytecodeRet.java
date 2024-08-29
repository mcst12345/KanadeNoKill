package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeRet extends BytecodeWideable {
    BytecodeRet(Method method, int bci) {
        super(method, bci);
    }


    public boolean isValid() {
        return this.javaCode() == 169;
    }

    public static BytecodeRet at(Method method, int bci) {
        return new BytecodeRet(method, bci);
    }

    public static BytecodeRet atCheck(Method method, int bci) {
        BytecodeRet b = new BytecodeRet(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeRet at(BytecodeStream bcs) {
        return new BytecodeRet(bcs.method(), bcs.bci());
    }

    public String toString() {
        return "ret" +
                " " +
                '#' +
                this.getLocalVarIndex();
    }
}
