package kanade.kill;

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

import java.io.IOException;
import java.io.InputStream;
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
            InputStream is = Empty.class.getResourceAsStream("/kanade/kill/KillItem.class");
            assert is != null;
            byte[] clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.KillItem", Unsafe.instance.defineClass("kanade.kill.KillItem", clazz, 0, clazz.length, Launch.classLoader, null));

            is = Empty.class.getResourceAsStream("/kanade/kill/DeathItem.class");
            assert is != null;
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.DeathItem", Unsafe.instance.defineClass("kanade.kill.DeathItem", clazz, 0, clazz.length, Launch.classLoader, null));

            is = Empty.class.getResourceAsStream("/kanade/kill/LateFields.class");
            assert is != null;
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.LateFields", Unsafe.instance.defineClass("kanade.kill.LateFields", clazz, 0, clazz.length, Launch.classLoader, null));

            kill_item = (Item) cachedClasses.get("kanade.kill.KillItem").newInstance();
            death_item = (Item) cachedClasses.get("kanade.kill.DeathItem").newInstance();
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
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
            event.getToolTip().add("§f");
        }
    }
}
