package kanade.kill;

import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.entity.EntityBeaconBeam;
import kanade.kill.entity.Infector;
import kanade.kill.entity.Lain;
import kanade.kill.item.SuperModeToggle;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.KillEntity;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.reflection.LateFields;
import kanade.kill.render.RenderBeaconBeam;
import kanade.kill.render.RenderLain;
import kanade.kill.thread.DisplayGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;
import scala.concurrent.util.Unsafe;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Timer;
import java.util.*;

import static kanade.kill.Launch.late_classes;

@Mod(modid = "kanade", acceptedMinecraftVersions = "1.12.2",version = "4.7.2")
@Mod.EventBusSubscriber
public class ModMain {
    public static final ResourceLocation Kanade = new ResourceLocation("kanade", "textures/misc/kanade.png");
    public static Object listeners;
    public static Object listenerOwners;
    public static final Item EMPTY = new Item();
    public static final Item kill_item;
    public static final Item death_item;
    public static int tooltip = 0;
    static {
        try {
            Launch.LOGGER.info("Defining classes.");
            ProtectionDomain domain = Loader.class.getProtectionDomain();
            for (String s : late_classes) {
                Launch.LOGGER.info("Defining class:{}", s);
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
                    try {
                        Class<?> Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, Launch.classLoader, domain);
                        ((Map<String, Class<?>>) Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.cachedClasses_offset)).put(s, Clazz);
                    } catch (Throwable t) {
                        if (t.getMessage().contains("attempted duplicate class definition") || t.getLocalizedMessage().contains("attempted duplicate class definition")) {
                            continue;
                        }
                        throw new RuntimeException(t);
                    }
                }
            }

            kanade.kill.Launch.LOGGER.info("Constructing items.");

            kill_item = (Item) Class.forName("kanade.kill.item.KillItem", true, Launch.classLoader).newInstance();
            death_item = (Item) Class.forName("kanade.kill.item.DeathItem", true, Launch.classLoader).newInstance();

            kanade.kill.Launch.LOGGER.info("Mod loading completed.");

            if (Launch.client) {
                Display.setTitle("Kanade's Kill MC1.12.2");

                JFrame frame = new JFrame("Kanade");
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setSize(400, 500);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.insets = new Insets(5, 5, 5, 5);
                JPanel panel = new JPanel(new GridBagLayout());
                List<Component> components = new ArrayList<>();
                JButton kill = new JButton("自杀");
                kill.addActionListener((e) -> {
                    EntityPlayerSP player = Minecraft.getMinecraft().PLAYER;
                    if(player != null){
                        NetworkHandler.INSTANCE.sendMessageToAll(new KillEntity(player.entityId, false));
                        if (Config.forceRender || Config.outScreenRender) {
                            Minecraft.dead = true;
                        }
                        Minecraft.getMinecraft().isGamePaused = true;
                        if (Minecraft.getMinecraft().PLAYER != null) {
                            Unsafe.instance.putBooleanVolatile(Minecraft.getMinecraft().PLAYER, LateFields.HatedByLife_offset, true);
                        }
                        Minecraft.getMinecraft().skipRenderWorld = true;
                        Minecraft.getMinecraft().pointedEntity = null;
                        Minecraft.getMinecraft().scheduledTasks.clear();
                        if (Config.forceRender || Config.outScreenRender) {
                            DisplayGui.display();
                        }
                    }
                });
                kill.setSize(50,50);
                components.add(kill);
                for(Component c : components){
                    panel.add(c,gbc);
                }
                frame.add(panel);
                frame.setVisible(true);
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (Launch.client) {
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

    public static final Item SuperMode = new SuperModeToggle();

    @SubscribeEvent
    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        Launch.LOGGER.info("Registering entities.");
        event.getRegistry().register(
                EntityEntryBuilder.create().
                        entity(Lain.class).
                        tracker(100, 100, true).
                        id(new ResourceLocation("kanade", "lain"), 0).
                        name("Lain").
                        build());
        event.getRegistry().register(
                EntityEntryBuilder.create().
                        entity(EntityBeaconBeam.class).
                        tracker(100, 100, true).
                        id(new ResourceLocation("kanade", "beacon_beam"), 1).
                        name("BeaconBeam").
                        build());
        event.getRegistry().register(
                EntityEntryBuilder.create().
                        entity(Infector.class).
                        tracker(100, 100, true).
                        id(new ResourceLocation("kanade", "infector"), 2).
                        name("Infector").
                        build());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            Launch.LOGGER.info("Registering keys.");
            Keys.init();
            Launch.LOGGER.info("Registering renderers.");
            RenderingRegistry.registerEntityRenderingHandler(Lain.class, manager -> new RenderLain(manager, new ModelPlayer(1, true), 0.0f));
            RenderingRegistry.registerEntityRenderingHandler(EntityBeaconBeam.class, RenderBeaconBeam::new);
        }
    }

    @SubscribeEvent
    public static void RegItem(RegistryEvent.Register<Item> event) {
        Launch.LOGGER.info("Registering items.");
        event.getRegistry().register(kill_item);
        event.getRegistry().register(death_item);
        event.getRegistry().register(SuperMode);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void RegModel(ModelRegistryEvent event) {
        Launch.LOGGER.info("Registering item models.");
        ModelLoader.setCustomModelResourceLocation(kill_item, 0, new ModelResourceLocation(Objects.requireNonNull(kill_item.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(death_item, 0, new ModelResourceLocation(Objects.requireNonNull(death_item.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(SuperMode, 0, new ModelResourceLocation(Objects.requireNonNull(SuperMode.getRegistryName()), "inventory"));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        kill_item.setCreativeTab(CreativeTabs.COMBAT);
        death_item.setCreativeTab(CreativeTabs.COMBAT);
        SuperMode.setCreativeTab(CreativeTabs.COMBAT);
        if (event.getSide() == Side.CLIENT) {
            RenderGlobal.SUN_TEXTURES = Kanade;
            RenderGlobal.CLOUDS_TEXTURES = Kanade;
            Display.setTitle("Kanade's Kill mc1.12.2");
        }
    }
}
