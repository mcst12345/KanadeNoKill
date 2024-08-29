package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SubsectionDirectory {
    short getHeaderLength();

    short getEntryLength();

    int getNumEntries();

    DebugVC50Subsection getSubsection(int var1);
}
