package miku.lib.jvm.hotspot.debugger.win32.coff;


public interface OptionalHeaderStandardFields {
    byte getMajorLinkerVersion();

    byte getMinorLinkerVersion();

    int getSizeOfCode();

    int getSizeOfInitializedData();

    int getSizeOfUninitializedData();

    int getAddressOfEntryPoint();

    int getBaseOfCode();

    int getBaseOfData() throws COFFException;
}
