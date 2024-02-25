package kanade.kill.fake;

import java.io.IOException;
import java.io.InputStream;

public class FakeInputStream extends InputStream {
    public static final FakeInputStream INSTANCE = new FakeInputStream();

    private FakeInputStream() {
    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}
