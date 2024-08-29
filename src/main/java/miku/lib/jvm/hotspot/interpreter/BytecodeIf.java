package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeIf extends BytecodeJmp {

    BytecodeIf(Method method, int bci) {
        super(method, bci);
    }

    public int getTargetBCI() {
        return this.bci() + this.javaShortAt(1);
    }

    public boolean isValid() {
        int jcode = this.javaCode();
        return jcode >= 153 && jcode <= 166 || jcode == 198 || jcode == 199;
    }

    public static BytecodeIf at(Method method, int bci) {
        BytecodeIf b = new BytecodeIf(method, bci);


        return b;
    }

    public static BytecodeIf atCheck(Method method, int bci) {
        BytecodeIf b = new BytecodeIf(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeIf at(BytecodeStream bcs) {
        return new BytecodeIf(bcs.method(), bcs.bci());
    }

}
