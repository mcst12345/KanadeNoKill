package kanade.kill.reflection;

import kanade.kill.Launch;
import org.lwjgl.opengl.*;

import java.lang.reflect.Method;

public class EarlyMethods {
    public static final Method getDeclaredFields0;
    public static final Method getDeclaredMethods0;
    public static final Method forName0;
    public static final Method getThreads;
    public static final Method invoke0;
    public static final Method getName0;
    public static final Method getFunctionAddress;
    public static final Method getBufferInt;
    public static final Method nglBlendFunc;
    public static final Method nglEnd;
    public static final Method nglVertex3f;
    public static final Method nglTexCoord2f;
    public static final Method nglBegin;
    public static final Method nglTexSubImage2D;
    public static final Method nglTexParameterf;
    public static final Method nglTexSubImage2DBO;
    public static final Method nglScalef;
    public static final Method nglShadeModel;
    public static final Method nglBlendFuncSeparate;
    public static final Method nglDrawArrays;
    public static final Method nglDisableVertexAttribArray;
    public static final Method nglDisableClientState;
    public static final Method nglVertexAttribPointer;
    public static final Method nglEnableVertexAttribArray;
    public static final Method nglTexCoordPointer;
    static {
        try {
            getDeclaredMethods0 = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
            getDeclaredMethods0.setAccessible(true);
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);
            forName0 = Class.class.getDeclaredMethod("forName0", String.class, boolean.class, ClassLoader.class, Class.class);
            forName0.setAccessible(true);
            Class<?> clazz = Class.forName("sun.management.ThreadImpl");
            getThreads = clazz.getDeclaredMethod("getThreads");
            getThreads.setAccessible(true);
            clazz = Class.forName("sun.reflect.NativeMethodAccessorImpl");
            invoke0 = clazz.getDeclaredMethod("invoke0", Method.class, Object.class, Object[].class);
            invoke0.setAccessible(true);
            getName0 = Class.class.getDeclaredMethod("getName0");
            getName0.setAccessible(true);
            if (Launch.client) {
                getFunctionAddress = ReflectionUtil.getMethod(GLContext.class, "ngetFunctionAddress", long.class);
                clazz = Class.forName("org.lwjgl.opengl.APIUtil");
                getBufferInt = ReflectionUtil.getMethod(clazz, "getBufferInt", ContextCapabilities.class);
                nglBlendFunc = ReflectionUtil.getMethod(GL11.class, "nglBlendFunc", int.class, int.class, long.class);
                nglEnd = ReflectionUtil.getMethod(GL11.class, "nglEnd", long.class);
                nglVertex3f = ReflectionUtil.getMethod(GL11.class, "nglVertex3f", float.class, float.class, float.class, long.class);
                nglTexCoord2f = ReflectionUtil.getMethod(GL11.class, "nglTexCoord2f", float.class, float.class, long.class);
                nglBegin = ReflectionUtil.getMethod(GL11.class, "nglBegin", int.class, long.class);
                nglTexSubImage2D = ReflectionUtil.getMethod(GL11.class, "nglTexSubImage2D", int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, long.class, long.class);
                nglTexParameterf = ReflectionUtil.getMethod(GL11.class, "nglTexParameterf", int.class, int.class, float.class, long.class);
                nglTexSubImage2DBO = ReflectionUtil.getMethod(GL11.class, "nglTexSubImage2DBO", int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, long.class, long.class);
                nglScalef = ReflectionUtil.getMethod(GL11.class, "nglScalef", float.class, float.class, float.class, long.class);
                nglShadeModel = ReflectionUtil.getMethod(GL11.class, "nglShadeModel", int.class, long.class);
                nglBlendFuncSeparate = ReflectionUtil.getMethod(GL14.class, "nglBlendFuncSeparate", int.class, int.class, int.class, int.class, long.class);
                nglDrawArrays = ReflectionUtil.getMethod(GL11.class, "nglDrawArrays", int.class, int.class, int.class, long.class);
                nglDisableVertexAttribArray = ReflectionUtil.getMethod(GL20.class, "nglDisableVertexAttribArray", int.class, long.class);
                nglDisableClientState = ReflectionUtil.getMethod(GL11.class, "nglDisableClientState", int.class, long.class);
                nglVertexAttribPointer = ReflectionUtil.getMethod(GL20.class, "nglVertexAttribPointer", int.class, int.class, int.class, boolean.class, int.class, long.class, long.class);
                nglEnableVertexAttribArray = ReflectionUtil.getMethod(GL20.class, "nglEnableVertexAttribArray", int.class, long.class);
                nglTexCoordPointer = ReflectionUtil.getMethod(GL11.class, "nglTexCoordPointer", int.class, int.class, int.class, long.class, long.class);

            } else {
                getFunctionAddress = null;
                getBufferInt = null;
                nglBlendFunc = null;
                nglEnd = null;
                nglVertex3f = null;
                nglTexCoord2f = null;
                nglBegin = null;
                nglTexSubImage2D = null;
                nglTexParameterf = null;
                nglTexSubImage2DBO = null;
                nglScalef = null;
                nglShadeModel = null;
                nglBlendFuncSeparate = null;
                nglDrawArrays = null;
                nglDisableVertexAttribArray = null;
                nglDisableClientState = null;
                nglVertexAttribPointer = null;
                nglEnableVertexAttribArray = null;
                nglTexCoordPointer = null;

            }
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
