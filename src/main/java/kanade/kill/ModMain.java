package kanade.kill;

import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.entity.Lain;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.render.RenderLain;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;
import scala.concurrent.util.Unsafe;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.Timer;
import java.util.*;

@Mod(modid = "kanade")
@Mod.EventBusSubscriber
@SuppressWarnings("unchecked")
public class ModMain {
    public static final Item EMPTY = new Item();
    public static final Item kill_item;
    public static final Item death_item;
    public static int tooltip = 0;

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

    static {
        try {
            kanade.kill.Launch.LOGGER.info("Defining classes.");

            final List<String> classes = new ArrayList<>();
            ProtectionDomain domain = Loader.class.getProtectionDomain();

            if (Launch.client) {
                classes.add("kanade.kill.util.BufferBuilder");
                classes.add("kanade.kill.util.DefaultVertexFormats");
                classes.add("kanade.kill.util.FontRenderer");
                classes.add("kanade.kill.util.VertexFormat");
                classes.add("kanade.kill.util.VertexFormatElement");
            }

            classes.add("kanade.kill.entity.Lain");
            classes.add("kanade.kill.util.EntityUtil");
            classes.add("kanade.kill.item.KillItem");
            classes.add("kanade.kill.item.DeathItem");
            classes.add("kanade.kill.reflection.LateFields");
            classes.add("kanade.kill.network.NetworkHandler");
            classes.add("kanade.kill.network.packets.Annihilation");
            classes.add("kanade.kill.network.packets.Annihilation$MessageHandler");
            classes.add("kanade.kill.network.packets.ClientTimeStop");
            classes.add("kanade.kill.network.packets.ClientTimeStop$MessageHandler");
            classes.add("kanade.kill.network.packets.CoreDump");
            classes.add("kanade.kill.network.packets.CoreDump$MessageHandler");
            classes.add("kanade.kill.network.packets.KillCurrentPlayer");
            classes.add("kanade.kill.network.packets.KillCurrentPlayer$MessageHandler");
            classes.add("kanade.kill.network.packets.KillEntity");
            classes.add("kanade.kill.network.packets.KillEntity$MessageHandler");
            classes.add("kanade.kill.network.packets.ServerTimeStop");
            classes.add("kanade.kill.network.packets.ServerTimeStop$MessageHandler");
            classes.add("kanade.kill.network.packets.SwitchTimePoint");
            classes.add("kanade.kill.network.packets.SwitchTimePoint$MessageHandler");
            classes.add("kanade.kill.network.packets.SaveTimePoint$MessageHandler");
            classes.add("kanade.kill.network.packets.SaveTimePoint");
            classes.add("kanade.kill.network.packets.TimeBack$MessageHandler");
            classes.add("kanade.kill.network.packets.TimeBack");
            classes.add("kanade.kill.network.packets.ClientReload$MessageHandler");
            classes.add("kanade.kill.network.packets.ClientReload");
            classes.add("kanade.kill.network.packets.BlackHole");
            classes.add("kanade.kill.network.packets.BlackHole$MessageHandler");
            classes.add("kanade.kill.network.packets.ConfigUpdatePacket");
            classes.add("kanade.kill.network.packets.ConfigUpdatePacket$MessageHandler");
            classes.add("kanade.kill.network.packets.UpdatePlayerProtectedState");
            classes.add("kanade.kill.network.packets.UpdatePlayerProtectedState$MessageHandler");
            classes.add("kanade.kill.command.KanadeKillCommand");
            classes.add("kanade.kill.network.packets.KillAllEntities");
            classes.add("kanade.kill.network.packets.KillAllEntities$MessageHandler");



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

                    for (IClassTransformer transformer : KanadeClassLoader.NecessaryTransformers) {
                        bytes = transformer.transform(s, s, bytes);
                    }
                    Class<?> Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, Launch.classLoader, domain);
                    ((Map<String, Class>) Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.cachedClasses_offset)).put(s, Clazz);

                }
            }

            kanade.kill.Launch.LOGGER.info("Constructing items.");

            kill_item = (Item) Class.forName("kanade.kill.item.KillItem", false, Launch.classLoader).newInstance();
            death_item = (Item) Class.forName("kanade.kill.item.DeathItem", false, Launch.classLoader).newInstance();

            kanade.kill.Launch.LOGGER.info("Mod loading completed.");

            if (Launch.client) {
                Display.setTitle("Kanade's Kill R1 beta MC1.12.2");
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (Launch.client) {
            Thread thread = new Thread(() -> JOptionPane.showMessageDialog(null, "Kanade's Kill会使游戏启动时间大幅延长，具体取决于你安装的mod数量。"));
            thread.start();
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

    @SubscribeEvent
    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create().entity(Lain.class).tracker(100, 100, true).id(new ResourceLocation("kanade", "lain"), 0).name("Lain").build());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (Launch.client) {
            Keys.init();
            RenderingRegistry.registerEntityRenderingHandler(Lain.class, manager -> new RenderLain(manager, new ModelPlayer(1, true), 0.0f));
        }
    }
}
