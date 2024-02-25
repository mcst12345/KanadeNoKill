/**
 * Original class written by Vazkii for Botania.
 */

// TEMA: this is the main shader stuff, where the programs are loaded and compiled for the card.
// other relevant files are the shader in /assets/physis/shader/, and the tesr in /client/render/tile/
// they have other comments like this in.

package kanade.kill.util;

import kanade.kill.Launch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ShaderHelper {
    private static final int VERT = ARBVertexShader.GL_VERTEX_SHADER_ARB;
    private static final int FRAG = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
    private static final String PREFIX = "/assets/kanade/shader/";
    public static int cosmicShader = 0;

    public static void initShaders() {
        if (!useShaders()) {
            return;
        }

        cosmicShader = createProgram("cosmic.vert", "cosmic.frag");
    }

    public static void useShader(int shader, ShaderCallback callback) {
        if (!useShaders()) {
            return;
        }

        ARBShaderObjects.glUseProgramObjectARB(shader);

        if (shader != 0) {
            int time = ARBShaderObjects.glGetUniformLocationARB(shader, "time");
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null && mc.player.world != null) {
                ARBShaderObjects.glUniform1iARB(time, (int) (mc.player.world.getWorldTime() % Integer.MAX_VALUE));
            }

            if (callback != null) {
                callback.call(shader);
            }
        }
    }

    public static void useShader(int shader) {
        useShader(shader, null);
    }

    public static void releaseShader() {
        useShader(0);
    }

    public static boolean useShaders() {
        return OpenGlHelper.shadersSupported;
    }

    private static int createProgram(String vert, String frag) {
        int vertId = 0, fragId = 0, program = 0;
        if (vert != null) {
            vertId = createShader(PREFIX + vert, VERT);
        }
        if (frag != null) {
            fragId = createShader(PREFIX + frag, FRAG);
        }

        program = ARBShaderObjects.glCreateProgramObjectARB();
        if (program == 0) {
            return 0;
        }

        if (vert != null) {
            ARBShaderObjects.glAttachObjectARB(program, vertId);
        }
        if (frag != null) {
            ARBShaderObjects.glAttachObjectARB(program, fragId);
        }

        ARBShaderObjects.glLinkProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            Launch.LOGGER.error(getLogInfo(program));
            return 0;
        }

        ARBShaderObjects.glValidateProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            Launch.LOGGER.error(getLogInfo(program));
            return 0;
        }

        return program;
    }

    // Most of the code taken from the LWJGL wiki
    // http://lwjgl.org/wiki/index.php?title=GLSL_Shaders_with_LWJGL

    private static int createShader(String filename, int shaderType) {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if (shader == 0) {
                return 0;
            }

            ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
                throw new RuntimeException("Error creating shader \"" + filename + "\": " + getLogInfo(shader));
            }

            return shader;
        } catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            Launch.LOGGER.warn("Catch exception:", e);
            return -1;
        }
    }

    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    private static String readFileAsString(String filename) throws Exception {
        StringBuilder source = new StringBuilder();
        InputStream in = ShaderHelper.class.getResourceAsStream(filename);
        Exception exception = null;
        BufferedReader reader;

        if (in == null) {
            return "";
        }

        try {
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            Exception innerExc = null;
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    source.append(line).append('\n');
                }
            } catch (Exception exc) {
                exception = exc;
            } finally {
                try {
                    reader.close();
                } catch (Exception exc) {
                    innerExc = exc;
                }
            }

            if (innerExc != null) {
                throw innerExc;
            }
        } catch (Exception exc) {
            exception = exc;
        } finally {
            try {
                in.close();
            } catch (Exception exc) {
                if (exception == null) {
                    exception = exc;
                } else {
                    Launch.LOGGER.warn("Catch exception:", exc);
                }
            }

            if (exception != null) {
                throw exception;
            }
        }

        return source.toString();
    }

    public static abstract class ShaderCallback {

        public abstract void call(int shader);
    }
}
