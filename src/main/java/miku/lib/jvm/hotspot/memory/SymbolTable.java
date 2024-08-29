package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.oops.Symbol;
import miku.lib.jvm.hotspot.utilities.Hashtable;
import miku.lib.jvm.hotspot.utilities.HashtableEntry;
import one.helfy.JVM;
import one.helfy.Type;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SymbolTable extends Hashtable {
    private static final long _the_table;

    static {
        Type type = JVM.type("SymbolTable");
        _the_table = unsafe.getAddress(type.global("_the_table"));
    }

    private static final SymbolTable INSTANCE = new SymbolTable(_the_table);

    private SymbolTable(long address) {
        super(address);
    }

    public static SymbolTable getTheTable() {
        return INSTANCE;
    }

    public Symbol probe(byte[] name) {
        long hashValue = hashSymbol(name);

        for (HashtableEntry e = (HashtableEntry) this.bucket(this.hashToIndex(hashValue)); e != null; e = (HashtableEntry) e.next()) {
            if (e.hash() == hashValue) {
                Symbol sym = new Symbol(e.literal());
                if (sym.equals(name)) {
                    return sym;
                }
            }
        }

        return null;
    }

    public Symbol probe(String name) {
        try {
            return this.probe(toModifiedUTF8Bytes(name));
        } catch (IOException var3) {
            return null;
        }
    }

    private static byte[] toModifiedUTF8Bytes(String name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(name);
        dos.flush();
        byte[] buf = baos.toByteArray();
        byte[] res = new byte[buf.length - 2];
        System.arraycopy(buf, 2, res, 0, res.length);
        return res;
    }

    public interface SymbolVisitor {
        void visit(Symbol var1);
    }

    public void symbolsDo(SymbolVisitor visitor) {
        int numBuckets = this.size();

        for (int i = 0; i < numBuckets; ++i) {
            for (HashtableEntry e = (HashtableEntry) this.bucket(i); e != null; e = (HashtableEntry) e.next()) {
                visitor.visit(new Symbol(e.literal()));
            }
        }

    }
}
