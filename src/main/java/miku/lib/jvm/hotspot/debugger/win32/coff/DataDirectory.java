package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DataDirectory {
    int getRVA();

    int getSize();
}
