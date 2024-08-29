package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeTableswitch extends Bytecode {
    BytecodeTableswitch(Method method, int bci) {
        super(method, bci);
    }


    public int defaultOffset() {
        return this.javaSignedWordAt(this.alignedOffset(1));
    }

    public int lowKey() {
        return this.javaSignedWordAt(this.alignedOffset(5));
    }

    public int highKey() {
        return this.javaSignedWordAt(this.alignedOffset(9));
    }

    public int length() {
        return this.highKey() - this.lowKey() + 1;
    }

    public int destOffsetAt(int i) {
        int x2 = this.alignedOffset(1 + (3 + i) * 4);
        this.javaSignedWordAt(x2);
        return this.javaSignedWordAt(this.alignedOffset(1 + (3 + i) * 4));
    }

    public boolean isValid() {
        boolean result = this.javaCode() == 170;
        if (!result) {
            return false;
        } else {
            int lo = this.lowKey();
            int hi = this.highKey();
            if (hi < lo) {
                return false;
            } else {
                int i = hi - lo - 1;

                while (i-- > 0) {
                }

                return true;
            }
        }
    }

    public static BytecodeTableswitch at(Method method, int bci) {
        return new BytecodeTableswitch(method, bci);
    }

    public static BytecodeTableswitch atCheck(Method method, int bci) {
        BytecodeTableswitch b = new BytecodeTableswitch(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeTableswitch at(BytecodeStream bcs) {
        return new BytecodeTableswitch(bcs.method(), bcs.bci());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("tableswitch");
        buf.append(" ");
        buf.append("default: ");
        buf.append(this.bci() + this.defaultOffset());
        buf.append(", ");
        int lo = this.lowKey();
        int hi = this.highKey();
        int i = hi - lo - 1;

        while (i-- > 0) {
            buf.append("case ");
            buf.append(lo + i);
            buf.append(':');
            buf.append(this.bci() + this.destOffsetAt(i));
            buf.append(", ");
        }

        return buf.toString();
    }
}
