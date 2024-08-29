package one.helfy;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import com.sun.management.HotSpotDiagnosticMXBean;

public class ChangeVmFlag {

    public static void main(String[] args) throws IOException {
        MBeanServer mbserver = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean mxbean =
                ManagementFactory.newPlatformMXBeanProxy(
                        mbserver,
                        "com.sun.management:type=HotSpotDiagnostic",
                        HotSpotDiagnosticMXBean.class);

        System.out.println("[BEFORE] UnlockDiagnosticVMOptions: " + mxbean.getVMOption("UnlockDiagnosticVMOptions"));

        JVM jvm = JVM.getInstance();

        Type flagType = JVM.type("Flag");
        int flagSize = flagType.size;

        Field flagsField = flagType.field("flags");
        long flagsFieldAddress = JVM.getAddress(flagsField.offset);

        Field numFlagsField = flagType.field("numFlags");
        int numFlagsValue = JVM.getInt(numFlagsField.offset);

        Field _nameField = flagType.field("_name");
        Field _addrField = flagType.field("_addr");

        // iterate until `numFlagsValue - 1` because last flag contains null values
        for (int i = 0; i < numFlagsValue - 1; i++) {
            long flagAddress = flagsFieldAddress + ((long) i * flagSize);
            long flagValueAddress = JVM.getAddress(flagAddress + _addrField.offset);
            long flagNameAddress = JVM.getAddress(flagAddress + _nameField.offset);
            String flagName = JVM.getString(flagNameAddress);
            System.err.println(flagName + " = " + JVM.getByte(flagValueAddress));
            if ("UnlockDiagnosticVMOptions".equals(flagName)) {
                if (JVM.getByte(flagValueAddress) == 0) {
                    jvm.putByte(flagValueAddress, (byte) 1);
                    System.out.println(flagName + " has been enabled");
                } else {
                    System.out.println(flagName + " is already enabled");
                }
            }
        }

        System.out.println("[AFTER] UnlockDiagnosticVMOptions: " + mxbean.getVMOption("UnlockDiagnosticVMOptions"));
    }

}
