package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface AuxBfEfRecord extends AuxSymbolRecord {
    short getLineNumber();

    int getPointerToNextFunction();
}
