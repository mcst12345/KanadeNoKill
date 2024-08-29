package miku.lib.jvm.hotspot.debugger;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class MappedByteBufferDataSource implements DataSource {
    private MappedByteBuffer buf;

    public MappedByteBufferDataSource(MappedByteBuffer buf) {
        this.buf = buf;
    }

    public byte readByte() throws IOException {
        return this.buf.get();
    }

    public short readShort() throws IOException {
        return this.buf.getShort();
    }

    public int readInt() throws IOException {
        return this.buf.getInt();
    }

    public long readLong() throws IOException {
        return this.buf.getLong();
    }

    public int read(byte[] b) throws IOException {
        this.buf.get(b);
        return b.length;
    }

    public void seek(long pos) throws IOException {
        try {
            this.buf.position((int) pos);
        } catch (IllegalArgumentException var4) {
            IllegalArgumentException e = var4;
            System.err.println("Error seeking to file position 0x" + Long.toHexString(pos));
            throw e;
        }
    }

    public long getFilePointer() throws IOException {
        return (long) this.buf.position();
    }

    public void close() throws IOException {
        this.buf = null;
    }
}
