package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SSSymbolBase extends DebugVC50Subsection {
    short getSymHashIndex();

    short getAddrHashIndex();

    int getSymTabSize();

    int getSymHashSize();

    int getAddrHashSize();

    DebugVC50SymbolIterator getSymbolIterator();
}
