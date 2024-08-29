package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SegInfo {
    short getSegment();

    int getOffset();

    int getSegmentCodeSize();
}
