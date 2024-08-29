package miku.lib.jvm.hotspot.debugger.win32.coff;


public interface DebugVC50SSSegMap extends DebugVC50Subsection {
    short getNumSegDesc();

    short getNumLogicalSegDesc();

    DebugVC50SegDesc getSegDesc(int var1);
}
