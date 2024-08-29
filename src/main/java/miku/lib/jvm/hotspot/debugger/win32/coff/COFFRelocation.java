package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface COFFRelocation {
    int getVirtualAddress();

    int getSymbolTableIndex();

    short getType();
}
