package miku.lib.jvm.hotspot.oops;

import java.io.PrintStream;

public class IndexableFieldIdentifier extends FieldIdentifier {
    private int index;

    public IndexableFieldIdentifier(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return Integer.toString(this.getIndex());
    }

    public void printOn(PrintStream tty) {
        tty.print(" - " + this.getIndex() + ":\t");
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof IndexableFieldIdentifier)) {
            return false;
        } else {
            return ((IndexableFieldIdentifier) obj).getIndex() == this.index;
        }
    }

    public int hashCode() {
        return this.index;
    }
}
