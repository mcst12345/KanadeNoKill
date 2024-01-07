//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kanade.kill.util;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import kanade.kill.Empty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GLOffsets;
import org.lwjgl.opengl.OpenGLHelper;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;

@SideOnly(Side.CLIENT)
public class FontRenderer {
    private static final String[] UNICODE_PAGE_LOCATIONS = new String[256];
    private static final IntBuffer DATA_BUFFER = ByteBuffer.allocateDirect(16777216).order(ByteOrder.nativeOrder()).asIntBuffer();
    protected final int[] charWidth = new int[256];
    protected final byte[] glyphWidth = new byte[65536];
    protected final ResourceLocation locationFontTexture;
    private final int[] colorCode = new int[32];
    private final String[][] loadedTextures = new String[2][262144];
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    protected float posX;
    protected float posY;
    private boolean unicodeFlag;
    private boolean bidiFlag;
    private float red;
    private float blue;
    private float green;
    private float alpha;
    private int textColor;
    private boolean randomStyle;
    private boolean boldStyle;
    private boolean italicStyle;
    private boolean underlineStyle;
    private boolean strikethroughStyle;
    private boolean redBackoff;
    private boolean alphaBackoff;
    private boolean shadowRendering;
    private int shadowColor;
    private float hue;
    private float r;
    private float g = 1.0F;
    private float b = 1.0F;
    private float a = 0.5F;
    private boolean firstLine;
    private int textureIndex;

    public FontRenderer(ResourceLocation location, int textureIndex) {
        this.locationFontTexture = location;
        this.textureIndex = textureIndex;
        this.unicodeFlag = true;

        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i >> 0 & 1) * 170 + j;
            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }

        this.readGlyphSizes();
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
    }

    public static String getFormatFromString(String text) {
        StringBuilder s = new StringBuilder();
        int i = -1;
        int j = text.length();

        while ((i = text.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = text.charAt(i + 1);
                if (isFormatColor(c0)) {
                    s = new StringBuilder("§" + c0);
                } else if (isFormatSpecial(c0)) {
                    s.append("§").append(c0);
                }
            }
        }

        return s.toString();
    }

    public static int getA(int col) {
        return (col & -16777216) >>> 24;
    }

    public static int getR(int col) {
        return (col & 16711680) >> 16;
    }

    public static int getG(int col) {
        return (col & '\uff00') >> 8;
    }

    public static int getB(int col) {
        return col & 255;
    }

    public static int color(int r, int g, int b, int a) {
        return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
    }

    public static int clamp(float f) {
        return MathHelper.clamp((int) (f * 255.0F), 0, 255);
    }

    public static int multShade(int input, float perc) {
        if (!(perc >= 1.0F) && input != 0) {
            return perc <= 0.0F ? input & -16777216 : color(Math.round((float) getR(input) * perc), Math.round((float) getG(input) * perc), Math.round((float) getB(input) * perc), getA(input));
        } else {
            return input;
        }
    }

    private void readFontTexture() {
    }

    private void readGlyphSizes() {
        InputStream iresource = Empty.class.getResourceAsStream("/assets/kanade/textures/glyph_sizes.bin");

        try {
            iresource.read(this.glyphWidth);
        } catch (IOException var10) {
            throw new RuntimeException(var10);
        } finally {
            try {
                iresource.close();
            } catch (IOException ignored) {
            }

        }

    }

    private float renderChar(char ch, boolean italic) {
        if (ch == 160) {
            return 4.0F;
        } else {
            return ch == ' ' ? 4.0F : this.renderUnicodeChar(ch, italic);
        }
    }

    private String getUnicodePageLocation(int page) {
        if (UNICODE_PAGE_LOCATIONS[page] == null) {
            UNICODE_PAGE_LOCATIONS[page] = String.format("/assets/kanade/textures/unicode/unicode_page_%02x.png", page);
        }

        return UNICODE_PAGE_LOCATIONS[page];
    }

    private void loadGlyphTexture(int page) {
        this.bindTexDirect(this.getUnicodePageLocation(page));
    }

    protected float renderUnicodeChar(char ch, boolean italic) {
        if (this.redBackoff) {
            this.r -= 0.0625F;
        } else {
            this.r += 0.0625F;
        }

        if (this.alphaBackoff) {
            this.a -= 0.03125F;
        } else {
            this.a += 0.03125F;
        }

        if (this.r >= 1.0F) {
            this.r = 1.0F;
            this.redBackoff = true;
        } else if (this.r <= 0.0F) {
            this.r = 0.0F;
            this.redBackoff = false;
        }

        if (this.a >= 1.0F) {
            this.a = 1.0F;
            this.alphaBackoff = true;
        } else if (this.a <= 0.5F) {
            this.a = 0.5F;
            this.alphaBackoff = false;
        }

        this.setHueColor();
        int i = this.glyphWidth[ch] & 255;
        if (i == 0) {
            return 0.0F;
        } else {
            int j = ch / 256;
            this.loadGlyphTexture(j);
            int k = i >>> 4;
            int l = i & 15;
            float f = (float) k;
            float f1 = (float) (l + 1);
            float f2 = (float) (ch % 16 * 16) + f;
            float f3 = (float) ((ch & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = italic ? 1.0F : 0.0F;
            OpenGLHelper.nglBegin(5, GLOffsets.glBegin);
            OpenGLHelper.nglTexCoord2f(f2 / 256.0F, f3 / 256.0F, GLOffsets.glTexCoord2f);
            OpenGLHelper.nglVertex3f(this.posX + f5, this.posY, 0.0F, GLOffsets.glVertex3f);
            OpenGLHelper.nglTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F, GLOffsets.glTexCoord2f);
            OpenGLHelper.nglVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F, GLOffsets.glVertex3f);
            OpenGLHelper.nglTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F, GLOffsets.glTexCoord2f);
            OpenGLHelper.nglVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F, GLOffsets.glVertex3f);
            OpenGLHelper.nglTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F, GLOffsets.glTexCoord2f);
            OpenGLHelper.nglVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F, GLOffsets.glVertex3f);
            OpenGLHelper.nglEnd(GLOffsets.glEnd);
            return (f1 - f) / 2.0F + 1.0F;
        }
    }

    public int drawStringWithShadowIcely(String text, float x, float y, int color) {
        int length = this.drawString(text, x, y, color, true);
        this.firstLine = false;
        return length;
    }

    public int drawString(String text, int x, int y, int color) {
        return this.drawString(text, (float) x, (float) y, color, false);
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        this.enableAlpha();
        this.resetStyles();
        int i;
        if (dropShadow) {
            i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
            i = Math.max(i, this.renderString(text, x, y, color, false));
        } else {
            i = this.renderString(text, x, y, color, false);
        }

        return i;
    }

    private String bidiReorder(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException var3) {
            return text;
        }
    }

    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    private void renderStringAtPos(String text, boolean shadow) {
        this.shadowRendering = shadow;
        this.setColor(this.red, this.blue, this.green, this.alpha);

        for (int i = 0; i < text.length(); ++i) {
            char c0 = text.charAt(i);
            int i1;
            int j1;
            if (c0 == 167 && i + 1 < text.length()) {
                i1 = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(i + 1)).toLowerCase(Locale.ROOT).charAt(0));
                if (i1 < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    if (i1 < 0 || i1 > 15) {
                        i1 = 15;
                    }

                    if (shadow) {
                        i1 += 16;
                    }

                    j1 = this.colorCode[i1];
                    this.textColor = j1;
                    this.setColor((float) (j1 >> 16) / 255.0F, (float) (j1 >> 8 & 255) / 255.0F, (float) (j1 & 255) / 255.0F, this.alpha);
                } else if (i1 == 16) {
                    this.randomStyle = true;
                } else if (i1 == 17) {
                    this.boldStyle = true;
                } else if (i1 == 18) {
                    this.strikethroughStyle = true;
                } else if (i1 == 19) {
                    this.underlineStyle = true;
                } else if (i1 == 20) {
                    this.italicStyle = true;
                } else if (i1 == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    this.setColor(this.red, this.blue, this.green, this.alpha);
                }

                ++i;
            } else {
                i1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(c0);
                if (this.randomStyle && i1 != -1) {
                    j1 = this.getCharWidth(c0);

                    char c1;
                    do {
                        i1 = this.fontRandom.nextInt("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".length());
                        c1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".charAt(i1);
                    } while (j1 != this.getCharWidth(c1));

                    c0 = c1;
                }

                float f1 = i1 != -1 && !this.unicodeFlag ? 1.0F : 0.5F;
                boolean flag = (c0 == 0 || i1 == -1 || this.unicodeFlag) && shadow;
                if (flag) {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderChar(c0, this.italicStyle);
                if (flag) {
                    this.posX += f1;
                    this.posY += f1;
                }

                this.doDraw(f);
            }
        }

        this.shadowRendering = false;
    }

    protected void doDraw(float f) {
        this.posX += (float) ((int) f);
    }

    private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow) {
        if (this.bidiFlag) {
            int i = this.getStringWidth(this.bidiReorder(text));
            x = x + width - i;
        }

        return this.renderString(text, (float) x, (float) y, color, dropShadow);
    }

    private int renderString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        } else {
            if (this.bidiFlag) {
                text = this.bidiReorder(text);
            }

            if ((color & -67108864) == 0) {
                color |= -16777216;
            }

            if (dropShadow) {
                color = (color & 16579836) >> 2 | color & -16777216;
                if (this.shadowColor == 0) {
                    this.shadowColor = color;
                }
            }

            this.red = (float) (color >> 16 & 255) / 255.0F;
            this.blue = (float) (color >> 8 & 255) / 255.0F;
            this.green = (float) (color & 255) / 255.0F;
            this.alpha = (float) (color >> 24 & 255) / 255.0F;
            this.posX = x;
            this.posY = y;
            this.renderStringAtPos(text, dropShadow);
            return (int) this.posX;
        }
    }

    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        } else {
            int i = 0;
            boolean flag = false;

            for (int j = 0; j < text.length(); ++j) {
                char c0 = text.charAt(j);
                int k = this.getCharWidth(c0);
                if (k < 0 && j < text.length() - 1) {
                    ++j;
                    c0 = text.charAt(j);
                    if (c0 != 'l' && c0 != 'L') {
                        if (c0 == 'r' || c0 == 'R') {
                            flag = false;
                        }
                    } else {
                        flag = true;
                    }

                    k = 0;
                }

                i += k;
                if (flag && k > 0) {
                    ++i;
                }
            }

            return i;
        }
    }

    public int getCharWidth(char character) {
        if (character == 160) {
            return 4;
        } else if (character == 167) {
            return -1;
        } else if (character == ' ') {
            return 4;
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(character);
            if (character > 0 && i != -1 && !this.unicodeFlag) {
                return this.charWidth[i];
            } else if (this.glyphWidth[character] != 0) {
                int j = this.glyphWidth[character] & 255;
                int k = j >>> 4;
                int l = j & 15;
                ++l;
                return (l - k) / 2 + 1;
            } else {
                return 0;
            }
        }
    }

    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = reverse ? text.length() - 1 : 0;
        int k = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
            char c0 = text.charAt(l);
            int i1 = this.getCharWidth(c0);
            if (flag) {
                flag = false;
                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (i1 < 0) {
                flag = true;
            } else {
                i += i1;
                if (flag1) {
                    ++i;
                }
            }

            if (i > width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        this.resetStyles();
        this.textColor = textColor;
        str = this.trimStringNewline(str);
        this.renderSplitString(str, x, y, wrapWidth);
        this.firstLine = false;
    }

    private void renderSplitString(String str, int x, int y, int wrapWidth) {
        for (Iterator var6 = this.listFormattedStringToWidth(str, wrapWidth).iterator(); var6.hasNext(); y += this.FONT_HEIGHT) {
            String s = (String) var6.next();
            this.renderStringAligned(s, x, y, wrapWidth, this.textColor, false);
        }

    }

    public int getWordWrappedHeight(String str, int maxLength) {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(str, maxLength).size();
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        int i = this.sizeStringToWidth(str, wrapWidth);
        if (str.length() <= i) {
            return str;
        } else {
            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == ' ' || c0 == '\n';
            String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    private int sizeStringToWidth(String str, int wrapWidth) {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k) {
            char c0 = str.charAt(k);
            switch (c0) {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += this.getCharWidth(c0);
                    if (flag) {
                        ++j;
                    }
                    break;
                case '§':
                    if (k < i - 1) {
                        ++k;
                        char c1 = str.charAt(k);
                        if (c1 != 'l' && c1 != 'L') {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }

            if (c0 == '\n') {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth) {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }

    public FontRenderer refresh() {
        this.firstLine = true;
        return this;
    }

    protected void setColor(float r, float g, float b, float a) {
        this.setHueColor();
    }

    private void setHueColor() {
        if (this.shadowRendering) {
            OpenGLHelper.nglColor4f((float) (this.shadowColor >> 16 & 255) / 255.0F, (float) (this.shadowColor >> 8 & 255) / 255.0F, (float) (this.shadowColor & 255) / 255.0F, (float) (this.shadowColor >> 24 & 255) / 255.0F, GLOffsets.glColor4f);
        } else {
            OpenGLHelper.nglColor4f(this.r, this.g, this.b, this.a, GLOffsets.glColor4f);
        }

    }

    protected void enableAlpha() {
        OpenGLHelper.nglEnable(3008, GLOffsets.glEnable);
    }

    public void bindTexDirect(String location) {
        try {
            for (String[] loadedTexture : this.loadedTextures) {
                if (location.equals(loadedTexture[0])) {
                    OpenGLHelper.nglBindTexture(3553, Integer.parseInt(loadedTexture[1]), GLOffsets.glBindTexture);
                    return;
                }
            }
        } catch (NumberFormatException ignored) {
        }

        InputStream imageStream = Empty.class.getResourceAsStream(location);

        try {
            ImageInputStream stream = ImageIO.createImageInputStream(imageStream);
            Iterator iter = ImageIO.getImageReaders(stream);
            ImageReader reader = (ImageReader) iter.next();
            ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput(stream, true, true);
            BufferedImage bufferedimage = reader.read(0, param);

            try {
                reader.dispose();
                stream.close();
            } catch (IOException var18) {
            }

            IntBuffer textures = OpenGLHelper.getBufferInt(GLOffsets.caps);
            OpenGLHelper.nglGenTextures(1, MemoryUtil.getAddress0(textures) + (long) (textures.position() << 2), GLOffsets.glGenTextures);
            int textureId = textures.get(0);
            OpenGLHelper.nglBindTexture(3553, textureId, GLOffsets.glBindTexture);
            OpenGLHelper.nglTexParameteri(3553, 33085, 0, GLOffsets.glTexParameteri);
            OpenGLHelper.nglTexParameteri(3553, 33082, 0, GLOffsets.glTexParameteri);
            OpenGLHelper.nglTexParameteri(3553, 33083, 0, GLOffsets.glTexParameteri);
            OpenGLHelper.nglTexParameterf(3553, 34049, 0.0F, GLOffsets.glTexParameterf);
            OpenGLHelper.nglTexImage2D(3553, 0, 6408, bufferedimage.getWidth(), bufferedimage.getHeight(), 0, 32993, 33639, 0L, GLOffsets.glTexImage2D);
            OpenGLHelper.nglBindTexture(3553, textureId, GLOffsets.glBindTexture);
            int i = bufferedimage.getWidth();
            int j = bufferedimage.getHeight();
            int k = 4194304 / i;
            int[] aint = new int[k * i];
            OpenGLHelper.nglTexParameteri(3553, 10241, 9728, GLOffsets.glTexParameteri);
            OpenGLHelper.nglTexParameteri(3553, 10240, 9728, GLOffsets.glTexParameteri);
            OpenGLHelper.nglTexParameteri(3553, 10242, 10497, GLOffsets.glTexParameteri);
            OpenGLHelper.nglTexParameteri(3553, 10243, 10497, GLOffsets.glTexParameteri);

            for (int l = 0; l < i * j; l += i * k) {
                int i1 = l / i;
                int j1 = Math.min(k, j - i1);
                int k1 = i * j1;
                bufferedimage.getRGB(0, i1, i, j1, aint, 0, i);
                DATA_BUFFER.clear();
                DATA_BUFFER.put(aint, 0, k1);
                DATA_BUFFER.position(0).limit(k1);
                OpenGLHelper.nglTexSubImage2D(3553, 0, 0, i1, i, j1, 32993, 33639, MemoryUtil.getAddress0(DATA_BUFFER) + (long) (DATA_BUFFER.position() << 2), GLOffsets.glTexSubImage2D);
            }

            OpenGLHelper.nglBindTexture(3553, textureId, GLOffsets.glBindTexture);
            this.loadedTextures[this.textureIndex][0] = location;
            this.loadedTextures[this.textureIndex][1] = String.valueOf(textureId);
            ++textureId;
        } catch (NumberFormatException | IOException var19) {
        }

    }

    protected IResource getResource(ResourceLocation location) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(location);
    }

    public int getColorCode(char character) {
        int i = "0123456789abcdef".indexOf(character);
        return i >= 0 && i < this.colorCode.length ? this.colorCode[i] : -1;
    }
}
