package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugDirectoryEntry {
    int getCharacteristics();

    int getTimeDateStamp();

    short getMajorVersion();

    short getMinorVersion();

    int getType();

    int getSizeOfData();

    int getAddressOfRawData();

    int getPointerToRawData();

    DebugVC50 getDebugVC50();

    byte getRawDataByte(int var1);
}
