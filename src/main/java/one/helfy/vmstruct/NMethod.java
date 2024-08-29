package one.helfy.vmstruct;

import one.helfy.JVM;

/**
 * @author aleksei.gromov
 * @date 26.04.2018
 */
public class NMethod {
    private static final JVM jvm = JVM.getInstance();

    private static final int wordSize = JVM.intConstant("oopSize");
    private static final long _scope_data_begin = JVM.type("nmethod").offset("_scopes_data_offset");
    private static final long _scope_start = JVM.type("nmethod").offset("_scopes_pcs_offset");
    private static final long _scope_end = JVM.type("nmethod").offset("_dependencies_offset");
    private static final long _metadata_begin = JVM.type("nmethod").offset("_metadata_offset");
    private static final long _oops_begin = JVM.type("nmethod").offset("_oops_offset");
    private static final long _compile_level = JVM.type("nmethod").offset("_comp_level");

    public static long getMetadataAt(long cb, int index) {
        if (index == 0) {
            return 0;
        }
        //senderPC = senderSP.getAddressAt(-1L * VM.getVM().getAddressSize());
        //long senderPC = jvm.getAddress(senderSP - slot_return_addr * wordSize);
        return JVM.getAddress(getMetadataBegin(cb) + (long) (index - 1) * wordSize);
    }

    public static long getOopAt(long cb, int index) {
        if (index == 0) {
            return 0;
        }
        return JVM.getAddress(getOopBegin(cb) + (long) (index - 1) * wordSize);
    }

    public static long getScopesDataBegin(long cb) {
        return cb + JVM.getInt(cb + _scope_data_begin);
    }

    private static long getMetadataBegin(long cb) {
        return cb + JVM.getInt(cb + _metadata_begin);
    }

    private static long getOopBegin(long cb) {
        return cb + JVM.getInt(cb + _oops_begin);
    }

    public static long getCompileLevel(long cb) {
        return JVM.getInt(cb + _compile_level);
    }

}
