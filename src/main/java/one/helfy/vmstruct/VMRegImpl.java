package one.helfy.vmstruct;

import one.helfy.JVM;

/**
 * @author aleksei.gromov
 * @date 17.05.2018
 */
public class VMRegImpl {
    private static final JVM jvm = JVM.getInstance();
    private static final int wordSize = JVM.intConstant("oopSize");
    private static final long stack0 = JVM.type("VMRegImpl").global("stack0");
    private static final long regName = JVM.type("VMRegImpl").global("regName[0]");


    public static int getStack0() {
        return (int) JVM.getAddress(stack0);
    }

    public static String getRegisterName(int index) {
        return JVM.getStringRef(regName + (long) index * wordSize);
    }

}
