package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SSModule extends DebugVC50Subsection {
    short getOverlayNumber();

    short getLibrariesIndex();

    short getNumCodeSegments();

    short getDebuggingStyle();

    DebugVC50SegInfo getSegInfo(int var1);

    String getName();
}
