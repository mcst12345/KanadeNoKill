package org.lwjgl.opengl;

import java.nio.IntBuffer;

public class GLHelper {
    public static void nglPushMatrix(long function_pointer) {
        GL11.nglPushMatrix(function_pointer);
    }

    public static void nglClear(int mask, long function_pointer) {
        GL11.nglClear(mask, function_pointer);
    }

    public static void nglBindFramebuffer(int target, int framebuffer, long function_pointer) {
        GL30.nglBindFramebuffer(target, framebuffer, function_pointer);
    }

    public static void nglViewport(int x, int y, int width, int height, long function_pointer) {
        GL11.nglViewport(x, y, width, height, function_pointer);
    }

    public static void nglPopMatrix(long function_pointer) {
        GL11.nglPopMatrix(function_pointer);
    }

    public static void nglColorMask(boolean red, boolean green, boolean blue, boolean alpha, long function_pointer) {
        GL11.nglColorMask(red, green, blue, alpha, function_pointer);
    }

    public static void nglEnable(int caps, long function_pointer) {
        GL11.nglEnable(caps, function_pointer);
    }

    public static void nglDisable(int caps, long function_pointer) {
        GL11.nglDisable(caps, function_pointer);
    }

    public static void nglDepthMask(boolean flag, long function_pointer) {
        GL11.nglDepthMask(flag, function_pointer);
    }

    public static void nglMatrixMode(int mode, long function_pointer) {
        GL11.nglMatrixMode(mode, function_pointer);
    }

    public static void nglLoadIdentity(long function_pointer) {
        GL11.nglLoadIdentity(function_pointer);
    }

    public static void nglOrtho(double left, double right, double bottom, double top, double zNear, double zFar, long function_pointer) {
        GL11.nglOrtho(left, right, bottom, top, zNear, zFar, function_pointer);
    }

    public static void nglTranslatef(float x, float y, float z, long function_pointer) {
        GL11.nglTranslatef(x, y, z, function_pointer);
    }

    public static void nglColor4f(float colorRed, float colorGreen, float colorBlue, float colorAlpha, long function_pointer) {
        GL11.nglColor4f(colorRed, colorGreen, colorBlue, colorAlpha, function_pointer);
    }

    public static void nglGenFramebuffers(int framebuffers_n, long framebuffers, long function_pointer) {
        GL30.nglGenFramebuffers(framebuffers_n, framebuffers, function_pointer);
    }

    public static void nglGenTextures(int textures_n, long textures, long function_pointer) {
        GL11.nglGenTextures(textures_n, textures, function_pointer);
    }

    public static void nglGenRenderbuffers(int renderbuffers_n, long renderbuffers, long function_pointer) {
        GL30.nglGenRenderbuffers(renderbuffers_n, renderbuffers, function_pointer);
    }

    public static void nglBindTexture(int target, int texture, long function_pointer) {
        GL11.nglBindTexture(target, texture, function_pointer);
    }

    public static void nglTexParameteri(int target, int pname, int param, long function_pointer) {
        GL11.nglTexParameteri(target, pname, param, function_pointer);
    }

    public static void nglTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels, long function_pointer) {
        GL11.nglTexImage2D(target, level, internalformat, width, height, border, format, type, pixels, function_pointer);
    }

    public static void nglFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level, long function_pointer) {
        GL30.nglFramebufferTexture2D(target, attachment, textarget, texture, level, function_pointer);
    }

    public static void nglBindRenderbuffer(int target, int renderbuffer, long function_pointer) {
        GL30.nglBindRenderbuffer(target, renderbuffer, function_pointer);
    }

    public static void nglRenderbufferStorage(int target, int internalformat, int width, int height, long function_pointer) {
        GL30.nglRenderbufferStorage(target, internalformat, width, height, function_pointer);
    }

    public static void nglFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer, long function_pointer) {
        GL30.nglFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer, function_pointer);
    }

    public static void nglClearColor(float red, float green, float blue, float alpha, long function_pointer) {
        GL11.nglClearColor(red, green, blue, alpha, function_pointer);
    }

    public static void nglClearDepth(double depth, long function_pointer) {
        GL11.nglClearDepth(depth, function_pointer);
    }

    public static void nglVertexPointer(int size, int type, int stride, long pointer, long function_pointer) {
        GL11.nglVertexPointer(size, type, stride, pointer, function_pointer);
    }

    public static void nglEnableClientState(int cap, long function_pointer) {
        GL11.nglEnableClientState(cap, function_pointer);
    }

    public static void nglNormalPointer(int type, int stride, long pointer, long function_pointer) {
        GL11.nglNormalPointer(type, stride, pointer, function_pointer);
    }

    public static void nglColorPointer(int size, int type, int stride, long pointer, long function_pointer) {
        GL11.nglColorPointer(size, type, stride, pointer, function_pointer);
    }

    public static void nglClientActiveTexture(int texture, long function_pointer) {
        (StateTracker.getReferences(GLOffsets.caps)).glClientActiveTexture = texture - 33984;
        GL13.nglClientActiveTexture(texture, function_pointer);
    }

    public static void nglTexCoordPointer(int size, int type, int stride, long pointer, long function_pointer) {
        GL11.nglTexCoordPointer(size, type, stride, pointer, function_pointer);
    }

    public static void nglEnableVertexAttribArray(int index, long function_pointer) {
        GL20.nglEnableVertexAttribArray(index, function_pointer);
    }

    public static void nglVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long buffer, long function_pointer) {
        GL20.nglVertexAttribPointer(index, size, type, normalized, stride, buffer, function_pointer);
    }

    public static void nglDisableClientState(int cap, long function_pointer) {
        GL11.nglDisableClientState(cap, function_pointer);
    }

    public static void nglDisableVertexAttribArray(int index, long function_pointer) {
        GL20.nglDisableVertexAttribArray(index, function_pointer);
    }

    public static void nglDrawArrays(int mode, int first, int count, long function_pointer) {
        GL11.nglDrawArrays(mode, first, count, function_pointer);
    }

    public static void nglBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha, long function_pointer) {
        GL14.nglBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha, function_pointer);
    }

    public static void nglShadeModel(int mode, long function_pointer) {
        GL11.nglShadeModel(mode, function_pointer);
    }

    public static void nglScalef(float x, float y, float z, long function_pointer) {
        GL11.nglScalef(x, y, z, function_pointer);
    }

    public static void nglTexParameterf(int target, int pname, float param, long function_pointer) {
        GL11.nglTexParameterf(target, pname, param, function_pointer);
    }

    public static void nglTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels, long function_pointer) {
        GL11.nglTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels, function_pointer);
    }

    public static void nglBegin(int mode, long function_pointer) {
        GL11.nglBegin(mode, function_pointer);
    }

    public static void nglTexCoord2f(float s, float t, long function_pointer) {
        GL11.nglTexCoord2f(s, t, function_pointer);
    }

    public static void nglVertex3f(float x, float y, float z, long function_pointer) {
        GL11.nglVertex3f(x, y, z, function_pointer);
    }

    public static void nglEnd(long function_pointer) {
        GL11.nglEnd(function_pointer);
    }

    public static void nglBlendFunc(int sfactor, int dfactor, long function_pointer) {
        GL11.nglBlendFunc(sfactor, dfactor, function_pointer);
    }

    public static IntBuffer getBufferInt(ContextCapabilities ctxcap) {
        return APIUtil.getBufferInt(ctxcap);
    }
}
