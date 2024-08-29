package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface OptionalHeaderDataDirectories {
    DataDirectory getExportTable() throws COFFException;

    ExportDirectoryTable getExportDirectoryTable() throws COFFException;

    DataDirectory getImportTable() throws COFFException;

    DataDirectory getResourceTable() throws COFFException;

    DataDirectory getExceptionTable() throws COFFException;

    DataDirectory getCertificateTable() throws COFFException;

    DataDirectory getBaseRelocationTable() throws COFFException;

    DataDirectory getDebug() throws COFFException;

    DebugDirectory getDebugDirectory() throws COFFException;

    DataDirectory getArchitecture() throws COFFException;

    DataDirectory getGlobalPtr() throws COFFException;

    DataDirectory getTLSTable() throws COFFException;

    DataDirectory getLoadConfigTable() throws COFFException;

    DataDirectory getBoundImportTable() throws COFFException;

    DataDirectory getImportAddressTable() throws COFFException;

    DataDirectory getDelayImportDescriptor() throws COFFException;

    DataDirectory getCOMPlusRuntimeHeader() throws COFFException;
}
