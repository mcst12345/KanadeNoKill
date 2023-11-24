package kanade.kill;

import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.ExceptionHandler;
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
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
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
            System.out.println("Defining classes.");

            ProtectionDomain domain = Launch.class.getProtectionDomain();
            InputStream is = Empty.class.getResourceAsStream("/kanade/kill/item/KillItem.class");
            assert is != null;
            byte[] clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.item.KillItem", Unsafe.instance.defineClass("kanade.kill.item.KillItem", clazz, 0, clazz.length, Launch.classLoader, domain));

            is = Empty.class.getResourceAsStream("/kanade/kill/item/DeathItem.class");
            assert is != null;
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.item.DeathItem", Unsafe.instance.defineClass("kanade.kill.item.DeathItem", clazz, 0, clazz.length, Launch.classLoader, domain));

            is = Empty.class.getResourceAsStream("/kanade/kill/reflection/LateFields.class");
            assert is != null;
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.reflection.LateFields", Unsafe.instance.defineClass("kanade.kill.reflection.LateFields", clazz, 0, clazz.length, Launch.classLoader, domain));

            if (client) {
                is = Empty.class.getResourceAsStream("/kanade/kill/thread/GuiThread.class");
                assert is != null;
                clazz = new byte[is.available()];
                is.read(clazz);
                is.close();
                cachedClasses.put("kanade.kill.thread.GuiThread", Unsafe.instance.defineClass("kanade.kill.thread.GuiThread", clazz, 0, clazz.length, Launch.classLoader, domain));
            }

            System.out.println("Constructing items.");

            kill_item = (Item) cachedClasses.get("kanade.kill.item.KillItem").newInstance();
            death_item = (Item) cachedClasses.get("kanade.kill.item.DeathItem").newInstance();

            System.out.println("Replacing exception handlers.");

            ThreadGroup group = Thread.currentThread().getThreadGroup();
            Thread[] threads = new Thread[group.activeCount()];
            group.enumerate(threads);
            for (Thread thread : threads) {
                Unsafe.instance.putObjectVolatile(thread, EarlyFields.uncaughtExceptionHandler_offset, ExceptionHandler.instance);
            }
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }

        if (client) {
            JOptionPane.showMessageDialog(null, "Kanade's Kill会使游戏启动时间大幅延长，具体取决于你安装的mod数量。");
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
