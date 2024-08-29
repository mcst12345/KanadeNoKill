package miku.lib.jvm.hotspot.debugger.win32.coff;


public interface DebugVC50SSGlobalTypes extends DebugVC50Subsection {
    int getNumTypes();

    int getTypeOffset(int var1);

    DebugVC50TypeIterator getTypeIterator();
}
