package miku.lib.jvm.hotspot.oops;

import java.io.PrintStream;

public class NamedFieldIdentifier extends FieldIdentifier {
    private String name;

    public NamedFieldIdentifier(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void printOn(PrintStream tty) {
        tty.print(" - " + this.getName() + ":\t");
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            return obj instanceof NamedFieldIdentifier && ((NamedFieldIdentifier) obj).getName().equals(this.name);
        }
    }

    public int hashCode() {
        return this.name.hashCode();
    }
}
