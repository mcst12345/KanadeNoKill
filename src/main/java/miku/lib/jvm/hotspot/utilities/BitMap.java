package miku.lib.jvm.hotspot.utilities;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;

public class BitMap {
    private int size;
    private final int[] data;
    private static final int bitsPerWord = 32;
    private static final int bytesPerWord = 4;

    public BitMap(int sizeInBits) {
        this.size = sizeInBits;
        int nofWords = this.sizeInWords();
        this.data = new int[nofWords];
    }

    public int size() {
        return this.size;
    }

    public boolean at(int offset) {
        return Bits.isSetNthBit(this.wordFor(offset), offset % 32);
    }

    public void atPut(int offset, boolean value) {
        int index = this.indexFor(offset);
        int pos = offset % 32;
        if (value) {
            this.data[index] = Bits.setNthBit(this.data[index], pos);
        } else {
            this.data[index] = Bits.clearNthBit(this.data[index], pos);
        }

    }

    public void set_size(int value) {
        this.size = value;
    }

    public void set_map(long addr) {
        for (int i = 0; i < this.sizeInWords(); ++i) {//(int)addr.getCIntegerAt(0L, 4L, true)
            this.data[i] = (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(addr, 4), true);
            addr += 4;
        }
    }

    public void clear() {
        for (int i = 0; i < this.sizeInWords(); ++i) {
            this.data[i] = 0;
        }

    }

    public void iterate(BitMapClosure blk) {
        for (int index = 0; index < this.sizeInWords(); ++index) {
            int rest = this.data[index];

            for (int offset = index * 32; rest != 0; ++offset) {
                if (rest % 2 == 1) {
                    if (offset >= this.size()) {
                        return;
                    }

                    blk.doBit(offset);
                }

                rest >>>= 1;
            }
        }

    }

    public boolean setUnion(BitMap other) {

        boolean changed = false;

        for (int index = 0; index < this.sizeInWords(); ++index) {
            int temp = this.data[index] | other.data[index];
            changed = changed || temp != this.data[index];
            this.data[index] = temp;
        }

        return changed;
    }

    public void setIntersection(BitMap other) {

        for (int index = 0; index < this.sizeInWords(); ++index) {
            this.data[index] &= other.data[index];
        }

    }

    public void setFrom(BitMap other) {

        if (this.sizeInWords() >= 0) System.arraycopy(other.data, 0, this.data, 0, this.sizeInWords());

    }

    public boolean setDifference(BitMap other) {

        boolean changed = false;

        for (int index = 0; index < this.sizeInWords(); ++index) {
            int temp = this.data[index] & ~other.data[index];
            changed = changed || temp != this.data[index];
            this.data[index] = temp;
        }

        return changed;
    }

    public boolean isSame(BitMap other) {

        for (int index = 0; index < this.sizeInWords(); ++index) {
            if (this.data[index] != other.data[index]) {
                return false;
            }
        }

        return true;
    }

    public int getNextOneOffset(int l_offset, int r_offset) {
        if (l_offset == r_offset) {
            return l_offset;
        } else {
            int index = this.indexFor(l_offset);
            int r_index = this.indexFor(r_offset);
            int res_offset = l_offset;
            int pos = this.bitInWord(res_offset);
            int res = this.data[index] >> pos;
            if (res != 0) {
                while ((res & 1) == 0) {
                    res >>= 1;
                    ++res_offset;
                }

                return res_offset;
            } else {
                ++index;

                while (index < r_index) {
                    res = this.data[index];
                    if (res != 0) {
                        for (res_offset = index * 32; (res & 1) == 0; ++res_offset) {
                            res >>= 1;
                        }

                        return res_offset;
                    }

                    ++index;
                }

                return r_offset;
            }
        }
    }

    private int sizeInWords() {
        return (this.size() + 32 - 1) / 32;
    }

    private int indexFor(int offset) {
        return offset / 32;
    }

    private int wordFor(int offset) {
        return this.data[offset / 32];
    }

    private int bitInWord(int offset) {
        return offset & 31;
    }
}
