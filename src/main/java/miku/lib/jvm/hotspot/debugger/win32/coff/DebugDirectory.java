package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugDirectory {
    int getNumEntries();

    DebugDirectoryEntry getEntry(int var1);
}
