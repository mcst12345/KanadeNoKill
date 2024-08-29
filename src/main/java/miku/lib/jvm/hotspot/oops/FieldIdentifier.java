package miku.lib.jvm.hotspot.oops;

import java.io.PrintStream;

public class FieldIdentifier {
    public FieldIdentifier() {
    }

    public String getName() {
        return "";
    }

    public void printOn(PrintStream tty) {
        tty.print(" - " + this.getName() + ":\t");
    }

}
