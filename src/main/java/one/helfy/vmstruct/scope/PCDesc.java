package one.helfy.vmstruct.scope;

import one.helfy.JVM;

/**
 * @author aleksei.gromov
 * @date 26.04.2018
 */
public class PCDesc {
    private static final JVM jvm = JVM.getInstance();
    private static final long _scope_data_start = JVM.type("nmethod").offset("_scopes_data_offset");
    private static final long _scope_pcs_start = JVM.type("nmethod").offset("_scopes_pcs_offset");
    private static final long _scope_end = JVM.type("nmethod").offset("_dependencies_offset");
    private static final long _pc_offset = JVM.type("PcDesc").offset("_pc_offset");
    private static final long _content_offset = JVM.type("CodeBlob").offset("_content_offset");
    private static final long _size = JVM.type("PcDesc").size;
    private static final long _scope_decode_offset = JVM.type("PcDesc").offset("_scope_decode_offset");
    private static final long _obj_decode_offset = JVM.type("PcDesc").offset("_obj_decode_offset");
    private static final long _flags = JVM.type("PcDesc").offset("_flags");
    private static final int reexecuteMask = JVM.intConstant("PcDesc::PCDESC_reexecute");
    private static final int isMethodHandleInvokeMask = JVM.intConstant("PcDesc::PCDESC_is_method_handle_invoke");
    private static final int returnOopMask = JVM.intConstant("PcDesc::PCDESC_return_oop");

    public static ScopeDesc getPCDescAt(long pc, long cb) {
        long scopesPcBegin = cb + JVM.getInt(cb + _scope_pcs_start);
        long scopesPcEnd = cb + JVM.getInt(cb + _scope_end);
        for (long pcDescSearch = scopesPcBegin; pcDescSearch < scopesPcEnd; pcDescSearch += _size) {
            if (getRealPc(cb, pcDescSearch) == pc) {
                return new ScopeDesc(cb, pcDescSearch);
            }
        }
        return null;
    }

    public static long getRealPc(long cb, long pcdesc) {
        long codeBegin = cb + JVM.getInt(cb + _content_offset);
        return codeBegin + JVM.getInt(pcdesc + _pc_offset);
    }

    public static int getScopeDecodeOffset(long pcdesc) {
        return JVM.getInt(pcdesc + _scope_decode_offset);
    }

    public static int getObjectDecodeOffset(long pcdesc) {
        return JVM.getInt(pcdesc + _obj_decode_offset);
    }
}
