package kanade.kill;

import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.EarlyFields;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.concurrent.util.Unsafe;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mod(modid = "kanade")
@Mod.EventBusSubscriber
@SuppressWarnings("unchecked")
public class ModMain {
    public static final Item kill_item;
    public static final Item death_item;
    public static final boolean client = System.getProperty("minecraft.client.jar") != null;
    public static final Class<?> GUI;
    static {
        try {
            kanade.kill.Launch.LOGGER.info("Defining classes.");

            final List<String> classes = new ArrayList<>();
            ProtectionDomain domain = net.minecraft.launchwrapper.Launch.class.getProtectionDomain();
            classes.add("kanade.kill.util.Gui");
            classes.add("kanade.kill.item.KillItem");
            classes.add("kanade.kill.item.DeathItem");
            classes.add("kanade.kill.reflection.LateFields");
            classes.add("kanade.kill.network.packets.KillAllEntities");
            classes.add("kanade.kill.network.NetworkHandler");
            classes.add("kanade.kill.network.packets.CoreDump");
            classes.add("kanade.kill.network.packets.CoreDump$MessageHandler");
            classes.add("kanade.kill.command.KanadeKillCommand");
            classes.add("kanade.kill.network.packets.KillAllEntities$MessageHandler");

            Class<?> tmp = null;

            for (String s : classes) {
                kanade.kill.Launch.LOGGER.info("Defining class:" + s);
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

                    for (IClassTransformer transformer : KanadeClassLoader.SafeTransformers) {
                        bytes = transformer.transform(s, s, bytes);
                    }
                    if (!s.equals("kanade.kill.util.Gui")) {
                        Class<?> Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, kanade.kill.Launch.classLoader, domain);
                        ((Map<String, Class>) Unsafe.instance.getObjectVolatile(kanade.kill.Launch.classLoader, EarlyFields.cachedClasses_offset)).put(s, Clazz);
                    } else {
                        tmp = Unsafe.instance.defineAnonymousClass(GuiScreen.class, bytes, null);
                    }

                }
            }

            GUI = tmp;

            kanade.kill.Launch.LOGGER.info("Constructing items.");

            kill_item = (Item) Class.forName("kanade.kill.item.KillItem", false, Launch.classLoader).newInstance();
            death_item = (Item) Class.forName("kanade.kill.item.DeathItem", false, Launch.classLoader).newInstance();

            kanade.kill.Launch.LOGGER.info("Mod loading completed.");
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (client) {
            Thread thread = new Thread(() -> JOptionPane.showMessageDialog(null, "Kanade's Kill会使游戏启动时间大幅延长，具体取决于你安装的mod数量。"));
            thread.start();
        }
    }

    @SubscribeEvent
    public static void RegItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(kill_item);
        event.getRegistry().register(death_item);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void RegModel(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(kill_item, 0, new ModelResourceLocation(Objects.requireNonNull(kill_item.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(death_item, 0, new ModelResourceLocation(Objects.requireNonNull(death_item.getRegistryName()), "inventory"));
    }

    @SubscribeEvent
    public static void ToolTip(ItemTooltipEvent event) {
        if (event.getItemStack().getITEM() == kill_item) {
            event.getToolTip().add("§f僕らは命に嫌われている。");
        }
    }
}
