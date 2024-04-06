package kanade.kill.util;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.item.KillItem;
import kanade.kill.reflection.EarlyMethods;
import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.util.memory.MemoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Random;

@SuppressWarnings({"unused", "raw"})
public class Util {
    public static boolean killing;

    public static boolean NoRemove(Object item) {
        return item == ModMain.kill_item || item == ModMain.death_item || (item instanceof ItemStack && NoRemove(((ItemStack) item).getITEM()));
    }

    public static void CoreDump() {
        Unsafe.instance.freeMemory(114514);
    }

    public static boolean BadGui(Gui gui) {
        if (gui == null) {
            return false;
        }
        String name = ReflectionUtil.getName(gui.getClass());
        String l = name.toLowerCase();
        return (Config.guiProtect && (ObjectUtil.ModClass(name) || gui.getClass().getProtectionDomain() == null || gui.getClass().getProtectionDomain().getCodeSource() == null)) || l.contains("death") || l.contains("over") || l.contains("die") || l.contains("dead");
    }

    public static void DisplayDeathGui() {

    }
    public static boolean shouldPostEvent(Event event) {
        if (Launch.client) {
            if (event instanceof GuiOpenEvent) {
                GuiOpenEvent guiOpenEvent = (GuiOpenEvent) event;
                GuiScreen gui = guiOpenEvent.getGui();
                return !(gui instanceof GuiGameOver) && !(gui instanceof GuiChat) && !(gui instanceof GuiIngameMenu) && !(gui instanceof GuiMainMenu) && !(gui instanceof GuiContainerCreative) && !(gui instanceof GuiInventory);
            } else if (event instanceof EntityViewRenderEvent) {
                EntityViewRenderEvent cameraSetup = (EntityViewRenderEvent) event;
                return !KillItem.inList(cameraSetup.getEntity());
            } else if (event instanceof RenderTooltipEvent.Pre) {
                RenderTooltipEvent.Pre tooltipEvent = (RenderTooltipEvent.Pre) event;
                if (tooltipEvent.getStack().getITEM() == ModMain.kill_item) {
                    for (int i = 0; i < 4; ++i) {
                        drawRandomString();
                    }
                }
            } else if (MemoryHelper.getClassName(event.getClass()).replace('/', '.').startsWith("net.minecraftforge.client.event.")) {
                return !KillItem.inList(Minecraft.getMinecraft());
            }
        }
        if (event instanceof PlayerEvent) {
            PlayerEvent playerEvent = (PlayerEvent) event;
            return !KillItem.inList(playerEvent.getEntityPlayer());
        } else if (event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent) {
            net.minecraftforge.fml.common.gameevent.PlayerEvent playerEvent = (net.minecraftforge.fml.common.gameevent.PlayerEvent) event;
            return !KillItem.inList(playerEvent.player);
        } else if (event instanceof EntityEvent) {
            EntityEvent entityEvent = (EntityEvent) event;
            return !KillItem.inList(entityEvent.getEntity());
        }
        return true;
    }

    public static long GLAddress(String name) {
        if (!Launch.client) {
            return 0;
        }
        ByteBuffer buffer = MemoryUtil.encodeASCII(name);
        long addr = MemoryUtil.getAddress(buffer);
        return (long) ReflectionUtil.invoke(EarlyMethods.getFunctionAddress, null, new Object[]{addr});
    }


    private static void drawRandomString() {
        if (!Launch.client) {
            return;
        }

        int i = new Random().nextInt(28);
        GL11.glPushMatrix();
        Random random = new Random();
        GL11.glColor4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0F);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        GL11.glPopMatrix();
        i = new Random().nextInt(28);
        GL11.glPushMatrix();
        random = new Random();
        GL11.glColor4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0F);
        GL11.glScalef(1.7F, 1.7F, 1.7F);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        GL11.glPopMatrix();
        i = new Random().nextInt(28);
        GL11.glPushMatrix();
        random = new Random();
        GL11.glColor4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0F);
        GL11.glScalef(1.3F, 1.3F, 1.3F);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        GL11.glPopMatrix();
        i = new Random().nextInt(28);
        GL11.glPushMatrix();
        random = new Random();
        GL11.glColor4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0F);
        GL11.glScalef(2.7F, 2.7F, 2.7F);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        randomDrawString(0, Minecraft.getMinecraft().FontRenderer, i);
        GL11.glPopMatrix();
    }

    public static void drawString(String text, int x, int y, int color, FontRenderer fontRenderer) {
        fontRenderer.drawStringWithShadow(text, (float) x, (float) y, color);
    }

    public static void randomDrawString(int color, FontRenderer fontRenderer, int id) {
        String text = null;
        switch (id) {
            case 0: {
                text = "Gott ist tot";
                break;
            }
            case 1: {
                text = Minecraft.getMinecraft().PLAYER.getName() + "   Why do █ y█u es██pe";
                break;
            }
            case 2: {
                text = "RESPOND RESPOND RESPOND";
                break;
            }
            case 4: {
                text = "origin";
                break;
            }
            case 5: {
                text = "殺して解して並べて揃えて晒してやんよ";
                break;
            }
            case 6: {
                text = "傑作だぜ";
                break;
            }
            case 7: {
                text = "零崎---開始します";
                break;
            }
            case 8: {
                text = "戯言だよな";
                break;
            }
            case 9: {
                text = "縁が《合りたら》、また会おら";
                break;
            }
            case 10: {
                text = "核融合炉にさ";
                break;
            }
            case 11: {
                text = "少しずつ死んでゆく世界";
                break;
            }
            case 12: {
                text = "幸せの背景は不幸";
                break;
            }
            case 13: {
                text = "善意の指針は悪意";
                break;
            }
            case 14: {
                text = "死の礎は生";
                break;
            }
            case 15: {
                text = "絆の支柱は欲望";
                break;
            }
            case 16: {
                text = "欲望の主柱は絆";
                break;
            }
            case 17: {
                text = "嘘の価値は真実";
                break;
            }
            case 18: {
                text = "死後の影響は生前";
                break;
            }
            case 19: {
                text = "日常の価値は非凡";
                break;
            }
            case 20: {
                text = "始まりの未来は終わり";
                break;
            }
            case 21: {
                text = "終わりの終わりは始まり";
                break;
            }
            case 22: {
                text = "記憶の形成は作為";
                break;
            }
            case 23: {
                text = "海を、見に行こうと思った。";
                break;
            }
            case 24: {
                text = "背中から一枚ずつ夢幻を剝がすように、歩いていく。";
                break;
            }
            case 25: {
                text = "祭り、に行っていたよね    知らない、女の子と一緒にいて";
                break;
            }
            case 26: {
                text = "この世で造花より綺麗な花は無いわ";
                break;
            }
            case 27: {
                text = "パパッパラパッパララッパッパ";
                break;
            }
        }
        int x = (int) (Math.random() * (double) Display.getWidth());
        int y = (int) (Math.random() * (double) Display.getHeight());
        drawString(text, x, y, color, fontRenderer);
    }

    public static void printClassNode(ClassNode cn) {
        Launch.LOGGER.info("Printing ClassNode.");
        Launch.LOGGER.info("name:" + cn.name);
        Launch.LOGGER.info("super:" + cn.superName);
        if (cn.signature != null) {
            Launch.LOGGER.info("sign:" + cn.signature);
        }
        if (!cn.fields.isEmpty()) {
            Launch.LOGGER.info("Printing fields.");
            for (FieldNode fn : cn.fields) {
                Launch.LOGGER.info("        field:" + fn.name);
                Launch.LOGGER.info("        desc:" + fn.desc);
                if (fn.signature != null) {
                    Launch.LOGGER.info("        sign:" + fn.signature);
                }
            }
        }
        if (!cn.methods.isEmpty()) {
            Launch.LOGGER.info("Printing methods.");
            for (MethodNode mn : cn.methods) {
                Launch.LOGGER.info("        method:" + mn.name);
                Launch.LOGGER.info("        desc:" + mn.desc);
                if (mn.signature != null) {
                    Launch.LOGGER.info("        sign:" + mn.signature);
                }
                if (!mn.localVariables.isEmpty()) {
                    Launch.LOGGER.info("        Printing local variables.");
                    for (LocalVariableNode ain : mn.localVariables) {
                        Launch.LOGGER.info("                index:" + ain.index);
                        Launch.LOGGER.info("                local:" + ain.name);
                        Launch.LOGGER.info("                desc" + ain.desc);
                    }
                }
            }
        }
    }

    private static boolean shouldRemove(IEventListener listener) {
        if (listener instanceof ASMEventHandler) {
            IEventListener internal = ((ASMEventHandler) listener).handler;
            try {
                Field field = internal.getClass().getField("instance");
                Object callback = field.get(internal);
                Launch.LOGGER.info("Remove:" + ((ASMEventHandler) listener).owner.getName());
                return NativeMethods.HaveTag(callback, 14514L);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return false;
            }
        }
        return false;
    }

    public static void printStackTrace() {
        Throwable t = new Throwable();
        t.fillInStackTrace();
        Launch.LOGGER.info("Stacktrace:", t);
    }
}