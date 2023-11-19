package kanade.kill.util;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final ExceptionHandler instance = new ExceptionHandler();

    private ExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();
    }
}
