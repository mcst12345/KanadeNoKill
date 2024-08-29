package miku.lib.jvm.hotspot.debugger;

import java.io.IOException;

public interface DataSource {
    byte readByte() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    int read(byte[] var1) throws IOException;

    void seek(long var1) throws IOException;

    long getFilePointer() throws IOException;

    void close() throws IOException;
}
