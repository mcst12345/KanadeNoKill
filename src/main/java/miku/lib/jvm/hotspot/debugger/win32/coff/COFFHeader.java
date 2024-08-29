package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface COFFHeader {
    short getMachineType();

    short getNumberOfSections();

    int getTimeDateStamp();

    int getPointerToSymbolTable();

    int getNumberOfSymbols();

    short getSizeOfOptionalHeader();

    OptionalHeader getOptionalHeader() throws COFFException;

    short getCharacteristics();

    boolean hasCharacteristic(short var1);

    SectionHeader getSectionHeader(int var1);

    COFFSymbol getCOFFSymbol(int var1);

    int getNumberOfStrings();

    String getString(int var1);
}

