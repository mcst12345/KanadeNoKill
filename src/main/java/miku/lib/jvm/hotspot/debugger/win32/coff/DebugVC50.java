package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50 {
    int getSubsectionDirectoryOffset();

    DebugVC50SubsectionDirectory getSubsectionDirectory();
}
