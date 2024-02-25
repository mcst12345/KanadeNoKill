package kanade.kill.fake;

import java.io.IOException;
import java.io.OutputStream;

public class FakeOutputStream extends OutputStream {
    public static final FakeOutputStream INSTANCE = new FakeOutputStream();

    private FakeOutputStream() {
    }

    @Override
    public void write(int b) throws IOException {

    }
}
