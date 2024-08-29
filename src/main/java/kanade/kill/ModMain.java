package kanade.kill;

import kanade.kill.entity.EntityBeaconBeam;
import kanade.kill.entity.Infector;
import kanade.kill.entity.Lain;
import kanade.kill.item.SuperModeToggle;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.KillEntity;
import kanade.kill.render.RenderBeaconBeam;
import kanade.kill.render.RenderLain;
import kanade.kill.thread.DisplayGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Mod(modid = "kanade", acceptedMinecraftVersions = "1.12.2", version = "4.7.4.3")
@Mod.EventBusSubscriber
public class ModMain {

    static {
        boolean AllowAgent = Boolean.parseBoolean(System.getProperty("AllowAgent"));

        try {
            PrintStream out = null;
            try {
                Object obj = System.out;
                Field field = FilterOutputStream.class.getDeclaredField("out");
                field.setAccessible(true);
                out = (PrintStream) field.get(obj);
            } catch (Throwable ignored) {
            }

            boolean flag = out != null;

            if (System.getProperty("KanadeMode") == null) {
                if (flag) {
                    out.println("os:" + System.getProperty("os.name"));
                } else {
                    System.out.println("os:" + System.getProperty("os.name"));
                }
                final boolean win = System.getProperty("os.name").startsWith("Windows");
                String jar = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("!/kanade/kill/Empty.class", "").replace("file:", "");
                if (win) {
                    jar = jar.substring(1);
                }
                try {
                    jar = URLDecoder.decode(jar, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException ignored) {
                }

                System.out.println("Restarting game.");
                StringBuilder LAUNCH = new StringBuilder();
                String args = System.getProperty("sun.java.command").replace("net.minecraft.launchwrapper.Launch", "kanade.kill.Launch");
                if (out != null) {
                    out.println("Arguments:");
                    out.println(args);
                } else {
                    System.out.println("Arguments:");
                    System.out.println(args);
                }
                String JAVA = System.getProperty("java.home");
                System.out.println("java.home:" + JAVA);
                if (JAVA.endsWith("jre")) {
                    String JavaHome = JAVA.substring(0, JAVA.length() - 3) + "bin" + File.separator + "java";
                    if (win) {
                        JavaHome = JavaHome + ".exe";
                    }
                    JavaHome = "\"" + JavaHome + "\" ";
                    LAUNCH.insert(0, JavaHome);
                } else {
                    String tmp = JAVA + File.separator + "bin" + File.separator + "java";
                    if (win) {
                        tmp = tmp + ".exe";
                    }
                    tmp = "\"" + tmp + "\" ";
                    LAUNCH.insert(0, tmp);
                }

                {
                    String filename = win ? "KanadeAgent.dll" : "KanadeAgent.so";
                    try (InputStream is = Empty.class.getResourceAsStream("/" + filename)) {
                        assert is != null;
                        final ByteArrayOutputStream output = new ByteArrayOutputStream();
                        final byte[] buffer = new byte[8024];
                        int n;
                        while (-1 != (n = is.read(buffer))) {
                            output.write(buffer, 0, n);
                        }
                        byte[] bytes = output.toByteArray();
                        File file = new File(filename);
                        if (file.exists()) {
                            Files.delete(file.toPath());
                        }
                        Files.write(file.toPath(), bytes);
                    }
                    File file = new File("KanadeAgent" + (win ? ".dll" : ".so"));
                    String path = file.getAbsolutePath();
                    LAUNCH.append("\"-agentpath:").append(path).append("\" ");
                }


                boolean verify_flag = false;

                for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if ((!AllowAgent && s.contains("-javaagent:")) || s.contains("-agentpath:") || s.contains("-agentlib:") || s.contains("-D")) {
                        continue;
                    }
                    if (s.contains("-Xverify:none")) {
                        verify_flag = true;
                    }
                    if (s.contains("=")) {
                        LAUNCH.append('"');
                        LAUNCH.append(s);
                        LAUNCH.append('"');
                    } else LAUNCH.append(s);
                    LAUNCH.append(' ');
                }

                if (!verify_flag) {
                    LAUNCH.append("-Xverify:none ");
                }

                AtomicReference<String> classpath = new AtomicReference<>();
                ManagementFactory.getRuntimeMXBean().getSystemProperties().forEach((s, v) -> {
                    if (!s.equals("java.class.path")) {
                        if (!s.equals("sun.boot.class.path") && !s.equals("java.vm.vendor") && !s.equals("sun.arch.data.model") && !s.equals("java.vendor.url") && !s.equals("user.timezone") && !s.equals("os.name") && !s.equals("java.vm.specification.version") && !s.equals("user.country") && !s.equals("sun.java.launcher") && !s.equals("sun.boot.library.path") && !s.equals("line.separator") && !s.equals("java.vm.specification.vendor") && !s.equals("java.home") && !s.equals("java.specification.name") && !s.equals("java.awt.graphicsenv") && !s.equals("java.rmi.server.useCodebaseOnly") && !s.equals("com.sun.jndi.rmi.object.trustURLCodebase") && !s.equals("com.sun.jndi.cosnaming.object.trustURLCodebase") && !s.equals("sun.desktop") && !s.equals("awt.toolkit") && !s.equals("java.specification.version") && !s.equals("sun.cpu.isalist") && !s.equals("sun.java.command") && !s.equals("sun.cpu.endian") && !s.equals("user.home") && !s.equals("user.language") && !s.equals("sun.management.compiler") && !s.equals("user.variant") && !s.equals("java.vm.version") && !s.equals("java.ext.dirs") && !s.equals("sun.io.unicode.encoding") && !s.equals("java.class.version") && !s.equals("user.script") && !s.equals("java.runtime.version") && !s.equals("file.separator") && !s.equals("java.specification.vendor") && !s.equals("java.net.preferIPv4Stack") && !s.equals("user.name") && !s.equals("path.separator") && !s.equals("os.version") && !s.equals("java.endorsed.dirs") && !s.equals("java.runtime.name") && !s.equals("java.vm.name") && !s.equals("java.vendor.url.bug") && !s.equals("java.io.tmpdir") && !s.equals("java.version") && !s.equals("user.dir") && !s.equals("os.arch") && !s.equals("java.vm.specification.name") && !s.equals("java.awt.printerjob") && !s.equals("sun.os.patch.level") && !s.equals("java.vm.info") && !s.equals("java.vendor")) {
                            LAUNCH.append("\"-D").append(s).append("=").append(v).append("\" ");
                        }
                    } else {
                        classpath.set(v);
                    }
                });
                LAUNCH.append("\"-DKanadeMode=true\" ");
                LAUNCH.append("-cp \"").append(classpath).append(win ? ";" : ":").append(jar).append("\" ").append(args);

                String str = LAUNCH.toString();
                str = str.replace("C:\\Users\\YHF\\AppData\\Roaming\\PCL\\JavaWrapper.jar", "");

                System.out.println(str);

                if (!win) {
                    ProcessBuilder process = new ProcessBuilder("/bin/sh", "-c", str);
                    process.redirectErrorStream(true);
                    Process mc = process.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(mc.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (flag) {
                            out.println(line);
                        } else {
                            System.out.println(line);
                        }
                    }
                } else {
                    /*File file = new File("relauncher.bat");
                    if (file.exists()) {
                        Files.delete(file.toPath());
                        Files.createFile(file.toPath());
                    }
                    Files.write(file.toPath(),str.getBytes(StandardCharsets.UTF_8));
                    ProcessBuilder process = new ProcessBuilder("cmd /c start \"\" relauncher.bat");
                    process.redirectErrorStream(true);
                    Process mc = process.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(mc.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (flag) {
                            out.println(line);
                        } else {
                            System.out.println(line);
                        }
                    }

                     */
                    ProcessBuilder process = new ProcessBuilder("cmd /c start \"\"" + str);
                    process.redirectErrorStream(true);
                    Process mc = process.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(mc.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (flag) {
                            out.println(line);
                        } else {
                            System.out.println(line);
                        }
                    }
                }

                try {
                    if (!win) {
                        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
                        Runtime.getRuntime().exec("/bin/sh", new String[]{"kill -9 " + pid});
                    }
                } catch (Throwable ignored) {
                }

                boolean f = false;

                try {
                    Class<?> clazz = Class.forName("java.lang.Shutdown");
                    Method method = clazz.getDeclaredMethod("halt0", int.class);
                    method.setAccessible(true);
                    method.invoke(null, 0);
                } catch (Throwable t) {
                    if (flag) {
                        out.println(t);
                    }
                    f = true;
                }

                if (f) {
                    Runtime.getRuntime().exit(0);
                }
            } else {
                System.out.println("Kanade loaded.");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        /*ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "net/minecraft/client/MinecraftKanade", null, "java/lang/Object", null);


        classWriter.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC | ACC_FINAL | ACC_STATIC);

        {
            fieldVisitor = classWriter.visitField(ACC_FINAL | ACC_STATIC | ACC_SYNTHETIC, "$assertionsDisabled", "Z", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(14, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/MinecraftKanade;", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, "lambda$static$0", "(Ljava/lang/StringBuilder;Ljava/util/concurrent/atomic/AtomicReference;Ljava/lang/String;Ljava/lang/String;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(123, label0);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.class.path");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            Label label1 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label1);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(124, label2);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.boot.class.path");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            Label label3 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vm.vendor");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.arch.data.model");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vendor.url");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.timezone");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("os.name");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vm.specification.version");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.country");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.java.launcher");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.boot.library.path");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("line.separator");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vm.specification.vendor");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.home");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.specification.name");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.awt.graphicsenv");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.rmi.server.useCodebaseOnly");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("com.sun.jndi.rmi.object.trustURLCodebase");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("com.sun.jndi.cosnaming.object.trustURLCodebase");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.desktop");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("awt.toolkit");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.specification.version");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.cpu.isalist");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.java.command");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.cpu.endian");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.home");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.language");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.management.compiler");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.variant");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vm.version");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.ext.dirs");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.io.unicode.encoding");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.class.version");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.script");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.runtime.version");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("file.separator");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.specification.vendor");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.net.preferIPv4Stack");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.name");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("path.separator");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("os.version");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.endorsed.dirs");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.runtime.name");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vm.name");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vendor.url.bug");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.io.tmpdir");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.version");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("user.dir");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("os.arch");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vm.specification.name");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.awt.printerjob");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("sun.os.patch.level");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vm.info");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn("java.vendor");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label3);
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLineNumber(125, label4);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitLdcInsn("\"-D");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("=");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("\" ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitJumpInsn(GOTO, label3);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(128, label1);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicReference", "set", "(Ljava/lang/Object;)V", false);
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(130, label3);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitInsn(RETURN);
            Label label5 = new Label();
            methodVisitor.visitLabel(label5);
            methodVisitor.visitLocalVariable("LAUNCH", "Ljava/lang/StringBuilder;", null, label0, label5, 0);
            methodVisitor.visitLocalVariable("classpath", "Ljava/util/concurrent/atomic/AtomicReference;", null, label0, label5, 1);
            methodVisitor.visitLocalVariable("s", "Ljava/lang/String;", null, label0, label5, 2);
            methodVisitor.visitLocalVariable("v", "Ljava/lang/String;", null, label0, label5, 3);
            methodVisitor.visitMaxs(2, 4);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            Label label1 = new Label();
            Label label2 = new Label();
            methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/Throwable");
            Label label3 = new Label();
            Label label4 = new Label();
            Label label5 = new Label();
            methodVisitor.visitTryCatchBlock(label3, label4, label5, "java/io/UnsupportedEncodingException");
            Label label6 = new Label();
            Label label7 = new Label();
            Label label8 = new Label();
            methodVisitor.visitTryCatchBlock(label6, label7, label8, "java/lang/Throwable");
            Label label9 = new Label();
            Label label10 = new Label();
            Label label11 = new Label();
            methodVisitor.visitTryCatchBlock(label9, label10, label11, "java/lang/Throwable");
            Label label12 = new Label();
            methodVisitor.visitTryCatchBlock(label9, label10, label12, null);
            Label label13 = new Label();
            Label label14 = new Label();
            Label label15 = new Label();
            methodVisitor.visitTryCatchBlock(label13, label14, label15, "java/lang/Throwable");
            Label label16 = new Label();
            methodVisitor.visitTryCatchBlock(label11, label16, label12, null);
            Label label17 = new Label();
            Label label18 = new Label();
            Label label19 = new Label();
            methodVisitor.visitTryCatchBlock(label17, label18, label19, "java/lang/Throwable");
            Label label20 = new Label();
            Label label21 = new Label();
            Label label22 = new Label();
            methodVisitor.visitTryCatchBlock(label20, label21, label22, "java/lang/Throwable");
            Label label23 = new Label();
            Label label24 = new Label();
            Label label25 = new Label();
            methodVisitor.visitTryCatchBlock(label23, label24, label25, "java/lang/Throwable");
            Label label26 = new Label();
            methodVisitor.visitLabel(label26);
            methodVisitor.visitLineNumber(14, label26);
            methodVisitor.visitLdcInsn(Type.getType("Lnet/minecraft/client/MinecraftKanade;"));
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "desiredAssertionStatus", "()Z", false);
            Label label27 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label27);
            methodVisitor.visitInsn(ICONST_1);
            Label label28 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label28);
            methodVisitor.visitLabel(label27);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitLabel(label28);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Opcodes.INTEGER});
            methodVisitor.visitFieldInsn(PUTSTATIC, "net/minecraft/client/MinecraftKanade", "$assertionsDisabled", "Z");
            Label label29 = new Label();
            methodVisitor.visitLabel(label29);
            methodVisitor.visitLineNumber(17, label29);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn(Type.getType("Lnet/minecraftforge/fml/common/asm/transformers/deobf/FMLDeobfuscatingRemapper;"));
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label30 = new Label();
            methodVisitor.visitLabel(label30);
            methodVisitor.visitLineNumber(19, label30);
            methodVisitor.visitLdcInsn("AllowAgent");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
            methodVisitor.visitVarInsn(ISTORE, 0);
            methodVisitor.visitLabel(label23);
            methodVisitor.visitLineNumber(22, label23);
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitVarInsn(ASTORE, 1);
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(24, label0);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitVarInsn(ASTORE, 2);
            Label label31 = new Label();
            methodVisitor.visitLabel(label31);
            methodVisitor.visitLineNumber(25, label31);
            methodVisitor.visitLdcInsn(Type.getType("Ljava/io/FilterOutputStream;"));
            methodVisitor.visitLdcInsn("out");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
            methodVisitor.visitVarInsn(ASTORE, 3);
            Label label32 = new Label();
            methodVisitor.visitLabel(label32);
            methodVisitor.visitLineNumber(26, label32);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
            Label label33 = new Label();
            methodVisitor.visitLabel(label33);
            methodVisitor.visitLineNumber(27, label33);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(CHECKCAST, "java/io/PrintStream");
            methodVisitor.visitVarInsn(ASTORE, 1);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(29, label1);
            Label label34 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label34);
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(28, label2);
            methodVisitor.visitFrame(Opcodes.F_FULL, 2, new Object[]{Opcodes.INTEGER, "java/io/PrintStream"}, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 2);
            methodVisitor.visitLabel(label34);
            methodVisitor.visitLineNumber(31, label34);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 1);
            Label label35 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label35);
            methodVisitor.visitInsn(ICONST_1);
            Label label36 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label36);
            methodVisitor.visitLabel(label35);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitLabel(label36);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Opcodes.INTEGER});
            methodVisitor.visitVarInsn(ISTORE, 2);
            Label label37 = new Label();
            methodVisitor.visitLabel(label37);
            methodVisitor.visitLineNumber(33, label37);
            methodVisitor.visitLdcInsn("KanadeMode");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            Label label38 = new Label();
            methodVisitor.visitJumpInsn(IFNONNULL, label38);
            Label label39 = new Label();
            methodVisitor.visitLabel(label39);
            methodVisitor.visitLineNumber(34, label39);
            methodVisitor.visitVarInsn(ILOAD, 2);
            Label label40 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label40);
            Label label41 = new Label();
            methodVisitor.visitLabel(label41);
            methodVisitor.visitLineNumber(35, label41);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("os:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("os.name");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label42 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label42);
            methodVisitor.visitLabel(label40);
            methodVisitor.visitLineNumber(37, label40);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{Opcodes.INTEGER}, 0, null);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("os:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("os.name");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitLabel(label42);
            methodVisitor.visitLineNumber(39, label42);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitLdcInsn("os.name");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            methodVisitor.visitLdcInsn("Windows");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
            methodVisitor.visitVarInsn(ISTORE, 3);
            Label label43 = new Label();
            methodVisitor.visitLabel(label43);
            methodVisitor.visitLineNumber(40, label43);
            methodVisitor.visitLdcInsn(Type.getType("Lkanade/kill/Empty;"));
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getProtectionDomain", "()Ljava/security/ProtectionDomain;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/security/ProtectionDomain", "getCodeSource", "()Ljava/security/CodeSource;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/security/CodeSource", "getLocation", "()Ljava/net/URL;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/net/URL", "getPath", "()Ljava/lang/String;", false);
            methodVisitor.visitLdcInsn("!/kanade/kill/Empty.class");
            methodVisitor.visitLdcInsn("");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", false);
            methodVisitor.visitLdcInsn("file:");
            methodVisitor.visitLdcInsn("");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 4);
            Label label44 = new Label();
            methodVisitor.visitLabel(label44);
            methodVisitor.visitLineNumber(41, label44);
            methodVisitor.visitVarInsn(ILOAD, 3);
            methodVisitor.visitJumpInsn(IFEQ, label3);
            Label label45 = new Label();
            methodVisitor.visitLabel(label45);
            methodVisitor.visitLineNumber(42, label45);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 4);
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(45, label3);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.INTEGER, "java/lang/String"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/nio/charset/Charset", "name", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/net/URLDecoder", "decode", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 4);
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLineNumber(47, label4);
            Label label46 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label46);
            methodVisitor.visitLabel(label5);
            methodVisitor.visitLineNumber(46, label5);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/io/UnsupportedEncodingException"});
            methodVisitor.visitVarInsn(ASTORE, 5);
            methodVisitor.visitLabel(label46);
            methodVisitor.visitLineNumber(49, label46);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("Restarting game.");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label47 = new Label();
            methodVisitor.visitLabel(label47);
            methodVisitor.visitLineNumber(50, label47);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ASTORE, 5);
            Label label48 = new Label();
            methodVisitor.visitLabel(label48);
            methodVisitor.visitLineNumber(51, label48);
            methodVisitor.visitLdcInsn("sun.java.command");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            methodVisitor.visitLdcInsn("net.minecraft.launchwrapper.Launch");
            methodVisitor.visitLdcInsn("kanade.kill.Launch");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 6);
            Label label49 = new Label();
            methodVisitor.visitLabel(label49);
            methodVisitor.visitLineNumber(52, label49);
            methodVisitor.visitVarInsn(ALOAD, 1);
            Label label50 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label50);
            Label label51 = new Label();
            methodVisitor.visitLabel(label51);
            methodVisitor.visitLineNumber(53, label51);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitLdcInsn("Arguments:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label52 = new Label();
            methodVisitor.visitLabel(label52);
            methodVisitor.visitLineNumber(54, label52);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label53 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label53);
            methodVisitor.visitLabel(label50);
            methodVisitor.visitLineNumber(56, label50);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/lang/StringBuilder", "java/lang/String"}, 0, null);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("Arguments:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label54 = new Label();
            methodVisitor.visitLabel(label54);
            methodVisitor.visitLineNumber(57, label54);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitLabel(label53);
            methodVisitor.visitLineNumber(59, label53);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitLdcInsn("java.home");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 7);
            Label label55 = new Label();
            methodVisitor.visitLabel(label55);
            methodVisitor.visitLineNumber(60, label55);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("java.home:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label56 = new Label();
            methodVisitor.visitLabel(label56);
            methodVisitor.visitLineNumber(61, label56);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitLdcInsn("jre");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z", false);
            Label label57 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label57);
            Label label58 = new Label();
            methodVisitor.visitLabel(label58);
            methodVisitor.visitLineNumber(62, label58);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
            methodVisitor.visitInsn(ICONST_3);
            methodVisitor.visitInsn(ISUB);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("bin");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/io/File", "separator", "Ljava/lang/String;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("java");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 8);
            Label label59 = new Label();
            methodVisitor.visitLabel(label59);
            methodVisitor.visitLineNumber(63, label59);
            methodVisitor.visitVarInsn(ILOAD, 3);
            Label label60 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label60);
            Label label61 = new Label();
            methodVisitor.visitLabel(label61);
            methodVisitor.visitLineNumber(64, label61);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn(".exe");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 8);
            methodVisitor.visitLabel(label60);
            methodVisitor.visitLineNumber(66, label60);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/lang/String", "java/lang/String"}, 0, null);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("\"");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("\" ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 8);
            Label label62 = new Label();
            methodVisitor.visitLabel(label62);
            methodVisitor.visitLineNumber(67, label62);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "insert", "(ILjava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label63 = new Label();
            methodVisitor.visitLabel(label63);
            methodVisitor.visitLineNumber(68, label63);
            Label label64 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label64);
            methodVisitor.visitLabel(label57);
            methodVisitor.visitLineNumber(69, label57);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/io/File", "separator", "Ljava/lang/String;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("bin");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/io/File", "separator", "Ljava/lang/String;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("java");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 8);
            Label label65 = new Label();
            methodVisitor.visitLabel(label65);
            methodVisitor.visitLineNumber(70, label65);
            methodVisitor.visitVarInsn(ILOAD, 3);
            Label label66 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label66);
            Label label67 = new Label();
            methodVisitor.visitLabel(label67);
            methodVisitor.visitLineNumber(71, label67);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn(".exe");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 8);
            methodVisitor.visitLabel(label66);
            methodVisitor.visitLineNumber(73, label66);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("\"");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("\" ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 8);
            Label label68 = new Label();
            methodVisitor.visitLabel(label68);
            methodVisitor.visitLineNumber(74, label68);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "insert", "(ILjava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitLabel(label64);
            methodVisitor.visitLineNumber(78, label64);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            methodVisitor.visitVarInsn(ILOAD, 3);
            Label label69 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label69);
            methodVisitor.visitLdcInsn("KanadeAgent.dll");
            Label label70 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label70);
            methodVisitor.visitLabel(label69);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitLdcInsn("KanadeAgent.so");
            methodVisitor.visitLabel(label70);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/String"});
            methodVisitor.visitVarInsn(ASTORE, 8);
            Label label71 = new Label();
            methodVisitor.visitLabel(label71);
            methodVisitor.visitLineNumber(79, label71);
            methodVisitor.visitLdcInsn(Type.getType("Lkanade/kill/Empty;"));
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("/");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
            methodVisitor.visitVarInsn(ASTORE, 9);
            Label label72 = new Label();
            methodVisitor.visitLabel(label72);
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitVarInsn(ASTORE, 10);
            methodVisitor.visitLabel(label9);
            methodVisitor.visitLineNumber(80, label9);
            methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/client/MinecraftKanade", "$assertionsDisabled", "Z");
            Label label73 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label73);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitJumpInsn(IFNONNULL, label73);
            methodVisitor.visitTypeInsn(NEW, "java/lang/AssertionError");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/AssertionError", "<init>", "()V", false);
            methodVisitor.visitInsn(ATHROW);
            methodVisitor.visitLabel(label73);
            methodVisitor.visitLineNumber(81, label73);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 3, new Object[]{"java/lang/String", "java/io/InputStream", "java/lang/Throwable"}, 0, null);
            methodVisitor.visitTypeInsn(NEW, "java/io/ByteArrayOutputStream");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ASTORE, 11);
            Label label74 = new Label();
            methodVisitor.visitLabel(label74);
            methodVisitor.visitLineNumber(82, label74);
            methodVisitor.visitIntInsn(SIPUSH, 8024);
            methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
            methodVisitor.visitVarInsn(ASTORE, 12);
            Label label75 = new Label();
            methodVisitor.visitLabel(label75);
            methodVisitor.visitLineNumber(84, label75);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/io/ByteArrayOutputStream", "[B"}, 0, null);
            methodVisitor.visitInsn(ICONST_M1);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitVarInsn(ALOAD, 12);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "read", "([B)I", false);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ISTORE, 13);
            Label label76 = new Label();
            methodVisitor.visitLabel(label76);
            Label label77 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPEQ, label77);
            Label label78 = new Label();
            methodVisitor.visitLabel(label78);
            methodVisitor.visitLineNumber(85, label78);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitVarInsn(ALOAD, 12);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ILOAD, 13);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "write", "([BII)V", false);
            methodVisitor.visitJumpInsn(GOTO, label75);
            methodVisitor.visitLabel(label77);
            methodVisitor.visitLineNumber(87, label77);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{Opcodes.INTEGER}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "toByteArray", "()[B", false);
            methodVisitor.visitVarInsn(ASTORE, 14);
            Label label79 = new Label();
            methodVisitor.visitLabel(label79);
            methodVisitor.visitLineNumber(88, label79);
            methodVisitor.visitTypeInsn(NEW, "java/io/File");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 15);
            Label label80 = new Label();
            methodVisitor.visitLabel(label80);
            methodVisitor.visitLineNumber(89, label80);
            methodVisitor.visitVarInsn(ALOAD, 15);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "exists", "()Z", false);
            Label label81 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label81);
            Label label82 = new Label();
            methodVisitor.visitLabel(label82);
            methodVisitor.visitLineNumber(90, label82);
            methodVisitor.visitVarInsn(ALOAD, 15);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "toPath", "()Ljava/nio/file/Path;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/nio/file/Files", "delete", "(Ljava/nio/file/Path;)V", false);
            methodVisitor.visitLabel(label81);
            methodVisitor.visitLineNumber(92, label81);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"[B", "java/io/File"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 15);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "toPath", "()Ljava/nio/file/Path;", false);
            methodVisitor.visitVarInsn(ALOAD, 14);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/nio/file/OpenOption");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/nio/file/Files", "write", "(Ljava/nio/file/Path;[B[Ljava/nio/file/OpenOption;)Ljava/nio/file/Path;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitLabel(label10);
            methodVisitor.visitLineNumber(93, label10);
            methodVisitor.visitVarInsn(ALOAD, 9);
            Label label83 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label83);
            methodVisitor.visitVarInsn(ALOAD, 10);
            Label label84 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label84);
            methodVisitor.visitLabel(label6);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "close", "()V", false);
            methodVisitor.visitLabel(label7);
            methodVisitor.visitJumpInsn(GOTO, label83);
            methodVisitor.visitLabel(label8);
            methodVisitor.visitFrame(Opcodes.F_FULL, 11, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", "java/lang/String", "java/io/InputStream", "java/lang/Throwable"}, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 11);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "addSuppressed", "(Ljava/lang/Throwable;)V", false);
            methodVisitor.visitJumpInsn(GOTO, label83);
            methodVisitor.visitLabel(label84);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "close", "()V", false);
            methodVisitor.visitJumpInsn(GOTO, label83);
            methodVisitor.visitLabel(label11);
            methodVisitor.visitLineNumber(79, label11);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 11);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitVarInsn(ASTORE, 10);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitInsn(ATHROW);
            methodVisitor.visitLabel(label12);
            methodVisitor.visitLineNumber(93, label12);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 16);
            methodVisitor.visitLabel(label16);
            methodVisitor.visitVarInsn(ALOAD, 9);
            Label label85 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label85);
            methodVisitor.visitVarInsn(ALOAD, 10);
            Label label86 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label86);
            methodVisitor.visitLabel(label13);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "close", "()V", false);
            methodVisitor.visitLabel(label14);
            methodVisitor.visitJumpInsn(GOTO, label85);
            methodVisitor.visitLabel(label15);
            methodVisitor.visitFrame(Opcodes.F_FULL, 17, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", "java/lang/String", "java/io/InputStream", "java/lang/Throwable", Opcodes.TOP, Opcodes.TOP, Opcodes.TOP, Opcodes.TOP, Opcodes.TOP, "java/lang/Throwable"}, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 17);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitVarInsn(ALOAD, 17);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "addSuppressed", "(Ljava/lang/Throwable;)V", false);
            methodVisitor.visitJumpInsn(GOTO, label85);
            methodVisitor.visitLabel(label86);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "close", "()V", false);
            methodVisitor.visitLabel(label85);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 16);
            methodVisitor.visitInsn(ATHROW);
            methodVisitor.visitLabel(label83);
            methodVisitor.visitLineNumber(94, label83);
            methodVisitor.visitFrame(Opcodes.F_FULL, 9, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", "java/lang/String"}, 0, new Object[]{});
            methodVisitor.visitTypeInsn(NEW, "java/io/File");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("KanadeAgent");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ILOAD, 3);
            Label label87 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label87);
            methodVisitor.visitLdcInsn(".dll");
            Label label88 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label88);
            methodVisitor.visitLabel(label87);
            methodVisitor.visitFrame(Opcodes.F_FULL, 9, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", "java/lang/String"}, 3, new Object[]{label83, label83, "java/lang/StringBuilder"});
            methodVisitor.visitLdcInsn(".so");
            methodVisitor.visitLabel(label88);
            methodVisitor.visitFrame(Opcodes.F_FULL, 9, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", "java/lang/String"}, 4, new Object[]{label83, label83, "java/lang/StringBuilder", "java/lang/String"});
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 9);
            Label label89 = new Label();
            methodVisitor.visitLabel(label89);
            methodVisitor.visitLineNumber(95, label89);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 10);
            Label label90 = new Label();
            methodVisitor.visitLabel(label90);
            methodVisitor.visitLineNumber(96, label90);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitLdcInsn("\"-agentpath:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("\" ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label91 = new Label();
            methodVisitor.visitLabel(label91);
            methodVisitor.visitLineNumber(100, label91);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 8);
            Label label92 = new Label();
            methodVisitor.visitLabel(label92);
            methodVisitor.visitLineNumber(102, label92);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/management/ManagementFactory", "getRuntimeMXBean", "()Ljava/lang/management/RuntimeMXBean;", false);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/lang/management/RuntimeMXBean", "getInputArguments", "()Ljava/util/List;", true);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
            methodVisitor.visitVarInsn(ASTORE, 9);
            Label label93 = new Label();
            methodVisitor.visitLabel(label93);
            methodVisitor.visitFrame(Opcodes.F_FULL, 10, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", Opcodes.INTEGER, "java/util/Iterator"}, 0, new Object[]{});
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
            Label label94 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label94);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
            methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
            methodVisitor.visitVarInsn(ASTORE, 10);
            Label label95 = new Label();
            methodVisitor.visitLabel(label95);
            methodVisitor.visitLineNumber(103, label95);
            methodVisitor.visitVarInsn(ILOAD, 0);
            Label label96 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label96);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitLdcInsn("-javaagent:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label93);
            methodVisitor.visitLabel(label96);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitLdcInsn("-agentpath:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label93);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitLdcInsn("-agentlib:");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
            methodVisitor.visitJumpInsn(IFNE, label93);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitLdcInsn("-D");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
            Label label97 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label97);
            Label label98 = new Label();
            methodVisitor.visitLabel(label98);
            methodVisitor.visitLineNumber(104, label98);
            methodVisitor.visitJumpInsn(GOTO, label93);
            methodVisitor.visitLabel(label97);
            methodVisitor.visitLineNumber(106, label97);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitLdcInsn("-Xverify:none");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
            Label label99 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label99);
            Label label100 = new Label();
            methodVisitor.visitLabel(label100);
            methodVisitor.visitLineNumber(107, label100);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitVarInsn(ISTORE, 8);
            methodVisitor.visitLabel(label99);
            methodVisitor.visitLineNumber(109, label99);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitLdcInsn("=");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
            Label label101 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label101);
            Label label102 = new Label();
            methodVisitor.visitLabel(label102);
            methodVisitor.visitLineNumber(110, label102);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitIntInsn(BIPUSH, 34);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label103 = new Label();
            methodVisitor.visitLabel(label103);
            methodVisitor.visitLineNumber(111, label103);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label104 = new Label();
            methodVisitor.visitLabel(label104);
            methodVisitor.visitLineNumber(112, label104);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitIntInsn(BIPUSH, 34);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label105 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label105);
            methodVisitor.visitLabel(label101);
            methodVisitor.visitLineNumber(113, label101);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitLabel(label105);
            methodVisitor.visitLineNumber(114, label105);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitIntInsn(BIPUSH, 32);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label106 = new Label();
            methodVisitor.visitLabel(label106);
            methodVisitor.visitLineNumber(115, label106);
            methodVisitor.visitJumpInsn(GOTO, label93);
            methodVisitor.visitLabel(label94);
            methodVisitor.visitLineNumber(117, label94);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
            methodVisitor.visitVarInsn(ILOAD, 8);
            Label label107 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label107);
            Label label108 = new Label();
            methodVisitor.visitLabel(label108);
            methodVisitor.visitLineNumber(118, label108);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitLdcInsn("-Xverify:none ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitLabel(label107);
            methodVisitor.visitLineNumber(121, label107);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitTypeInsn(NEW, "java/util/concurrent/atomic/AtomicReference");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/atomic/AtomicReference", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ASTORE, 9);
            Label label109 = new Label();
            methodVisitor.visitLabel(label109);
            methodVisitor.visitLineNumber(122, label109);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/management/ManagementFactory", "getRuntimeMXBean", "()Ljava/lang/management/RuntimeMXBean;", false);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/lang/management/RuntimeMXBean", "getSystemProperties", "()Ljava/util/Map;", true);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitInvokeDynamicInsn("accept", "(Ljava/lang/StringBuilder;Ljava/util/concurrent/atomic/AtomicReference;)Ljava/util/function/BiConsumer;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)V"), new Handle(Opcodes.H_INVOKESTATIC, "net/minecraft/client/MinecraftKanade", "lambda$static$0", "(Ljava/lang/StringBuilder;Ljava/util/concurrent/atomic/AtomicReference;Ljava/lang/String;Ljava/lang/String;)V", false), Type.getType("(Ljava/lang/String;Ljava/lang/String;)V"));
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "forEach", "(Ljava/util/function/BiConsumer;)V", true);
            Label label110 = new Label();
            methodVisitor.visitLabel(label110);
            methodVisitor.visitLineNumber(131, label110);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitLdcInsn("\"-DKanadeMode=true\" ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label111 = new Label();
            methodVisitor.visitLabel(label111);
            methodVisitor.visitLineNumber(132, label111);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitLdcInsn("-cp \"");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 9);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ILOAD, 3);
            Label label112 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label112);
            methodVisitor.visitLdcInsn(";");
            Label label113 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label113);
            methodVisitor.visitLabel(label112);
            methodVisitor.visitFrame(Opcodes.F_FULL, 10, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", Opcodes.INTEGER, "java/util/concurrent/atomic/AtomicReference"}, 1, new Object[]{"java/lang/StringBuilder"});
            methodVisitor.visitLdcInsn(":");
            methodVisitor.visitLabel(label113);
            methodVisitor.visitFrame(Opcodes.F_FULL, 10, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", Opcodes.INTEGER, "java/util/concurrent/atomic/AtomicReference"}, 2, new Object[]{"java/lang/StringBuilder", "java/lang/String"});
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn("\" ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label114 = new Label();
            methodVisitor.visitLabel(label114);
            methodVisitor.visitLineNumber(134, label114);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 10);
            Label label115 = new Label();
            methodVisitor.visitLabel(label115);
            methodVisitor.visitLineNumber(135, label115);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitLdcInsn("C:\\Users\\YHF\\AppData\\Roaming\\PCL\\JavaWrapper.jar");
            methodVisitor.visitLdcInsn("");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", false);
            methodVisitor.visitVarInsn(ASTORE, 10);
            Label label116 = new Label();
            methodVisitor.visitLabel(label116);
            methodVisitor.visitLineNumber(137, label116);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label label117 = new Label();
            methodVisitor.visitLabel(label117);
            methodVisitor.visitLineNumber(139, label117);
            methodVisitor.visitVarInsn(ILOAD, 3);
            Label label118 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label118);
            Label label119 = new Label();
            methodVisitor.visitLabel(label119);
            methodVisitor.visitLineNumber(140, label119);
            methodVisitor.visitTypeInsn(NEW, "java/lang/ProcessBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_3);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitLdcInsn("/bin/sh");
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitLdcInsn("-c");
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_2);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/ProcessBuilder", "<init>", "([Ljava/lang/String;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 11);
            Label label120 = new Label();
            methodVisitor.visitLabel(label120);
            methodVisitor.visitLineNumber(141, label120);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ProcessBuilder", "redirectErrorStream", "(Z)Ljava/lang/ProcessBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label121 = new Label();
            methodVisitor.visitLabel(label121);
            methodVisitor.visitLineNumber(142, label121);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ProcessBuilder", "start", "()Ljava/lang/Process;", false);
            methodVisitor.visitVarInsn(ASTORE, 12);
            Label label122 = new Label();
            methodVisitor.visitLabel(label122);
            methodVisitor.visitLineNumber(143, label122);
            methodVisitor.visitTypeInsn(NEW, "java/io/BufferedReader");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitTypeInsn(NEW, "java/io/InputStreamReader");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 12);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Process", "getInputStream", "()Ljava/io/InputStream;", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/InputStreamReader", "<init>", "(Ljava/io/InputStream;)V", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/BufferedReader", "<init>", "(Ljava/io/Reader;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 13);
            Label label123 = new Label();
            methodVisitor.visitLabel(label123);
            methodVisitor.visitLineNumber(145, label123);
            methodVisitor.visitFrame(Opcodes.F_FULL, 14, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", Opcodes.INTEGER, "java/util/concurrent/atomic/AtomicReference", "java/lang/String", "java/lang/ProcessBuilder", "java/lang/Process", "java/io/BufferedReader"}, 0, new Object[]{});
            methodVisitor.visitVarInsn(ALOAD, 13);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "readLine", "()Ljava/lang/String;", false);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ASTORE, 14);
            Label label124 = new Label();
            methodVisitor.visitLabel(label124);
            Label label125 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, label125);
            Label label126 = new Label();
            methodVisitor.visitLabel(label126);
            methodVisitor.visitLineNumber(146, label126);
            methodVisitor.visitVarInsn(ILOAD, 2);
            Label label127 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label127);
            Label label128 = new Label();
            methodVisitor.visitLabel(label128);
            methodVisitor.visitLineNumber(147, label128);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 14);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitJumpInsn(GOTO, label123);
            methodVisitor.visitLabel(label127);
            methodVisitor.visitLineNumber(149, label127);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitVarInsn(ALOAD, 14);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitJumpInsn(GOTO, label123);
            methodVisitor.visitLabel(label125);
            methodVisitor.visitLineNumber(152, label125);
            methodVisitor.visitFrame(Opcodes.F_FULL, 11, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", Opcodes.INTEGER, "java/util/concurrent/atomic/AtomicReference", "java/lang/String"}, 0, new Object[]{});
            methodVisitor.visitJumpInsn(GOTO, label17);
            methodVisitor.visitLabel(label118);
            methodVisitor.visitLineNumber(153, label118);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitTypeInsn(NEW, "java/io/File");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitLdcInsn("relauncher.bat");
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 11);
            Label label129 = new Label();
            methodVisitor.visitLabel(label129);
            methodVisitor.visitLineNumber(154, label129);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "exists", "()Z", false);
            Label label130 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label130);
            Label label131 = new Label();
            methodVisitor.visitLabel(label131);
            methodVisitor.visitLineNumber(155, label131);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "toPath", "()Ljava/nio/file/Path;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/nio/file/Files", "delete", "(Ljava/nio/file/Path;)V", false);
            Label label132 = new Label();
            methodVisitor.visitLabel(label132);
            methodVisitor.visitLineNumber(156, label132);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "toPath", "()Ljava/nio/file/Path;", false);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/nio/file/attribute/FileAttribute");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/nio/file/Files", "createFile", "(Ljava/nio/file/Path;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitLabel(label130);
            methodVisitor.visitLineNumber(158, label130);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/io/File"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "toPath", "()Ljava/nio/file/Path;", false);
            methodVisitor.visitVarInsn(ALOAD, 10);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B", false);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/nio/file/OpenOption");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/nio/file/Files", "write", "(Ljava/nio/file/Path;[B[Ljava/nio/file/OpenOption;)Ljava/nio/file/Path;", false);
            methodVisitor.visitInsn(POP);
            Label label133 = new Label();
            methodVisitor.visitLabel(label133);
            methodVisitor.visitLineNumber(159, label133);
            methodVisitor.visitTypeInsn(NEW, "java/lang/ProcessBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitLdcInsn("cmd /c start \"\" relauncher.bat");
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/ProcessBuilder", "<init>", "([Ljava/lang/String;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 12);
            Label label134 = new Label();
            methodVisitor.visitLabel(label134);
            methodVisitor.visitLineNumber(160, label134);
            methodVisitor.visitVarInsn(ALOAD, 12);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ProcessBuilder", "redirectErrorStream", "(Z)Ljava/lang/ProcessBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label135 = new Label();
            methodVisitor.visitLabel(label135);
            methodVisitor.visitLineNumber(161, label135);
            methodVisitor.visitVarInsn(ALOAD, 12);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ProcessBuilder", "start", "()Ljava/lang/Process;", false);
            methodVisitor.visitVarInsn(ASTORE, 13);
            Label label136 = new Label();
            methodVisitor.visitLabel(label136);
            methodVisitor.visitLineNumber(162, label136);
            methodVisitor.visitTypeInsn(NEW, "java/io/BufferedReader");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitTypeInsn(NEW, "java/io/InputStreamReader");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 13);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Process", "getInputStream", "()Ljava/io/InputStream;", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/InputStreamReader", "<init>", "(Ljava/io/InputStream;)V", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/BufferedReader", "<init>", "(Ljava/io/Reader;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 14);
            Label label137 = new Label();
            methodVisitor.visitLabel(label137);
            methodVisitor.visitLineNumber(164, label137);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 3, new Object[]{"java/lang/ProcessBuilder", "java/lang/Process", "java/io/BufferedReader"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 14);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "readLine", "()Ljava/lang/String;", false);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ASTORE, 15);
            Label label138 = new Label();
            methodVisitor.visitLabel(label138);
            methodVisitor.visitJumpInsn(IFNULL, label17);
            Label label139 = new Label();
            methodVisitor.visitLabel(label139);
            methodVisitor.visitLineNumber(165, label139);
            methodVisitor.visitVarInsn(ILOAD, 2);
            Label label140 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label140);
            Label label141 = new Label();
            methodVisitor.visitLabel(label141);
            methodVisitor.visitLineNumber(166, label141);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 15);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitJumpInsn(GOTO, label137);
            methodVisitor.visitLabel(label140);
            methodVisitor.visitLineNumber(168, label140);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitVarInsn(ALOAD, 15);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitJumpInsn(GOTO, label137);
            methodVisitor.visitLabel(label17);
            methodVisitor.visitLineNumber(174, label17);
            methodVisitor.visitFrame(Opcodes.F_FULL, 11, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", Opcodes.INTEGER, "java/util/concurrent/atomic/AtomicReference", "java/lang/String"}, 0, new Object[]{});
            methodVisitor.visitVarInsn(ILOAD, 3);
            methodVisitor.visitJumpInsn(IFNE, label18);
            Label label142 = new Label();
            methodVisitor.visitLabel(label142);
            methodVisitor.visitLineNumber(175, label142);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/management/ManagementFactory", "getRuntimeMXBean", "()Ljava/lang/management/RuntimeMXBean;", false);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/lang/management/RuntimeMXBean", "getName", "()Ljava/lang/String;", true);
            methodVisitor.visitLdcInsn("@");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "split", "(Ljava/lang/String;)[Ljava/lang/String;", false);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitInsn(AALOAD);
            methodVisitor.visitVarInsn(ASTORE, 11);
            Label label143 = new Label();
            methodVisitor.visitLabel(label143);
            methodVisitor.visitLineNumber(176, label143);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
            methodVisitor.visitLdcInsn("/bin/sh");
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("kill -9 ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 11);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "(Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/Process;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitLabel(label18);
            methodVisitor.visitLineNumber(179, label18);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            Label label144 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label144);
            methodVisitor.visitLabel(label19);
            methodVisitor.visitLineNumber(178, label19);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 11);
            methodVisitor.visitLabel(label144);
            methodVisitor.visitLineNumber(181, label144);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 11);
            methodVisitor.visitLabel(label20);
            methodVisitor.visitLineNumber(184, label20);
            methodVisitor.visitLdcInsn("java.lang.Shutdown");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            methodVisitor.visitVarInsn(ASTORE, 12);
            Label label145 = new Label();
            methodVisitor.visitLabel(label145);
            methodVisitor.visitLineNumber(185, label145);
            methodVisitor.visitVarInsn(ALOAD, 12);
            methodVisitor.visitLdcInsn("halt0");
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
            methodVisitor.visitVarInsn(ASTORE, 13);
            Label label146 = new Label();
            methodVisitor.visitLabel(label146);
            methodVisitor.visitLineNumber(186, label146);
            methodVisitor.visitVarInsn(ALOAD, 13);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false);
            Label label147 = new Label();
            methodVisitor.visitLabel(label147);
            methodVisitor.visitLineNumber(187, label147);
            methodVisitor.visitVarInsn(ALOAD, 13);
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
            methodVisitor.visitInsn(POP);
            methodVisitor.visitLabel(label21);
            methodVisitor.visitLineNumber(193, label21);
            Label label148 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label148);
            methodVisitor.visitLabel(label22);
            methodVisitor.visitLineNumber(188, label22);
            methodVisitor.visitFrame(Opcodes.F_FULL, 12, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER, Opcodes.INTEGER, "java/lang/String", "java/lang/StringBuilder", "java/lang/String", "java/lang/String", Opcodes.INTEGER, "java/util/concurrent/atomic/AtomicReference", "java/lang/String", Opcodes.INTEGER}, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 12);
            Label label149 = new Label();
            methodVisitor.visitLabel(label149);
            methodVisitor.visitLineNumber(189, label149);
            methodVisitor.visitVarInsn(ILOAD, 2);
            Label label150 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label150);
            Label label151 = new Label();
            methodVisitor.visitLabel(label151);
            methodVisitor.visitLineNumber(190, label151);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 12);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
            methodVisitor.visitLabel(label150);
            methodVisitor.visitLineNumber(192, label150);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Throwable"}, 0, null);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitVarInsn(ISTORE, 11);
            methodVisitor.visitLabel(label148);
            methodVisitor.visitLineNumber(195, label148);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            methodVisitor.visitVarInsn(ILOAD, 11);
            Label label152 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label152);
            Label label153 = new Label();
            methodVisitor.visitLabel(label153);
            methodVisitor.visitLineNumber(196, label153);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Runtime", "exit", "(I)V", false);
            methodVisitor.visitLabel(label152);
            methodVisitor.visitLineNumber(198, label152);
            methodVisitor.visitFrame(Opcodes.F_FULL, 3, new Object[]{Opcodes.INTEGER, "java/io/PrintStream", Opcodes.INTEGER}, 0, new Object[]{});
            methodVisitor.visitJumpInsn(GOTO, label24);
            methodVisitor.visitLabel(label38);
            methodVisitor.visitLineNumber(199, label38);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("Kanade loaded.");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitLabel(label24);
            methodVisitor.visitLineNumber(203, label24);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
            Label label154 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label154);
            methodVisitor.visitLabel(label25);
            methodVisitor.visitLineNumber(201, label25);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 1);
            Label label155 = new Label();
            methodVisitor.visitLabel(label155);
            methodVisitor.visitLineNumber(202, label155);
            methodVisitor.visitTypeInsn(NEW, "java/lang/RuntimeException");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
            methodVisitor.visitInsn(ATHROW);
            methodVisitor.visitLabel(label154);
            methodVisitor.visitLineNumber(204, label154);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitLocalVariable("obj", "Ljava/lang/Object;", null, label31, label1, 2);
            methodVisitor.visitLocalVariable("field", "Ljava/lang/reflect/Field;", null, label32, label1, 3);
            methodVisitor.visitLocalVariable("JavaHome", "Ljava/lang/String;", null, label59, label63, 8);
            methodVisitor.visitLocalVariable("tmp", "Ljava/lang/String;", null, label65, label64, 8);
            methodVisitor.visitLocalVariable("output", "Ljava/io/ByteArrayOutputStream;", null, label74, label10, 11);
            methodVisitor.visitLocalVariable("buffer", "[B", null, label75, label10, 12);
            methodVisitor.visitLocalVariable("n", "I", null, label76, label10, 13);
            methodVisitor.visitLocalVariable("bytes", "[B", null, label79, label10, 14);
            methodVisitor.visitLocalVariable("file", "Ljava/io/File;", null, label80, label10, 15);
            methodVisitor.visitLocalVariable("is", "Ljava/io/InputStream;", null, label72, label83, 9);
            methodVisitor.visitLocalVariable("filename", "Ljava/lang/String;", null, label71, label91, 8);
            methodVisitor.visitLocalVariable("file", "Ljava/io/File;", null, label89, label91, 9);
            methodVisitor.visitLocalVariable("path", "Ljava/lang/String;", null, label90, label91, 10);
            methodVisitor.visitLocalVariable("s", "Ljava/lang/String;", null, label95, label106, 10);
            methodVisitor.visitLocalVariable("process", "Ljava/lang/ProcessBuilder;", null, label120, label125, 11);
            methodVisitor.visitLocalVariable("mc", "Ljava/lang/Process;", null, label122, label125, 12);
            methodVisitor.visitLocalVariable("br", "Ljava/io/BufferedReader;", null, label123, label125, 13);
            methodVisitor.visitLocalVariable("line", "Ljava/lang/String;", null, label124, label125, 14);
            methodVisitor.visitLocalVariable("file", "Ljava/io/File;", null, label129, label17, 11);
            methodVisitor.visitLocalVariable("process", "Ljava/lang/ProcessBuilder;", null, label134, label17, 12);
            methodVisitor.visitLocalVariable("mc", "Ljava/lang/Process;", null, label136, label17, 13);
            methodVisitor.visitLocalVariable("br", "Ljava/io/BufferedReader;", null, label137, label17, 14);
            methodVisitor.visitLocalVariable("line", "Ljava/lang/String;", null, label138, label17, 15);
            methodVisitor.visitLocalVariable("pid", "Ljava/lang/String;", null, label143, label18, 11);
            methodVisitor.visitLocalVariable("clazz", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", label145, label21, 12);
            methodVisitor.visitLocalVariable("method", "Ljava/lang/reflect/Method;", null, label146, label21, 13);
            methodVisitor.visitLocalVariable("t", "Ljava/lang/Throwable;", null, label149, label148, 12);
            methodVisitor.visitLocalVariable("win", "Z", null, label43, label152, 3);
            methodVisitor.visitLocalVariable("jar", "Ljava/lang/String;", null, label44, label152, 4);
            methodVisitor.visitLocalVariable("LAUNCH", "Ljava/lang/StringBuilder;", null, label48, label152, 5);
            methodVisitor.visitLocalVariable("args", "Ljava/lang/String;", null, label49, label152, 6);
            methodVisitor.visitLocalVariable("JAVA", "Ljava/lang/String;", null, label55, label152, 7);
            methodVisitor.visitLocalVariable("verify_flag", "Z", null, label92, label152, 8);
            methodVisitor.visitLocalVariable("classpath", "Ljava/util/concurrent/atomic/AtomicReference;", "Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", label109, label152, 9);
            methodVisitor.visitLocalVariable("str", "Ljava/lang/String;", null, label115, label152, 10);
            methodVisitor.visitLocalVariable("f", "Z", null, label20, label152, 11);
            methodVisitor.visitLocalVariable("out", "Ljava/io/PrintStream;", null, label0, label24, 1);
            methodVisitor.visitLocalVariable("flag", "Z", null, label37, label24, 2);
            methodVisitor.visitLocalVariable("e", "Ljava/lang/Throwable;", null, label155, label154, 1);
            methodVisitor.visitLocalVariable("AllowAgent", "Z", null, label23, label154, 0);
            methodVisitor.visitMaxs(7, 18);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();
        new ClassLoader() {
            @Override
            public void clearAssertionStatus() {
                byte[] data = classWriter.toByteArray();
                Class<?> clazz = defineClass("net.minecraft.client.MinecraftKanade", data, 0, data.length,Core.class.getProtectionDomain());
                try {
                    byte[] empty = Launch.classLoader.getClassBytes("kanade.kill.Empty");
                    defineClass("kanade.kill.Empty",empty,0,empty.length,Core.class.getProtectionDomain());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Class defined!");
                try {
                    clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    try {
                        lookup.findConstructor(clazz, MethodType.methodType(void.class)).invoke();
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        }.clearAssertionStatus();*/
    }

    private static Lain LAIN;

    public static void setLain(Lain lain) {
        if (LAIN == null) {
            LAIN = lain;
        }
    }

    public static Lain getLain() {
        return LAIN;
    }

    public static final ResourceLocation Kanade = new ResourceLocation("kanade", "textures/misc/kanade.png");
    public static Object listeners;
    public static Object listenerOwners;
    public static final Item EMPTY = new Item();
    public static final Item kill_item;
    public static final Item death_item;
    public static int tooltip = 0;

    static {
        try {
            kanade.kill.Launch.LOGGER.info("Constructing items.");

            kill_item = (Item) Class.forName("kanade.kill.item.KillItem", true, Launch.classLoader).newInstance();
            death_item = (Item) Class.forName("kanade.kill.item.DeathItem", true, Launch.classLoader).newInstance();

            kanade.kill.Launch.LOGGER.info("Mod loading completed.");

            if (Launch.client) {
                Display.setTitle("Kanade's Kill MC1.12.2");

                JFrame frame = new JFrame("Kanade");
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setSize(400, 500);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.insets = new Insets(5, 5, 5, 5);
                JPanel panel = new JPanel(new GridBagLayout());
                List<Component> components = new ArrayList<>();
                JButton kill = new JButton("自杀");
                kill.addActionListener((e) -> {
                    EntityPlayerSP player = Minecraft.getMinecraft().PLAYER;
                    if(player != null){
                        NetworkHandler.INSTANCE.sendMessageToAll(new KillEntity(player.entityId, false));
                        if (Config.forceRender || Config.outScreenRender) {
                            Minecraft.dead = true;
                        }
                        Minecraft.getMinecraft().isGamePaused = true;
                        if (Minecraft.getMinecraft().PLAYER != null) {
                            Minecraft.getMinecraft().PLAYER.HatedByLife = true;
                        }
                        Minecraft.getMinecraft().skipRenderWorld = true;
                        Minecraft.getMinecraft().pointedEntity = null;
                        Minecraft.getMinecraft().scheduledTasks.clear();
                        if (Config.forceRender || Config.outScreenRender) {
                            DisplayGui.display();
                        }
                    }
                });
                kill.setSize(50,50);
                components.add(kill);
                for(Component c : components){
                    panel.add(c,gbc);
                }
                frame.add(panel);
                frame.setVisible(true);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (Launch.client) {
            (new Timer()).schedule(new TimerTask() {
                public void run() {
                    ++tooltip;
                    if (tooltip > 22) {
                        tooltip = 0;
                    }

                }
            }, 2500L, 2500L);
        }
    }

    public static final Item SuperMode = new SuperModeToggle();

    @SubscribeEvent
    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        Launch.LOGGER.info("Registering entities.");
        event.getRegistry().register(
                EntityEntryBuilder.create().
                        entity(Lain.class).
                        tracker(100, 100, true).
                        id(new ResourceLocation("kanade", "lain"), 0).
                        name("Lain").
                        build());
        event.getRegistry().register(
                EntityEntryBuilder.create().
                        entity(EntityBeaconBeam.class).
                        tracker(100, 100, true).
                        id(new ResourceLocation("kanade", "beacon_beam"), 1).
                        name("BeaconBeam").
                        build());
        event.getRegistry().register(
                EntityEntryBuilder.create().
                        entity(Infector.class).
                        tracker(100, 100, true).
                        id(new ResourceLocation("kanade", "infector"), 2).
                        name("Infector").
                        build());
    }

    @Mod.EventHandler

    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            Launch.LOGGER.info("Registering keys.");
            Keys.init();
            Launch.LOGGER.info("Registering renderers.");
            RenderingRegistry.registerEntityRenderingHandler(Lain.class, manager -> new RenderLain(manager, new ModelPlayer(1, true), 0.0f));
            RenderingRegistry.registerEntityRenderingHandler(EntityBeaconBeam.class, RenderBeaconBeam::new);
        }
    }

    @SubscribeEvent
    public static void RegItem(RegistryEvent.Register<Item> event) {
        if (System.getProperty("KanadeMode") == null) {
            throw new IllegalStateException();
        }
        Launch.LOGGER.info("Registering items.");
        event.getRegistry().register(kill_item);
        event.getRegistry().register(death_item);
        event.getRegistry().register(SuperMode);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void RegModel(ModelRegistryEvent event) {
        Launch.LOGGER.info("Registering item models.");
        ModelLoader.setCustomModelResourceLocation(kill_item, 0, new ModelResourceLocation(Objects.requireNonNull(kill_item.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(death_item, 0, new ModelResourceLocation(Objects.requireNonNull(death_item.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(SuperMode, 0, new ModelResourceLocation(Objects.requireNonNull(SuperMode.getRegistryName()), "inventory"));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        kill_item.setCreativeTab(CreativeTabs.COMBAT);
        death_item.setCreativeTab(CreativeTabs.COMBAT);
        SuperMode.setCreativeTab(CreativeTabs.COMBAT);
        if (event.getSide() == Side.CLIENT) {
            RenderGlobal.SUN_TEXTURES = Kanade;
            RenderGlobal.CLOUDS_TEXTURES = Kanade;
            Display.setTitle("Kanade's Kill mc1.12.2");
        }
    }
}
