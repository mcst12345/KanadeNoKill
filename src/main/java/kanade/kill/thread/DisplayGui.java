package kanade.kill.thread;

import kanade.kill.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.MemoryUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLHelper;
import org.lwjgl.opengl.GLOffsets;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class DisplayGui {
    private static boolean display;

    private DisplayGui() {
    }

    public synchronized static void display() {
        display = true;
    }

    public static void run() { //Wll be called from he Minecraft client thread.
        {
            if (display) {
                BufferBuilder bufferbuilder = new BufferBuilder(2097152);
                FontRenderer fontRenderer = new FontRenderer(null, 0);
                int width = -1;
                int height = -1;
                int framebufferObject = -1;
                int framebufferTexture = -1;
                float fogAlpha = 0.0F;
                boolean alphaBackoff = false;
                float backgroundRed = 0.0F;
                boolean redBackoff = false;
                int mouseY;
                if (width != Display.getWidth() || height != Display.getHeight()) {
                    IntBuffer framebuffers = GLHelper.getBufferInt(GLOffsets.caps);
                    GLHelper.nglGenFramebuffers(1, MemoryUtil.getAddress0(framebuffers) + ((long) framebuffers.position() << 2), GLOffsets.glGenFramebuffers);
                    framebufferObject = framebuffers.get(0);
                    IntBuffer textures = GLHelper.getBufferInt(GLOffsets.caps);
                    GLHelper.nglGenTextures(1, MemoryUtil.getAddress0(textures) + ((long) textures.position() << 2), GLOffsets.glGenTextures);
                    framebufferTexture = textures.get(0);
                    IntBuffer renderbuffers = GLHelper.getBufferInt(GLOffsets.caps);
                    GLHelper.nglGenRenderbuffers(1, MemoryUtil.getAddress0(renderbuffers) + ((long) renderbuffers.position() << 2), GLOffsets.glGenRenderbuffers);
                    int depthBuffer = renderbuffers.get(0);
                    GLHelper.nglBindTexture(3553, framebufferTexture, GLOffsets.glBindTexture);
                    GLHelper.nglTexParameteri(3553, 10241, 9728, GLOffsets.glTexParameteri);
                    GLHelper.nglTexParameteri(3553, 10240, 9728, GLOffsets.glTexParameteri);
                    GLHelper.nglTexParameteri(3553, 10242, 10496, GLOffsets.glTexParameteri);
                    GLHelper.nglTexParameteri(3553, 10243, 10496, GLOffsets.glTexParameteri);
                    GLHelper.nglBindTexture(3553, 0, GLOffsets.glBindTexture);
                    GLHelper.nglBindTexture(3553, framebufferTexture, GLOffsets.glBindTexture);
                    GLHelper.nglTexImage2D(3553, 0, 32856, Display.getWidth(), Display.getHeight(), 0, 6408, 5121, 0L, GLOffsets.glTexImage2D);
                    GLHelper.nglBindFramebuffer(36160, framebufferObject, GLOffsets.glBindFramebuffer);
                    GLHelper.nglFramebufferTexture2D(36160, 36064, 3553, framebufferTexture, 0, GLOffsets.glFramebufferTexture2D);
                    GLHelper.nglBindRenderbuffer(36161, depthBuffer, GLOffsets.glBindRenderbuffer);
                    GLHelper.nglRenderbufferStorage(36161, 33190, Display.getWidth(), Display.getHeight(), GLOffsets.glRenderbufferStorage);
                    GLHelper.nglFramebufferRenderbuffer(36160, 36096, 36161, depthBuffer, GLOffsets.glFramebufferRenderbuffer);
                    GLHelper.nglBindFramebuffer(36160, framebufferObject, GLOffsets.glBindFramebuffer);
                    GLHelper.nglViewport(0, 0, Display.getWidth(), Display.getHeight(), GLOffsets.glViewport);
                    GLHelper.nglClearColor(0.0F, 0.0F, 0.0F, 0.0F, GLOffsets.glClearColor);
                    mouseY = 16384;
                    GLHelper.nglClearDepth(1.0, GLOffsets.glClearDepth);
                    mouseY |= 256;
                    GLHelper.nglClear(mouseY, GLOffsets.glClear);
                    GLHelper.nglBindFramebuffer(36160, 0, GLOffsets.glBindFramebuffer);
                    GLHelper.nglBindTexture(3553, 0, GLOffsets.glBindTexture);
                }

                GLHelper.nglBindFramebuffer(36160, 0, GLOffsets.glBindFramebuffer);
                GLHelper.nglPopMatrix(GLOffsets.glPopMatrix);
                GLHelper.nglPushMatrix(GLOffsets.glPushMatrix);
                GLHelper.nglClear(16640, GLOffsets.glClear);
                GLHelper.nglBindFramebuffer(36160, framebufferObject, GLOffsets.glBindFramebuffer);
                GLHelper.nglViewport(0, 0, Display.getWidth(), Display.getHeight(), GLOffsets.glViewport);
                GLHelper.nglEnable(3553, GLOffsets.glEnable);
                ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
                int scaledWidth = scaledresolution.getScaledWidth();
                int scaledHeight = scaledresolution.getScaledHeight();
                int mouseX = Mouse.getX() * scaledWidth / Display.getWidth();
                mouseY = scaledHeight - Mouse.getY() * scaledHeight / Display.getHeight() - 1;
                GLHelper.nglViewport(0, 0, Display.getWidth(), Display.getHeight(), GLOffsets.glViewport);
                GLHelper.nglMatrixMode(5889, GLOffsets.glMatrixMode);
                GLHelper.nglLoadIdentity(GLOffsets.glLoadIdentity);
                GLHelper.nglMatrixMode(5888, GLOffsets.glMatrixMode);
                GLHelper.nglLoadIdentity(GLOffsets.glLoadIdentity);
                GLHelper.nglClear(256, GLOffsets.glClear);
                GLHelper.nglMatrixMode(5889, GLOffsets.glMatrixMode);
                GLHelper.nglLoadIdentity(GLOffsets.glLoadIdentity);
                GLHelper.nglOrtho(0.0, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0, 1000.0, 3000.0, GLOffsets.glOrtho);
                GLHelper.nglMatrixMode(5888, GLOffsets.glMatrixMode);
                GLHelper.nglLoadIdentity(GLOffsets.glLoadIdentity);
                GLHelper.nglTranslatef(0.0F, 0.0F, -2000.0F, GLOffsets.glTranslatef);
                GLHelper.nglClear(256, GLOffsets.glClear);
                if (redBackoff) {
                    backgroundRed += 0.0625F;
                } else {
                    backgroundRed -= 0.0625F;
                }

                if (backgroundRed >= 1.0F) {
                    backgroundRed = 1.0F;
                } else if (backgroundRed <= 0.0F) {
                    backgroundRed = 0.0F;
                }

                float af = 1.0F;
                float af2 = 1.0F;
                float af3 = 1.0F;
                float af4 = 1.0F;
                float af5 = 1.0F;
                float af6 = 1.0F;
                float af7 = 1.0F;
                GLHelper.nglDisable(3553, GLOffsets.glDisable);
                GLHelper.nglEnable(3042, GLOffsets.glEnable);
                GLHelper.nglDisable(3008, GLOffsets.glDisable);
                GLHelper.nglBlendFuncSeparate(770, 771, 1, 0, GLOffsets.glBlendFuncSeparate);
                GLHelper.nglShadeModel(7425, GLOffsets.glShadeModel);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos(scaledWidth, scaledHeight, 0.0).color(backgroundRed, af2, af3, af).endVertex();
                bufferbuilder.pos(0.0, scaledHeight, 0.0).color(backgroundRed, af2, af3, af).endVertex();
                bufferbuilder.pos(0.0, 0.0, 0.0).color(af5, af6, af7, af4).endVertex();
                bufferbuilder.pos(scaledWidth, 0.0, 0.0).color(af5, af6, af7, af4).endVertex();
                bufferbuilder.finishDrawing();
                VertexFormat vertexformat = bufferbuilder.getVertexFormat();
                int i2 = vertexformat.getNextOffset();
                ByteBuffer bytebuffer = bufferbuilder.getByteBuffer();
                List<VertexFormatElement> list = vertexformat.getElements();

                int i1;
                int x;
                for (i1 = 0; i1 < list.size(); ++i1) {
                    VertexFormatElement vertexformatelement = list.get(i1);
                    x = vertexformatelement.getIndex();
                    bytebuffer.position(vertexformat.getOffset(i1));
                    vertexformatelement.getUsage().preDraw(vertexformat, i1, i2, bytebuffer);
                }

                GLHelper.nglDrawArrays(bufferbuilder.getDrawMode(), 0, bufferbuilder.getVertexCount(), GLOffsets.glDrawArrays);
                i1 = 0;

                for (int j1 = list.size(); i1 < j1; ++i1) {
                    VertexFormatElement vertexformatelement1x = list.get(i1);
                    vertexformatelement1x.getUsage().postDraw(vertexformat, i1, i2, bytebuffer);
                }

                bufferbuilder.reset();
                GLHelper.nglShadeModel(7424, GLOffsets.glShadeModel);
                GLHelper.nglDisable(3042, GLOffsets.glDisable);
                GLHelper.nglEnable(3008, GLOffsets.glEnable);
                GLHelper.nglEnable(3553, GLOffsets.glEnable);
                GLHelper.nglPushMatrix(GLOffsets.glPushMatrix);
                GLHelper.nglScalef(2.0F, 2.0F, 2.0F, GLOffsets.glScalef);
                String title = "You die!";
                fontRenderer.refresh().drawStringWithShadowIcely(title, (float) (scaledWidth / 2 / 2 - fontRenderer.getStringWidth(title) / 2), 30.0F, 16777215);
                GLHelper.nglPopMatrix(GLOffsets.glPopMatrix);
                String causeOfDeath = "§f僕らは命に嫌われている。";
                fontRenderer.refresh().drawStringWithShadowIcely(causeOfDeath, (float) (scaledWidth / 2 - fontRenderer.getStringWidth(causeOfDeath) / 2), 85.0F, 16777215);
                String score = "Score: 25";
                fontRenderer.refresh().drawStringWithShadowIcely(score, (float) (scaledWidth / 2 - fontRenderer.getStringWidth(score) / 2), 100.0F, 16777215);
                fontRenderer.bindTexDirect("/assets/northest_attack/textures/widgets.png");
                GLHelper.nglColor4f(1.0F, 1.0F, 1.0F, 1.0F, GLOffsets.glColor4f);
                x = scaledWidth / 2 - 100;
                int y = scaledHeight / 4 + 72;
                boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + 200 && mouseY < y + 20;
                int i = hovered ? 2 : 1;
                GLHelper.nglEnable(3042, GLOffsets.glEnable);
                GLHelper.nglBlendFuncSeparate(770, 771, 1, 0, GLOffsets.glBlendFuncSeparate);
                GLHelper.nglBlendFunc(770, 771, GLOffsets.glBlendFunc);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(x, y + 20, 0.0).tex(0.0, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 100, y + 20, 0.0).tex(0.390625, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 100, y, 0.0).tex(0.390625, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x, y, 0.0).tex(0.0, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.finishDrawing();
                vertexformat = bufferbuilder.getVertexFormat();
                i2 = vertexformat.getNextOffset();
                bytebuffer = bufferbuilder.getByteBuffer();
                list = vertexformat.getElements();

                int j1xx;
                VertexFormatElement vertexformatelement1xxx;
                for (j1xx = 0; j1xx < list.size(); ++j1xx) {
                    vertexformatelement1xxx = list.get(j1xx);
                    bytebuffer.position(vertexformat.getOffset(j1xx));
                    vertexformatelement1xxx.getUsage().preDraw(vertexformat, j1xx, i2, bytebuffer);
                }

                GLHelper.nglDrawArrays(bufferbuilder.getDrawMode(), 0, bufferbuilder.getVertexCount(), GLOffsets.glDrawArrays);
                i1 = 0;

                for (j1xx = list.size(); i1 < j1xx; ++i1) {
                    vertexformatelement1xxx = list.get(i1);
                    vertexformatelement1xxx.getUsage().postDraw(vertexformat, i1, i2, bytebuffer);
                }

                bufferbuilder.reset();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos((double) x + 100.0, y + 20, 0.0).tex(0.390625, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 200, y + 20, 0.0).tex(0.78125, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 200, y, 0.0).tex(0.78125, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x, y, 0.0).tex(0.390625, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.finishDrawing();
                vertexformat = bufferbuilder.getVertexFormat();
                i2 = vertexformat.getNextOffset();
                bytebuffer = bufferbuilder.getByteBuffer();
                list = vertexformat.getElements();

                for (j1xx = 0; j1xx < list.size(); ++j1xx) {
                    vertexformatelement1xxx = list.get(j1xx);
                    bytebuffer.position(vertexformat.getOffset(j1xx));
                    vertexformatelement1xxx.getUsage().preDraw(vertexformat, j1xx, i2, bytebuffer);
                }

                GLHelper.nglDrawArrays(bufferbuilder.getDrawMode(), 0, bufferbuilder.getVertexCount(), GLOffsets.glDrawArrays);
                i1 = 0;

                for (j1xx = list.size(); i1 < j1xx; ++i1) {
                    vertexformatelement1xxx = list.get(i1);
                    vertexformatelement1xxx.getUsage().postDraw(vertexformat, i1, i2, bytebuffer);
                }

                bufferbuilder.reset();
                String displayString = "Gjenfødt";
                fontRenderer.refresh().drawStringWithShadowIcely(displayString, (float) (x + 100 - fontRenderer.getStringWidth(displayString) / 2), (float) (y + 6), 16777215);
                fontRenderer.bindTexDirect("/assets/northest_attack/textures/widgets.png");
                GLHelper.nglColor4f(1.0F, 1.0F, 1.0F, 1.0F, GLOffsets.glColor4f);
                x = scaledWidth / 2 - 100;
                y = scaledHeight / 4 + 96;
                hovered = mouseX >= x && mouseY >= y && mouseX < x + 200 && mouseY < y + 20;
                i = hovered ? 2 : 1;
                GLHelper.nglEnable(3042, GLOffsets.glEnable);
                GLHelper.nglBlendFuncSeparate(770, 771, 1, 0, GLOffsets.glBlendFuncSeparate);
                GLHelper.nglBlendFunc(770, 771, GLOffsets.glBlendFunc);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(x, y + 20, 0.0).tex(0.0, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 100, y + 20, 0.0).tex(0.390625, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 100, y, 0.0).tex(0.390625, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x, y, 0.0).tex(0.0, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.finishDrawing();
                vertexformat = bufferbuilder.getVertexFormat();
                i2 = vertexformat.getNextOffset();
                bytebuffer = bufferbuilder.getByteBuffer();
                list = vertexformat.getElements();

                int j1xxx;
                int j1x;
                VertexFormatElement vertexformatelement1xx;
                VertexFormatElement.EnumUsage vertexformatelement$enumusage1xx;
                for (j1x = 0; j1x < list.size(); ++j1x) {
                    vertexformatelement1xx = list.get(j1x);
                    bytebuffer.position(vertexformat.getOffset(j1x));
                    vertexformatelement1xx.getUsage().preDraw(vertexformat, j1x, i2, bytebuffer);
                }

                GLHelper.nglDrawArrays(bufferbuilder.getDrawMode(), 0, bufferbuilder.getVertexCount(), GLOffsets.glDrawArrays);
                i1 = 0;

                for (j1x = list.size(); i1 < j1x; ++i1) {
                    vertexformatelement1xx = list.get(i1);
                    vertexformatelement1xx.getUsage().postDraw(vertexformat, i1, i2, bytebuffer);
                }

                bufferbuilder.reset();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos((double) x + 100.0, y + 20, 0.0).tex(0.390625, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 200, y + 20, 0.0).tex(0.78125, (float) (46 + i * 20 + 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x + 200, y, 0.0).tex(0.78125, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.pos(x, y, 0.0).tex(0.390625, (float) (46 + i * 20) * 0.00390625F).endVertex();
                bufferbuilder.finishDrawing();
                vertexformat = bufferbuilder.getVertexFormat();
                i2 = vertexformat.getNextOffset();
                bytebuffer = bufferbuilder.getByteBuffer();
                list = vertexformat.getElements();

                for (j1x = 0; j1x < list.size(); ++j1x) {
                    vertexformatelement1xx = list.get(j1x);
                    bytebuffer.position(vertexformat.getOffset(j1x));
                    vertexformatelement1xx.getUsage().preDraw(vertexformat, j1x, i2, bytebuffer);
                }

                GLHelper.nglDrawArrays(bufferbuilder.getDrawMode(), 0, bufferbuilder.getVertexCount(), GLOffsets.glDrawArrays);
                i1 = 0;

                for (j1x = list.size(); i1 < j1x; ++i1) {
                    vertexformatelement1xx = list.get(i1);
                    vertexformatelement1xx.getUsage().postDraw(vertexformat, i1, i2, bytebuffer);
                }

                bufferbuilder.reset();
                displayString = ":)";
                fontRenderer.refresh().drawStringWithShadowIcely(displayString, (float) (x + 100 - fontRenderer.getStringWidth(displayString) / 2), (float) (y + 6), 16777215);
                if (alphaBackoff) {
                    fogAlpha -= 0.03125F;
                } else {
                    fogAlpha += 0.03125F;
                }

                if (fogAlpha >= 0.3F) {
                    fogAlpha = 0.3F;
                } else if (fogAlpha <= 0.0F) {
                    fogAlpha = 0.0F;
                }

                GLHelper.nglDisable(3553, GLOffsets.glDisable);
                GLHelper.nglEnable(3042, GLOffsets.glEnable);
                GLHelper.nglDisable(3008, GLOffsets.glDisable);
                GLHelper.nglBlendFuncSeparate(770, 771, 1, 0, GLOffsets.glBlendFuncSeparate);
                GLHelper.nglShadeModel(7425, GLOffsets.glShadeModel);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos(scaledWidth, scaledHeight, 0.0).color(1.0F, 1.0F, 1.0F, fogAlpha).endVertex();
                bufferbuilder.pos(0.0, scaledWidth, 0.0).color(1.0F, 1.0F, 1.0F, fogAlpha).endVertex();
                bufferbuilder.pos(0.0, 0.0, 0.0).color(1.0F, 1.0F, 1.0F, fogAlpha).endVertex();
                bufferbuilder.pos(scaledHeight, 0.0, 0.0).color(1.0F, 1.0F, 1.0F, fogAlpha).endVertex();
                bufferbuilder.finishDrawing();
                vertexformat = bufferbuilder.getVertexFormat();
                i2 = vertexformat.getNextOffset();
                bytebuffer = bufferbuilder.getByteBuffer();
                list = vertexformat.getElements();

                for (j1x = 0; j1x < list.size(); ++j1x) {
                    vertexformatelement1xx = list.get(j1x);
                    bytebuffer.position(vertexformat.getOffset(j1x));
                    vertexformatelement1xx.getUsage().preDraw(vertexformat, j1x, i2, bytebuffer);
                }

                GLHelper.nglDrawArrays(bufferbuilder.getDrawMode(), 0, bufferbuilder.getVertexCount(), GLOffsets.glDrawArrays);
                i1 = 0;

                for (j1x = list.size(); i1 < j1x; ++i1) {
                    vertexformatelement1xx = list.get(i1);
                    vertexformatelement1xx.getUsage().postDraw(vertexformat, i1, i2, bytebuffer);
                }

                bufferbuilder.reset();
                GLHelper.nglShadeModel(7424, GLOffsets.glShadeModel);
                GLHelper.nglDisable(3042, GLOffsets.glDisable);
                GLHelper.nglEnable(3008, GLOffsets.glEnable);
                GLHelper.nglEnable(3553, GLOffsets.glEnable);
                GLHelper.nglBindFramebuffer(36160, 0, GLOffsets.glBindFramebuffer);
                GLHelper.nglPopMatrix(GLOffsets.glPopMatrix);
                GLHelper.nglPushMatrix(GLOffsets.glPushMatrix);
                GLHelper.nglColorMask(true, true, true, false, GLOffsets.glColorMask);
                GLHelper.nglDisable(2929, GLOffsets.glDisable);
                GLHelper.nglDepthMask(false, GLOffsets.glDepthMask);
                GLHelper.nglMatrixMode(5889, GLOffsets.glMatrixMode);
                GLHelper.nglLoadIdentity(GLOffsets.glLoadIdentity);
                GLHelper.nglOrtho(0.0, Display.getWidth(), Display.getHeight(), 0.0, 1000.0, 3000.0, GLOffsets.glOrtho);
                GLHelper.nglMatrixMode(5888, GLOffsets.glMatrixMode);
                GLHelper.nglLoadIdentity(GLOffsets.glLoadIdentity);
                GLHelper.nglTranslatef(0.0F, 0.0F, -2000.0F, GLOffsets.glTranslatef);
                GLHelper.nglViewport(0, 0, Display.getWidth(), Display.getHeight(), GLOffsets.glViewport);
                GLHelper.nglEnable(3553, GLOffsets.glEnable);
                GLHelper.nglDisable(2896, GLOffsets.glDisable);
                GLHelper.nglDisable(3008, GLOffsets.glDisable);
                GLHelper.nglDisable(3042, GLOffsets.glDisable);
                GLHelper.nglEnable(2903, GLOffsets.glEnable);
                GLHelper.nglColor4f(1.0F, 1.0F, 1.0F, 1.0F, GLOffsets.glColor4f);
                GLHelper.nglBindTexture(3553, framebufferTexture, GLOffsets.glBindTexture);
                float f = (float) Display.getWidth();
                float f1 = (float) Display.getHeight();
                float f2 = 1.0F;
                float f3 = 1.0F;
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(0.0, f1, 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex();
                bufferbuilder.pos(f, f1, 0.0).tex(f2, 0.0).color(255, 255, 255, 255).endVertex();
                bufferbuilder.pos(f, 0.0, 0.0).tex(f2, f3).color(255, 255, 255, 255).endVertex();
                bufferbuilder.pos(0.0, 0.0, 0.0).tex(0.0, f3).color(255, 255, 255, 255).endVertex();
                bufferbuilder.finishDrawing();
                vertexformat = bufferbuilder.getVertexFormat();
                i2 = vertexformat.getNextOffset();
                bytebuffer = bufferbuilder.getByteBuffer();
                list = vertexformat.getElements();

                VertexFormatElement vertexformatelement1;
                for (j1xxx = 0; j1xxx < list.size(); ++j1xxx) {
                    vertexformatelement1 = list.get(j1xxx);
                    bytebuffer.position(vertexformat.getOffset(j1xxx));
                    vertexformatelement1.getUsage().preDraw(vertexformat, j1xxx, i2, bytebuffer);
                }

                GLHelper.nglDrawArrays(bufferbuilder.getDrawMode(), 0, bufferbuilder.getVertexCount(), GLOffsets.glDrawArrays);
                i1 = 0;

                for (j1xxx = list.size(); i1 < j1xxx; ++i1) {
                    vertexformatelement1 = list.get(i1);
                    vertexformatelement1.getUsage().postDraw(vertexformat, i1, i2, bytebuffer);
                }

                bufferbuilder.reset();
                GLHelper.nglBindTexture(3553, 0, GLOffsets.glBindTexture);
                GLHelper.nglDepthMask(true, GLOffsets.glDepthMask);
                GLHelper.nglColorMask(true, true, true, true, GLOffsets.glColorMask);
                GLHelper.nglPopMatrix(GLOffsets.glPopMatrix);
                Mouse.setGrabbed(false);
                Display.update(true);
                Thread.yield();
            }
        }
    }
}