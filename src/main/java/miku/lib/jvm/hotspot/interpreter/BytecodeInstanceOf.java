package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeInstanceOf extends BytecodeWithKlass {

    BytecodeInstanceOf(Method method, int bci) {
        super(method, bci);
    }

    public InstanceKlass getInstanceOfKlass() {
        return (InstanceKlass) this.getKlass();
    }


    public boolean isValid() {
        return this.javaCode() == 193;
    }

    public static BytecodeInstanceOf at(Method method, int bci) {

        return new BytecodeInstanceOf(method, bci);
    }

    public static BytecodeInstanceOf atCheck(Method method, int bci) {
        BytecodeInstanceOf b = new BytecodeInstanceOf(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeInstanceOf at(BytecodeStream bcs) {
        return new BytecodeInstanceOf(bcs.method(), bcs.bci());
    }
}