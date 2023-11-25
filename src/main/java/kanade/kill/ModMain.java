package kanade.kill;

import kanade.kill.classload.KanadeClassLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
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
import java.util.Objects;

import static kanade.kill.Core.cachedClasses;

@Mod(modid = "kanade")
@Mod.EventBusSubscriber
public class ModMain {
    public static final Item kill_item;
    public static final Item death_item;
    public static final boolean client = System.getProperty("minecraft.client.jar") != null;
    static {
        try {
            Core.LOGGER.info("Defining classes.");

            final List<String> classes = new ArrayList<>();
            ProtectionDomain domain = Launch.class.getProtectionDomain();
            classes.add("kanade.kill.item.KillItem");
            classes.add("kanade.kill.item.DeathItem");
            classes.add("kanade.kill.reflection.LateFields");
            classes.add("kanade.kill.network.packets.KillAllEntities");
            classes.add("kanade.kill.network.NetworkHandler");

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
                    cachedClasses.put(s, Unsafe.instance.defineClass(s, bytes, 0, bytes.length, KanadeClassLoader.INSTANCE, domain));
                }

            }

            Core.LOGGER.info("Constructing items.");

            kill_item = (Item) cachedClasses.get("kanade.kill.item.KillItem").newInstance();
            death_item = (Item) cachedClasses.get("kanade.kill.item.DeathItem").newInstance();
        } catch (InstantiationException | IllegalAccessException | IOException e) {
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
