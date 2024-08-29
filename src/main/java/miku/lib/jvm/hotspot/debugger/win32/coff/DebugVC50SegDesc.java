package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SegDesc {
    short getFlags();

    short getOverlayNum();

    short getGroup();

    short getFrame();

    short getName();

    short getClassName();

    int getOffset();

    int getSize();
}
