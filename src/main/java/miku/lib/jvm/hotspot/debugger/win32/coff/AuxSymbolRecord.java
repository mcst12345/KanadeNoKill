package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface AuxSymbolRecord {
    int FUNCTION_DEFINITION = 0;
    int BF_EF_RECORD = 1;
    int WEAK_EXTERNAL = 2;
    int FILE = 3;
    int SECTION_DEFINITION = 4;

    int getType();
}

