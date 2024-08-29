package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeANewArray extends BytecodeWithKlass {
    BytecodeANewArray(Method method, int bci) {
        super(method, bci);
    }

    public Klass getKlass() {
        return super.getKlass();
    }

    public boolean isValid() {
        return this.javaCode() == 189;
    }

    public static BytecodeANewArray at(Method method, int bci) {

        return new BytecodeANewArray(method, bci);
    }

    public static BytecodeANewArray atCheck(Method method, int bci) {
        BytecodeANewArray b = new BytecodeANewArray(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeANewArray at(BytecodeStream bcs) {
        return new BytecodeANewArray(bcs.method(), bcs.bci());
    }

}
