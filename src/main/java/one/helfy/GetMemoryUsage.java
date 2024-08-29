package one.helfy;

public class GetMemoryUsage {
    private static long tlabOffset;
    private static long topOffset;
    private static long startOffset;
    private static long endOffset;
    private static long allocatedBytesOffset;

    public static void main(String... args) throws Exception {
        Type jvmThread = JVM.type("Thread");
        allocatedBytesOffset = jvmThread.field("_allocated_bytes").offset;
        Field tlab = jvmThread.field("_tlab");
        tlabOffset = tlab.offset;
        Type tlabType = JVM.type(tlab.typeName);
        // actually both of these point to HeapWord which contains array of chars at offset 0
        topOffset = tlabType.field("_top").offset;
        startOffset = tlabType.field("_start").offset;
        endOffset = tlabType.field("_end").offset;


        java.lang.reflect.Field eetopField = Thread.class.getDeclaredField("eetop");
        eetopField.setAccessible(true);
        long jvmThreadOffset = (long) eetopField.get(Thread.currentThread());
        long threadLocalBufferTop = JVM.getAddress(jvmThreadOffset + tlabOffset + topOffset);
        long threadLocalBufferStart = JVM.getAddress(jvmThreadOffset + tlabOffset + startOffset);
        long threadLocalBufferEnd = JVM.getAddress(jvmThreadOffset + tlabOffset + endOffset);
        long allocatedBytes = JVM.getLong(jvmThreadOffset + allocatedBytesOffset);
        System.out.printf("Thread tlab start: %s, thread tlab top: %s, thread tlab end: %s, diff: %s, Raw Allocated Bytes: %s%n",
                Long.toHexString(threadLocalBufferStart),
                Long.toHexString(threadLocalBufferTop),
                Long.toHexString(threadLocalBufferEnd),
                threadLocalBufferTop - threadLocalBufferStart + allocatedBytes,
                allocatedBytes
        );
        int[] test = new int[500_000];
        threadLocalBufferTop = JVM.getAddress(jvmThreadOffset + tlabOffset + topOffset);
        threadLocalBufferStart = JVM.getAddress(jvmThreadOffset + tlabOffset + startOffset);
        threadLocalBufferEnd = JVM.getAddress(jvmThreadOffset + tlabOffset + endOffset);
        allocatedBytes = JVM.getLong(jvmThreadOffset + allocatedBytesOffset);
        System.out.printf("Thread tlab start: %s, thread tlab top: %s, thread tlab end: %s, diff: %s, Raw Allocated Bytes: %s%n",
                Long.toHexString(threadLocalBufferStart),
                Long.toHexString(threadLocalBufferTop),
                Long.toHexString(threadLocalBufferEnd),
                threadLocalBufferTop - threadLocalBufferStart + allocatedBytes,
                allocatedBytes
        );
    }

}
