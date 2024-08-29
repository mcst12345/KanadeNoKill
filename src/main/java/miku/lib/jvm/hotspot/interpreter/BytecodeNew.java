package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeNew extends BytecodeWithKlass {
    BytecodeNew(Method method, int bci) {
        super(method, bci);
    }

    public boolean isValid() {
        return this.javaCode() == 187;
    }


    public InstanceKlass getNewKlass() {
        return (InstanceKlass) this.getKlass();
    }

    public static BytecodeNew at(Method method, int bci) {
        BytecodeNew b = new BytecodeNew(method, bci);

        return b;
    }

    public static BytecodeNew atCheck(Method method, int bci) {
        BytecodeNew b = new BytecodeNew(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeNew at(BytecodeStream bcs) {
        return new BytecodeNew(bcs.method(), bcs.bci());
    }
}
