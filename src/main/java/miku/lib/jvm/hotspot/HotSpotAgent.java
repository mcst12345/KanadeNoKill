package miku.lib.jvm.hotspot;

public class HotSpotAgent {
    private static String[] jvmLibNames;

    static {
        setupJVMLibNames();
    }

    public static String[] getJvmLibNames() {
        return jvmLibNames;
    }

    private static void setupJVMLibNames() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("solaris")) {
            setupJVMLibNamesSolaris();
        } else if (os.contains("win32")) {
            setupJVMLibNamesWin32();
        } else if (os.contains("linux")) {
            setupJVMLibNamesLinux();
        } else if (os.contains("bsd")) {
            setupJVMLibNamesBsd();
        } else {
            if (!os.contains("darwin")) {
                throw new RuntimeException("Unknown OS type");
            }

            setupJVMLibNamesDarwin();
        }

    }

    private static void setupJVMLibNamesDarwin() {
        jvmLibNames = new String[]{"libjvm.dylib"};
    }

    private static void setupJVMLibNamesBsd() {
        jvmLibNames = new String[]{"libjvm.so"};
    }

    private static void setupJVMLibNamesLinux() {
        jvmLibNames = new String[]{"libjvm.so"};
    }

    private static void setupJVMLibNamesSolaris() {
        jvmLibNames = new String[]{"libjvm.so"};
    }

    private static void setupJVMLibNamesWin32() {
        jvmLibNames = new String[]{"jvm.dll"};
    }
}
