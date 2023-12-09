package kanade.kill;

import com.sun.jna.Platform;
import net.minecraftforge.fml.common.TracingPrintStream;
import net.minecraftforge.fml.relauncher.FMLCorePlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class Core extends FMLCorePlugin {

    static {
        try {
            if (System.getProperty("KanadeMode") == null) {
                String jar = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("!/kanade/kill/Empty.class", "").replace("file:", "");
                if (Platform.isWindows()) {
                    jar = jar.substring(1);
                }

                boolean win = Platform.isWindows();
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
                    tmp = "\"" + tmp + "\"";
                    LAUNCH.insert(0, tmp);
                }
                for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if (s.startsWith("-javaagent:") || s.startsWith("-agentpath:") || s.startsWith("-agentlib:")) {
                        continue;
                    }
                    if (s.contains("=")) {
                        if (!win) LAUNCH.append('"');
                        LAUNCH.append(s);
                        if (!win) LAUNCH.append('"');
                    } else LAUNCH.append(s);
                    LAUNCH.append(' ');
                }
                AtomicReference<String> classpath = new AtomicReference<>();
                ManagementFactory.getRuntimeMXBean().getSystemProperties().forEach((s, v) -> {
                    if (!s.equals("java.class.path")) {
                        LAUNCH.append("\"-D").append(s).append("=").append(v).append("\" ");
                    } else {
                        classpath.set(v);
                    }
                });
                LAUNCH.append("\"-DKanadeMode=true\" ");
                LAUNCH.append("-cp ").append(classpath).append(":").append(jar).append(" ").append(args);
                System.out.println(LAUNCH);

                PrintStream output = null;

                try {
                    TracingPrintStream tps = (TracingPrintStream) System.out;
                    Field field = FilterOutputStream.class.getDeclaredField("out");
                    field.setAccessible(true);
                    output = (PrintStream) field.get(tps);
                } catch (Throwable ignored) {
                }

                boolean flag = output != null;

                String shell = win ? "cmd /c " : "/bin/sh";
                ProcessBuilder process = win ? new ProcessBuilder(shell, LAUNCH.toString()) : new ProcessBuilder(shell, "-c", LAUNCH.toString());
                process.redirectErrorStream(true);
                Process mc = process.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(mc.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    if (flag) {
                        output.println(line);
                    } else {
                        System.out.println(line);
                    }
                }

                flag = false;

                try {
                    Class<?> clazz = Class.forName("java.lang.Shutdown");
                    Method method = clazz.getDeclaredMethod("halt0", int.class);
                    method.invoke(null, 0);
                } catch (Throwable t) {
                    flag = true;
                }

                if (flag) {
                    Runtime.getRuntime().exit(0);
                }
            } else {
                System.out.println("Kanade loaded.");
            }
	    
            /*if (funny) {
                Core.LOGGER.warn("Vanish mode enabled.");
            }

            Core.LOGGER.info("Kanade Core loading.");

            final List<String> classes = new ArrayList<>();
            ProtectionDomain domain = Launch.class.getProtectionDomain();

            classes.add("kanade.kill.Config");
            classes.add("kanade.kill.util.Util");
            classes.add("kanade.kill.reflection.EarlyMethods");
            classes.add("kanade.kill.reflection.ReflectionUtil");
            classes.add("kanade.kill.reflection.EarlyFields");
            classes.add("kanade.kill.asm.ASMUtil");
            classes.add("kanade.kill.asm.injections.DimensionManager");
            classes.add("kanade.kill.asm.injections.Entity");
            classes.add("kanade.kill.asm.injections.EntityLivingBase");
            classes.add("kanade.kill.asm.injections.EntityPlayer");
            classes.add("kanade.kill.asm.injections.EventBus");
            classes.add("kanade.kill.asm.injections.FMLClientHandler");
            classes.add("kanade.kill.asm.injections.ForgeHooksClient");
            classes.add("kanade.kill.asm.injections.ItemStack");
            classes.add("kanade.kill.asm.injections.Minecraft");
            classes.add("kanade.kill.asm.injections.MinecraftForge");
            classes.add("kanade.kill.asm.injections.MinecraftServer");
            classes.add("kanade.kill.asm.injections.NonNullList");
            classes.add("kanade.kill.asm.injections.RenderGlobal");
            classes.add("kanade.kill.asm.injections.ServerCommandManager");
            classes.add("kanade.kill.asm.injections.World");
            classes.add("kanade.kill.asm.injections.WorldClient");
            classes.add("kanade.kill.asm.injections.WorldServer");
            classes.add("kanade.kill.asm.Transformer");
            classes.add("kanade.kill.util.TransformerList");
            classes.add("kanade.kill.thread.TransformersCheckThread");
            classes.add("kanade.kill.thread.ClassLoaderCheckThread");
            classes.add("kanade.kill.classload.KanadeClassLoader");
            classes.add("kanade.kill.util.FieldInfo");
            classes.add("kanade.kill.util.KanadeSecurityManager");
            classes.add("kanade.kill.thread.SecurityManagerCheckThread");
            classes.add("kanade.kill.thread.KillerThread");
            classes.add("kanade.kill.thread.GuiThread");
            classes.add("kanade.kill.AgentMain");
            classes.add("kanade.kill.Attach");


            for (String s : classes) {
                Core.LOGGER.info("Defining class:" + s);
                try (InputStream is = Empty.class.getResourceAsStream('/' + s.replace('.', '/') + ".class")) {
                    assert is != null;
                    //6 lines below are from Apache common io.
                    final ByteArrayOutputStream output = new ByteArrayOutputStream();
                    final byte[] buffer = new byte[8024];
                    int n;
                    while (-1 != (n = is.read(buffer))) {
                        output.write(buffer, 0, n);
                    }
                    byte[] bytes = output.toByteArray();
                    cachedClasses.put(s, Unsafe.instance.defineClass(s, bytes, 0, bytes.length, Launch.classLoader, domain));
                }
            }

            //Uncompleted.
            //Core.LOGGER.info("Starting attach.");
            //Attach.run();

            Core.LOGGER.info("Injecting into LaunchClassLoader.");

            Object old = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            lists = (List<IClassTransformer>) cachedClasses.get("kanade.kill.util.TransformerList").getConstructor(Collection.class).newInstance(old);
            Unsafe.instance.putObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset, lists);

            Core.LOGGER.info("Constructing TransformersCheckThread.");
            Thread check = (Thread) cachedClasses.get("kanade.kill.thread.TransformersCheckThread").getConstructor(ThreadGroup.class).newInstance(KanadeThreads);
            check.start();

            Core.LOGGER.info("Constructing ClassLoaderCheckThread.");
            check = (Thread) cachedClasses.get("kanade.kill.thread.ClassLoaderCheckThread").getConstructor(ThreadGroup.class).newInstance(KanadeThreads);
            check.start();

            Core.LOGGER.info("Constructing SecurityManagerCheckThread.");
            check = (Thread) cachedClasses.get("kanade.kill.thread.SecurityManagerCheckThread").getConstructor(ThreadGroup.class).newInstance(KanadeThreads);
            check.start();

            Core.LOGGER.info("Constructing KillerThread.");
            check = (Thread) cachedClasses.get("kanade.kill.thread.KillerThread").getConstructor(ThreadGroup.class).newInstance(KanadeThreads);
            check.start();

            Core.LOGGER.info("Core loading completed.");*/
        } catch (Throwable e) {
            System.out.println(e);
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
