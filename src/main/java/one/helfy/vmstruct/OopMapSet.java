package one.helfy.vmstruct;

import one.helfy.JVM;

import java.util.Map;

/**
 * @author aleksei.gromov
 * @date 15.05.2018
 */
public class OopMapSet {
    private static final JVM jvm = JVM.getInstance();
    private static final int REG_COUNT = JVM.intConstant("REG_COUNT");
    private static final int wordSize = JVM.intConstant("oopSize");
    private static final int numberOfRegisters = JVM.intConstant("ConcreteRegisterImpl::number_of_registers");
    private static final int locationValidTypeSize = JVM.type("julong").size * 8;
    private static final long _om_count = JVM.type("OopMapSet").field("_om_count").offset;
    private static final long _om_size = JVM.type("OopMapSet").field("_om_size").offset;
    private static final long _om_data = JVM.type("OopMapSet").field("_om_data").offset;
    private static final long _pc_offset = JVM.type("OopMap").field("_pc_offset").offset;
    private static final long _omv_data = JVM.type("OopMap").field("_omv_data").offset;
    private static final long _omv_count = JVM.type("OopMap").field("_omv_count").offset;
    private static final long _write_stream = JVM.type("OopMap").field("_write_stream").offset;
    private static final long _write_stream_buffer = JVM.type("CompressedStream").field("_buffer").offset;


    public static void updateRegisters(long cb, long pc, long unextendedSP, Map<Integer, Long> registers) {
        long oopMap = CodeCache.getOopMapForReturnAddress(cb, pc);
        if (oopMap == 0) {
            return;
        }
        int stack0 = VMRegImpl.getStack0();
        long compressedStreamAddr = JVM.getAddress(oopMap + _omv_data);
        if (compressedStreamAddr == 0) {
            compressedStreamAddr = JVM.getAddress(oopMap + _write_stream);
            if (compressedStreamAddr == 0) {
                return;
            }
            compressedStreamAddr = JVM.getAddress(compressedStreamAddr);
            if (compressedStreamAddr == 0) {
                return;
            }
        }
        CompressedReadStream crs = new CompressedReadStream(compressedStreamAddr);
        int valueCnt = JVM.getInt(oopMap + _omv_count);
        for (int i = 0; i < valueCnt; i++) {
            OopMapValue omv = new OopMapValue(crs);
            if (omv.getType() != OopMapValue.CALLEE_SAVED_VALUE) {
                return;
            }
            int contentRegNum = omv.getContentRegNum();
            int regNum = omv.getRegNum();
            if (regNum < stack0) {
                registers.put(contentRegNum, registers.get(regNum));
            } else {
                registers.put(contentRegNum, unextendedSP + (long) wordSize * (regNum - stack0));
            }
        }
    }

    public static long getMapAtIndex(long oopMaps, int index) {
        int len = JVM.getInt(oopMaps + _om_count);
        if (index >= len) {
            return 0;
        }
        long cmData = JVM.getAddress(oopMaps + _om_data);
        return JVM.getAddress(cmData + (long) index * wordSize);
    }

    public static long findMapAtOffset(long oopMaps, long pcOffset) {
        int len = JVM.getInt(oopMaps + _om_count);
        long cmData = JVM.getAddress(oopMaps + _om_data);
        long offset = 0;
        long oopMap = 0;
        for (int i = 0; i < len && offset < pcOffset; i++) {
            oopMap = JVM.getAddress(cmData + (long) i * wordSize);
            offset = JVM.getAddress(oopMap + _pc_offset);
        }
        if (offset != pcOffset) {
            return 0;
        }
        return oopMap;
    }
}
