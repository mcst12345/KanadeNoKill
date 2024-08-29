package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeLookupswitch extends Bytecode {
    BytecodeLookupswitch(Method method, int bci) {
        super(method, bci);
    }


    public int defaultOffset() {
        return this.javaSignedWordAt(this.alignedOffset(1));
    }

    public int numberOfPairs() {
        return this.javaSignedWordAt(this.alignedOffset(5));
    }

    public LookupswitchPair pairAt(int i) {
        return new LookupswitchPair(this.method, this.bci + this.alignedOffset(1 + (1 + i) * 2 * 4));
    }

    public boolean isValid() {
        boolean result = this.javaCode() == 171;
        if (!result) {
            return false;
        } else {
            int i = this.numberOfPairs() - 1;

            do {
                if (i-- <= 0) {
                    return true;
                }
            } while (this.pairAt(i).match() <= this.pairAt(i + 1).match());

            return false;
        }
    }

    public static BytecodeLookupswitch at(Method method, int bci) {
        return new BytecodeLookupswitch(method, bci);
    }

    public static BytecodeLookupswitch atCheck(Method method, int bci) {
        BytecodeLookupswitch b = new BytecodeLookupswitch(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeLookupswitch at(BytecodeStream bcs) {
        return new BytecodeLookupswitch(bcs.method(), bcs.bci());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("lookupswitch");
        buf.append(" ");
        buf.append("default: ");
        buf.append(this.bci() + this.defaultOffset());
        buf.append(", ");
        int i = this.numberOfPairs() - 1;

        while (i-- > 0) {
            LookupswitchPair pair = this.pairAt(i);
            buf.append("case ");
            buf.append(pair.match());
            buf.append(':');
            buf.append(this.bci() + pair.offset());
            buf.append(", ");
        }

        return buf.toString();
    }
}
