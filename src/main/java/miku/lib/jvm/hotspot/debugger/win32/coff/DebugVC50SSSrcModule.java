package miku.lib.jvm.hotspot.debugger.win32.coff;


public interface DebugVC50SSSrcModule extends DebugVC50Subsection {
    int getNumSourceFiles();

    int getNumCodeSegments();

    DebugVC50SrcModFileDesc getSourceFileDesc(int var1);

    int getSegmentStartOffset(int var1);

    int getSegmentEndOffset(int var1);

    int getSegment(int var1);
}
