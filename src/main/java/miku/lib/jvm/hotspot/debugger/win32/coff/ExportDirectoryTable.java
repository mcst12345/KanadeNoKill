package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface ExportDirectoryTable {
    int getExportFlags();

    int getTimeDateStamp();

    short getMajorVersion();

    short getMinorVersion();

    int getNameRVA();

    String getDLLName();

    int getOrdinalBase();

    int getNumberOfAddressTableEntries();

    int getNumberOfNamePointers();

    int getExportAddressTableRVA();

    int getNamePointerTableRVA();

    int getOrdinalTableRVA();

    String getExportName(int var1);

    short getExportOrdinal(int var1);

    boolean isExportAddressForwarder(short var1);

    String getExportAddressForwarder(short var1);

    int getExportAddress(short var1);
}
