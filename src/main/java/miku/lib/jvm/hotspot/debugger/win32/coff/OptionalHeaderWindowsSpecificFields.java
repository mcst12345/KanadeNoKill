package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface OptionalHeaderWindowsSpecificFields {
    long getImageBase();

    int getSectionAlignment();

    int getFileAlignment();

    short getMajorOperatingSystemVersion();

    short getMinorOperatingSystemVersion();

    short getMajorImageVersion();

    short getMinorImageVersion();

    short getMajorSubsystemVersion();

    short getMinorSubsystemVersion();

    int getSizeOfImage();

    int getSizeOfHeaders();

    int getCheckSum();

    short getSubsystem();

    short getDLLCharacteristics();

    long getSizeOfStackReserve();

    long getSizeOfStackCommit();

    long getSizeOfHeapReserve();

    long getSizeOfHeapCommit();

    int getLoaderFlags();

    int getNumberOfRvaAndSizes();
}
