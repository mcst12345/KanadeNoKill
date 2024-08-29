package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SrcModLineNumberMap {
    int getSegment();

    int getNumSourceLinePairs();

    int getCodeOffset(int var1);

    int getLineNumber(int var1);
}
