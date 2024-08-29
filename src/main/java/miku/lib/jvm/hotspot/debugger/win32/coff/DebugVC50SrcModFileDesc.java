package miku.lib.jvm.hotspot.debugger.win32.coff;


public interface DebugVC50SrcModFileDesc {
    int getNumCodeSegments();

    DebugVC50SrcModLineNumberMap getLineNumberMap(int var1);

    int getSegmentStartOffset(int var1);

    int getSegmentEndOffset(int var1);

    String getSourceFileName();
}
