package one.helfy.vmstruct;

import one.helfy.JVM;

import java.util.HashMap;

public class JavaThread {
    private static final long _anchor = JVM.type("JavaThread").offset("_anchor");
    private static final long _last_Java_sp = _anchor + JVM.type("JavaFrameAnchor").offset("_last_Java_sp");
    private static final long _last_Java_pc = _anchor + JVM.type("JavaFrameAnchor").offset("_last_Java_pc");
    private static final long _last_Java_fp = _anchor + JVM.type("JavaFrameAnchor").offset("_last_Java_fp");

    public static Frame topFrame(long thread) {
        long lastJavaSP = JVM.getAddress(thread + _last_Java_sp);
        long lastJavaFP = JVM.getAddress(thread + _last_Java_fp);
        long lastJavaPC = JVM.getAddress(thread + _last_Java_pc);
        return Frame.getFrame(lastJavaSP, lastJavaFP, lastJavaPC, new HashMap<>());
    }
}