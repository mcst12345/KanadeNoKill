package miku.lib.jvm.hotspot.tools.jcore;

import miku.lib.jvm.hotspot.interpreter.Bytecodes;
import miku.lib.jvm.hotspot.oops.ConstantPool;
import miku.lib.jvm.hotspot.oops.ConstantPoolCache;
import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.runtime.Bytes;

public class ByteCodeRewriter {
    private Method method;
    private ConstantPool cpool;
    private ConstantPoolCache cpCache;
    private byte[] code;
    private static final int jintSize = 4;

    public ByteCodeRewriter(Method method, ConstantPool cpool, byte[] code) {
        this.method = method;
        this.cpool = cpool;
        this.cpCache = cpool.getCache();
        this.code = code;
    }

    protected short getConstantPoolIndexFromRefMap(int rawcode, int bci) {
        String fmt = Bytecodes.format(rawcode);
        int refIndex;
        switch (fmt.length()) {
            case 2:
                refIndex = 255 & this.method.getBytecodeByteArg(bci);
                break;
            case 3:
                refIndex = '\uffff' & Bytes.swapShort(this.method.getBytecodeShortArg(bci));
                break;
            default:
                throw new IllegalArgumentException();
        }

        return (short) this.cpool.objectToCPIndex(refIndex);
    }

    protected short getConstantPoolIndex(int rawcode, int bci) {
        String fmt = Bytecodes.format(rawcode);
        int cpCacheIndex;
        switch (fmt.length()) {
            case 2:
                cpCacheIndex = this.method.getBytecodeByteArg(bci);
                break;
            case 3:
                cpCacheIndex = this.method.getBytecodeShortArg(bci);
                break;
            case 4:
            default:
                throw new IllegalArgumentException();
            case 5:
                if (fmt.contains("__")) {
                    cpCacheIndex = this.method.getBytecodeShortArg(bci);
                } else {
                    cpCacheIndex = this.method.getBytecodeIntArg(bci);
                }
        }

        if (this.cpCache == null) {
            return (short) cpCacheIndex;
        } else if (fmt.contains("JJJJ")) {
            cpCacheIndex = ~cpCacheIndex;
            cpCacheIndex = Bytes.swapInt(cpCacheIndex);
            return (short) this.cpCache.getEntryAt(cpCacheIndex).getConstantPoolIndex();
        } else if (fmt.contains("JJ")) {
            return (short) this.cpCache.getEntryAt('\uffff' & Bytes.swapShort((short) cpCacheIndex)).getConstantPoolIndex();
        } else {
            return fmt.contains("j") ? (short) this.cpCache.getEntryAt(255 & cpCacheIndex).getConstantPoolIndex() : (short) cpCacheIndex;
        }
    }

    private static void writeShort(byte[] buf, int index, short value) {
        buf[index] = (byte) (value >> 8 & 255);
        buf[index + 1] = (byte) (value & 255);
    }

    public void rewrite() {
        int bytecode;
        int hotspotcode;
        int len;

        for (int bci = 0; bci < code.length; ) {
            hotspotcode = Bytecodes.codeAt(method, bci);
            bytecode = Bytecodes.javaCode(hotspotcode);

            int code_from_buffer = 0xFF & code[bci];
            if (!(code_from_buffer == hotspotcode
                    || code_from_buffer == Bytecodes._breakpoint)) {
                throw new IllegalStateException("Unexpected bytecode found in method bytecode buffer!");
            }

            // update the code buffer hotspot specific bytecode with the jvm bytecode
            code[bci] = (byte) (0xFF & bytecode);

            short cpoolIndex = 0;
            switch (bytecode) {
                // bytecodes with ConstantPoolCache index
                case Bytecodes._getstatic:
                case Bytecodes._putstatic:
                case Bytecodes._getfield:
                case Bytecodes._putfield:
                case Bytecodes._invokevirtual:
                case Bytecodes._invokespecial:
                case Bytecodes._invokestatic:
                case Bytecodes._invokeinterface: {
                    cpoolIndex = getConstantPoolIndex(hotspotcode, bci + 1);
                    writeShort(code, bci + 1, cpoolIndex);
                    break;
                }

                case Bytecodes._invokedynamic:
                    cpoolIndex = getConstantPoolIndex(hotspotcode, bci + 1);
                    writeShort(code, bci + 1, cpoolIndex);
                    writeShort(code, bci + 3, (short) 0);  // clear out trailing bytes
                    break;

                case Bytecodes._ldc_w:
                    if (hotspotcode != bytecode) {
                        // fast_aldc_w puts constant in reference map
                        cpoolIndex = getConstantPoolIndexFromRefMap(hotspotcode, bci + 1);
                        writeShort(code, bci + 1, cpoolIndex);
                    }
                    break;
                case Bytecodes._ldc:
                    if (hotspotcode != bytecode) {
                        // fast_aldc puts constant in reference map
                        cpoolIndex = getConstantPoolIndexFromRefMap(hotspotcode, bci + 1);
                        code[bci + 1] = (byte) (cpoolIndex);
                    }
                    break;
            }


            len = Bytecodes.lengthFor(bytecode);
            if (len <= 0) {
                len = Bytecodes.lengthAt(method, bci);
            }

            bci += len;
        }
    }
}
