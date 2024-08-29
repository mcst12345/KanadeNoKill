package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface SectionHeader {
    String getName();

    int getSize();

    int getVirtualAddress();

    int getSizeOfRawData();

    int getPointerToRawData();

    int getPointerToRelocations();

    int getPointerToLineNumbers();

    short getNumberOfRelocations();

    short getNumberOfLineNumbers();

    int getSectionFlags();

    boolean hasSectionFlag(int var1);

    COFFRelocation getCOFFRelocation(int var1);

    COFFLineNumber getCOFFLineNumber(int var1);
}
