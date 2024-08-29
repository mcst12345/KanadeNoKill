package miku.lib.jvm.hotspot.debugger.win32.coff;

public class COFFException extends RuntimeException {
    public COFFException() {
    }

    public COFFException(Throwable cause) {
        super(cause);
    }

    public COFFException(String message) {
        super(message);
    }

    public COFFException(String message, Throwable cause) {
        super(message, cause);
    }
}
