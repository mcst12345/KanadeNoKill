package miku.lib.jvm.hotspot.oops;

public class LineNumberTableElement {
    private final int start_bci;
    private final int line_number;

    public LineNumberTableElement(int start_bci, int line_number) {
        this.start_bci = start_bci;
        this.line_number = line_number;
    }

    public int getStartBCI() {
        return this.start_bci;
    }

    public int getLineNumber() {
        return this.line_number;
    }
}
