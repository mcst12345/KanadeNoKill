package one.helfy.vmstruct;

import one.helfy.JVM;

public class Klass {
    private static final JVM jvm = JVM.getInstance();
    private static final long _name = JVM.type("Klass").offset("_name");

    static String name(long klass) {
        long symbol = JVM.getAddress(klass + _name);
        return Symbol.asString(symbol);
    }
}