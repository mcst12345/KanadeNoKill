package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeCheckCast extends BytecodeWithKlass {
    BytecodeCheckCast(Method method, int bci) {
        super(method, bci);
    }

    public InstanceKlass getCheckCastKlass() {
        return (InstanceKlass) this.getKlass();
    }

    public boolean isValid() {
        return this.javaCode() == 192;
    }

    public static BytecodeCheckCast at(Method method, int bci) {

        return new BytecodeCheckCast(method, bci);
    }

    public static BytecodeCheckCast atCheck(Method method, int bci) {
        BytecodeCheckCast b = new BytecodeCheckCast(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeCheckCast at(BytecodeStream bcs) {
        return new BytecodeCheckCast(bcs.method(), bcs.bci());
    }
}
