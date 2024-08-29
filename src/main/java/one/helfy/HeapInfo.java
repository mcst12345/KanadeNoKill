package one.helfy;

public class HeapInfo {

    public static void main(String[] args) {

        // Parallel GC
        Type parallelScavengeHeap = JVM.type("ParallelScavengeHeap");
        long youngGen = JVM.getAddress(parallelScavengeHeap.global("_young_gen"));
        long oldGen = JVM.getAddress(parallelScavengeHeap.global("_old_gen"));
        if (youngGen != 0 && oldGen != 0) {
            System.out.println("ParallelScavengeHeap detected");
            Type psVirtualSpace = JVM.type("PSVirtualSpace");

            long vs = JVM.getAddress(youngGen + JVM.type("PSYoungGen").offset("_virtual_space"));
            long start = JVM.getAddress(vs + psVirtualSpace.offset("_reserved_low_addr"));
            long end = JVM.getAddress(vs + psVirtualSpace.offset("_reserved_high_addr"));
            System.out.println("PSYoungGen: 0x" + Long.toHexString(start) + " - 0x" + Long.toHexString(end));

            vs = JVM.getAddress(oldGen + JVM.type("PSOldGen").offset("_virtual_space"));
            start = JVM.getAddress(vs + psVirtualSpace.offset("_reserved_low_addr"));
            end = JVM.getAddress(vs + psVirtualSpace.offset("_reserved_high_addr"));
            System.out.println("PSOldGen: 0x" + Long.toHexString(start) + " - 0x" + Long.toHexString(end));
        }

        // CMS GC
        Type genCollectedHeap = JVM.type("GenCollectedHeap");
        long gch = JVM.getAddress(genCollectedHeap.global("_gch"));
        if (gch != 0) {
            System.out.println("GenCollectedHeap detected");
            Type generation = JVM.type("Generation");
            Type virtualSpace = JVM.type("VirtualSpace");
            int ptrSize = JVM.type("Generation*").size;

            int nGens = JVM.getInt(gch + genCollectedHeap.offset("_n_gens"));
            for (int i = 0; i < nGens; i++) {
                long gen = JVM.getAddress(gch + genCollectedHeap.offset("_gens") + (long) i * ptrSize);
                long vs = gen + generation.offset("_virtual_space");
                long start = JVM.getAddress(vs + virtualSpace.offset("_low_boundary"));
                long end = JVM.getAddress(vs + virtualSpace.offset("_high_boundary"));
                System.out.println("Generation " + i + ": 0x" + Long.toHexString(start) + " - 0x" + Long.toHexString(end));
            }
        }
    }
}
