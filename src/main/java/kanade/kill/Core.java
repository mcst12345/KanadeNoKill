package kanade.kill;

import cpw.mods.fml.relauncher.FMLCorePlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

public class Core extends FMLCorePlugin {
    public static final boolean isDemo = false;

    static {
        try {
            PrintStream out = null;
            try {
                Object obj = System.out;
                Field field = FilterOutputStream.class.getDeclaredField("out");
                field.setAccessible(true);
                out = (PrintStream) field.get(obj);
            } catch (Throwable ignored) {
            }

            boolean AllowAgent = Boolean.parseBoolean(System.getProperty("AllowAgent"));

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

                for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if ((!AllowAgent && s.contains("-javaagent:")) || s.contains("-agentpath:") || s.contains("-agentlib:") || s.contains("-D")) {
                        continue;
                    }
                    if (s.contains("=")) {
                        LAUNCH.append('"');
                        LAUNCH.append(s);
                        LAUNCH.append('"');
                    } else LAUNCH.append(s);
                    LAUNCH.append(' ');
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

                System.out.println(LAUNCH);

                if (!win) {
                    ProcessBuilder process = new ProcessBuilder("/bin/sh", "-c", LAUNCH.toString());
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
                    File file = new File("relauncher.bat");
                    if (file.exists()) {
                        Files.delete(file.toPath());
                        Files.createFile(file.toPath());
                    }
                    try (PrintWriter printWriter = new PrintWriter(file)) {
                        printWriter.write(LAUNCH.toString());
                    }
                    ProcessBuilder process = new ProcessBuilder("cmd /c start \"\" relauncher.bat", LAUNCH.toString());
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
                    Class<?> clazz = Class.forName("java.lang.Shutdown");
                    Method method = clazz.getDeclaredMethod("halt0", int.class);
                    method.setAccessible(true);
                    method.invoke(null, 0);
                } catch (Throwable t) {
                    if (flag) {
                        out.println(t);
                    }
                    flag = true;
                }

                if (flag) {
                    Runtime.getRuntime().exit(0);
                }
            } else {
                System.out.println("Kanade loaded.");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
