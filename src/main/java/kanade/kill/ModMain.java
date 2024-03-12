package kanade.kill;

import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.entity.EntityBeaconBeam;
import kanade.kill.entity.Lain;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.render.RenderBeaconBeam;
import kanade.kill.render.RenderLain;
import kanade.kill.util.ObjectUtil;
import kanade.kill.util.ShaderHelper;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;
import scala.concurrent.util.Unsafe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static kanade.kill.Launch.late_classes;

@Mod(modid = "kanade")
@Mod.EventBusSubscriber
@SuppressWarnings("unchecked")
public class ModMain {
    public static final ResourceLocation Kanade = new ResourceLocation("kanade", "textures/misc/kanade.png");
    public static Object listeners;
    public static Object listenerOwners;
    public static boolean NoMoreEventsShouldBeRegistered = false;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] COSMIC;
    public static final Item EMPTY = new Item();
    public static final Item kill_item;
    public static final Item death_item;
    public static int tooltip = 0;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_0;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_1;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_2;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_3;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_4;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_5;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_6;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_7;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_8;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite COSMIC_9;
    @SideOnly(Side.CLIENT)
    private static TextureMap map;

    static {
        try {
            kanade.kill.Launch.LOGGER.info("Defining classes.");

            ProtectionDomain domain = Loader.class.getProtectionDomain();





            for (String s : late_classes) {
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
                    try {
                        Class<?> Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, Launch.classLoader, domain);
                        ((Map<String, Class>) Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.cachedClasses_offset)).put(s, Clazz);
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

    public static void ServerStart(FMLServerStartingEvent event) {
        if (listeners == null) {
            listeners = ObjectUtil.clone(MinecraftForge.Event_bus.listeners);
        }
        if (listenerOwners == null) {
            listenerOwners = ObjectUtil.clone(MinecraftForge.Event_bus.listenerOwners);
        }
    }

    @Mod.EventHandler
    public static void ServerStopped(FMLServerStoppedEvent event) {
        NoMoreEventsShouldBeRegistered = false;
    }

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite register(String sprite) {

        return map.registerSprite(new ResourceLocation(sprite));
    }

    @SideOnly(Side.CLIENT)
    public static void loadShader(TextureMap map) {
        ModMain.map = map;
        if (Launch.client) {
            String SHADER_ = "kanade:shader/";
            COSMIC_0 = register(SHADER_ + "cosmic_0");
            COSMIC_1 = register(SHADER_ + "cosmic_1");
            COSMIC_2 = register(SHADER_ + "cosmic_2");
            COSMIC_3 = register(SHADER_ + "cosmic_3");
            COSMIC_4 = register(SHADER_ + "cosmic_4");
            COSMIC_5 = register(SHADER_ + "cosmic_5");
            COSMIC_6 = register(SHADER_ + "cosmic_6");
            COSMIC_7 = register(SHADER_ + "cosmic_7");
            COSMIC_8 = register(SHADER_ + "cosmic_8");
            COSMIC_9 = register(SHADER_ + "cosmic_9");
            COSMIC = new TextureAtlasSprite[]{
                    COSMIC_0,
                    COSMIC_1,
                    COSMIC_2,
                    COSMIC_3,
                    COSMIC_4,
                    COSMIC_5,
                    COSMIC_6,
                    COSMIC_7,
                    COSMIC_8,
                    COSMIC_9
            };
        }
    }

    @SubscribeEvent
    public static void RegItem(RegistryEvent.Register<Item> event) {
        Launch.LOGGER.info("Registering items.");
        event.getRegistry().register(kill_item);
        event.getRegistry().register(death_item);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void RegModel(ModelRegistryEvent event) {
        Launch.LOGGER.info("Registering item models.");
        ModelLoader.setCustomModelResourceLocation(kill_item, 0, new ModelResourceLocation(Objects.requireNonNull(kill_item.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(death_item, 0, new ModelResourceLocation(Objects.requireNonNull(death_item.getRegistryName()), "inventory"));
    }

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
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            Launch.LOGGER.info("Registering keys.");
            Keys.init();
            Launch.LOGGER.info("Registering renderers.");
            RenderingRegistry.registerEntityRenderingHandler(Lain.class, manager -> new RenderLain(manager, new ModelPlayer(1, true), 0.0f));
            RenderingRegistry.registerEntityRenderingHandler(EntityBeaconBeam.class, RenderBeaconBeam::new);
            Launch.LOGGER.info("Init shaders.");
            ShaderHelper.initShaders();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        kill_item.setCreativeTab(CreativeTabs.COMBAT);
        death_item.setCreativeTab(CreativeTabs.COMBAT);
        if (event.getSide() == Side.CLIENT) {
            RenderGlobal.SUN_TEXTURES = Kanade;
            RenderGlobal.CLOUDS_TEXTURES = Kanade;
            Display.setTitle("Kanade's Kill mc1.12.2");
        }
    }
}
