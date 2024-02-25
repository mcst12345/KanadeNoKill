package kanade.kill;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class GLTest {
    public static void main(String[] args) throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(200, 200));
        Display.setResizable(true);
        Display.setTitle("Test");
    }
}
