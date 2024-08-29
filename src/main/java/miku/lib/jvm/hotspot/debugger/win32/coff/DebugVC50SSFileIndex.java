package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SSFileIndex extends DebugVC50Subsection {
    short getNumModules();

    short getNumReferences();

    short[] getModStart();

    short[] getRefCount();

    int[] getNameRef();

    String[] getNames();
}
