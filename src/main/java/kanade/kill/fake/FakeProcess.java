package kanade.kill.fake;

import java.io.InputStream;
import java.io.OutputStream;

public class FakeProcess extends Process {
    @Override
    public OutputStream getOutputStream() {
        return FakeOutputStream.INSTANCE;
    }

    @Override
    public InputStream getInputStream() {
        return FakeInputStream.INSTANCE;
    }

    @Override
    public InputStream getErrorStream() {
        return FakeInputStream.INSTANCE;
    }

    @Override
    public int waitFor() {
        return 0;
    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public void destroy() {

    }
}
