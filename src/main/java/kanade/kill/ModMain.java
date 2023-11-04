package kanade.kill;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
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
    public static final Item item;
    static {
        try {
            InputStream is = Empty.class.getResourceAsStream("/kanade/kill/KillItem.class");
            assert is != null;
            byte[] clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.KillItem", Unsafe.instance.defineClass("kanade.kill.KillItem", clazz, 0, clazz.length, Launch.classLoader, null));

            is = Empty.class.getResourceAsStream("/kanade/kill/LateFields.class");
            assert is != null;
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.LateFields", Unsafe.instance.defineClass("kanade.kill.LateFields", clazz, 0, clazz.length, Launch.classLoader, null));

            item = (Item) cachedClasses.get("kanade.kill.KillItem").newInstance();
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void RegItem(RegistryEvent.Register<Item> event){
        event.getRegistry().register(item);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void RegModel(ModelRegistryEvent event){
        ModelLoader.setCustomModelResourceLocation(item,0,new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()),"inventory"));
    }
}
