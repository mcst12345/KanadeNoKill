package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.code.CompressedReadStream;

public class CompressedLineNumberReadStream extends CompressedReadStream {
    private int bci;
    private int line;

    public CompressedLineNumberReadStream(long buffer) {
        super(buffer);
    }

    public CompressedLineNumberReadStream(long buffer, int position) {
        super(buffer, position);
    }

    public boolean readPair() {
        int next = this.readByte() & 255;
        if (next == 0) {
            return false;
        } else {
            if (next == 255) {
                this.bci += this.readSignedInt();
                this.line += this.readSignedInt();
            } else {
                this.bci += next >> 3;
                this.line += next & 7;
            }

            return true;
        }
    }

    public int bci() {
        return this.bci;
    }

    public int line() {
        return this.line;
    }
}
