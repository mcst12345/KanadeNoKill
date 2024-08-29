package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface AuxSectionDefinitionsRecord extends AuxSymbolRecord {
    int getLength();

    short getNumberOfRelocations();

    short getNumberOfLineNumbers();

    int getCheckSum();

    short getNumber();

    byte getSelection();
}
