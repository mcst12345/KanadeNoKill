package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface COFFFile {
    COFFHeader getHeader();

    boolean isImage();

    void close();
}

