package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeNewArray extends Bytecode {
    BytecodeNewArray(Method method, int bci) {
        super(method, bci);
    }

    public int getType() {
        return this.javaByteAt(1);
    }

    public boolean isValid() {
        boolean result = this.javaCode() == 188;
        if (!result) {
            return false;
        } else {
            switch (this.getType()) {
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                    return true;
                default:
                    return false;
            }
        }
    }

    public String getTypeName() {
        String result;
        switch (this.getType()) {
            case 4:
                result = "boolean";
                break;
            case 5:
                result = "char";
                break;
            case 6:
                result = "float";
                break;
            case 7:
                result = "double";
                break;
            case 8:
                result = "byte";
                break;
            case 9:
                result = "short";
                break;
            case 10:
                result = "int";
                break;
            case 11:
                result = "long";
                break;
            default:
                result = "<invalid>";
        }

        return result;
    }

    public static BytecodeNewArray at(Method method, int bci) {
        return new BytecodeNewArray(method, bci);
    }

    public static BytecodeNewArray atCheck(Method method, int bci) {
        BytecodeNewArray b = new BytecodeNewArray(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeNewArray at(BytecodeStream bcs) {
        return new BytecodeNewArray(bcs.method(), bcs.bci());
    }

    public String toString() {
        return "newarray" +
                " " +
                this.getTypeName();
    }
}
