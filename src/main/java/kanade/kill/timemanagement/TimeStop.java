package kanade.kill.timemanagement;

public class TimeStop {
    private static boolean timestop = false;

    public static void SetTimeStop(boolean b) {
        timestop = b;
    }

    public static boolean isTimeStop() {
        return timestop;
    }
}
