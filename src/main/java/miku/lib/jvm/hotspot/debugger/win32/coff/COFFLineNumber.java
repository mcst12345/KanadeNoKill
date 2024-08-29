package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface COFFLineNumber {
    int getType();

    short getLineNumber();
}
