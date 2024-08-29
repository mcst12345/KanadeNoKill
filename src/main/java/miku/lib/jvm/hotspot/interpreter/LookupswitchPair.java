package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class LookupswitchPair extends Bytecode {
    LookupswitchPair(Method method, int bci) {
        super(method, bci);
    }

    public int match() {
        return this.javaSignedWordAt(0);
    }

    public int offset() {
        return this.javaSignedWordAt(4);
    }
}
