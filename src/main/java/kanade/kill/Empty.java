package kanade.kill;

import java.util.Arrays;

public class Empty {
    private static final String[] EXEMPT_LIBS = new String[]{
            "com.google.",
            "com.mojang.",
            "joptsimple.",
            "io.netty.",
            "it.unimi.dsi.fastutil.",
            "oshi.",
            "com.sun.",
            "com.ibm.",
            "paulscode.",
            "com.jcraft"
    };
    private static final String[] EXEMPT_DEV = new String[]{
            "net.minecraft.",
            "net.minecraftforge."
    };

    public static void main(String[] Args) {
        String name = "net.minecraftforge.client.GuiIngameForge";
        boolean transformLib = Arrays.stream(EXEMPT_LIBS).noneMatch(name::startsWith);

        System.out.println(transformLib);
    }
}
