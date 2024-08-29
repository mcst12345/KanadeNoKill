package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50Subsection {
    short getSubsectionType();

    short getSubsectionModuleIndex();

    int getSubsectionSize();
}
