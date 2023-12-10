package kanade.kill;

import java.lang.reflect.Method;

public class ServerPreMain {
    public static boolean Server = false;

    private ServerPreMain() {

    }

    public static void main(String[] args) {
        Server = true;
        new ServerPreMain().run(args);
    }

    private void run(String[] args) {
        if (System.getProperty("log4j.configurationFile") == null) {
            // Set this early so we don't need to reconfigure later
            System.setProperty("log4j.configurationFile", "log4j2_server.xml");
        }
        Class<?> launchwrapper = null;
        try {
            launchwrapper = Class.forName("net.minecraft.launchwrapper.Launch", true, getClass().getClassLoader());
            Class.forName("org.objectweb.asm.Type", true, getClass().getClassLoader());
        } catch (Exception e) {
            System.err.print("We appear to be missing one or more essential library files.\n" +
                    "You will need to add them to your server before FML and Forge will run successfully.");
            e.printStackTrace(System.err);
            System.exit(1);
        }

        try {
            Method main = launchwrapper.getMethod("main", String[].class);
            String[] allArgs = new String[args.length + 2];
            allArgs[0] = "--tweakClass";
            allArgs[1] = "net.minecraftforge.fml.common.launcher.FMLServerTweaker";
            System.arraycopy(args, 0, allArgs, 2, args.length);
            main.invoke(null, (Object) allArgs);
        } catch (Exception e) {
            System.err.print("A problem occurred running the Server launcher.");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
