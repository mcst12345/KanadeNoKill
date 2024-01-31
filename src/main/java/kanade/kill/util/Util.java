package kanade.kill.util;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.asm.Transformer;
import kanade.kill.item.KillItem;
import kanade.kill.reflection.EarlyMethods;
import kanade.kill.reflection.LateFields;
import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.thread.FieldSaveThread;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.lwjgl.MemoryUtil;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "raw"})
public class Util {
    public static final List<Runnable> tasks = new ArrayList<>();
    public static final Map<Field, Object> cache = new HashMap<>();
    public static final Map<Field, Object> cache2 = new HashMap<>();
    private static Object saved_listeners;
    private static Object saved_listeners_2;
    public static boolean killing;
    static final Object2LongOpenHashMap<Field> offsetCache = new Object2LongOpenHashMap<>();
    static final Object2ObjectOpenHashMap<Field, Object> baseCache = new Object2ObjectOpenHashMap<>();

    public static boolean NoRemove(Object item) {
        return item == ModMain.kill_item || item == ModMain.death_item || (item instanceof ItemStack && NoRemove(((ItemStack) item).getITEM()));
    }


    public synchronized static void save() {
        saved_listeners = ObjectUtil.clone(Unsafe.instance.getObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset));
        for (String s : Transformer.getEventListeners()) {
            try {
                Class<?> clazz = Class.forName(s);

                Launch.LOGGER.info("Listener:" + s);
                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (field == null) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        try {
                            Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
                        } catch (Throwable ignored) {
                        }
                        try {
                            Object o = ObjectUtil.getStatic(field);
                            cache.put(field, ObjectUtil.clone(o));
                        } catch (Throwable t) {
                            if (t instanceof StackOverflowError) {
                                Launch.LOGGER.warn("Too deep. Ignoring this field.");
                            } else {
                                throw new RuntimeException(t);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        saved_listeners_2 = ObjectUtil.clone(Unsafe.instance.getObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2));
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getMod() == null || container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                Class<?> clazz = container.getMod().getClass();
                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (field == null) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        try {
                            Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
                        } catch (Throwable ignored) {
                        }
                        try {
                            Object object = ObjectUtil.getStatic(field);

                            cache.put(field, ObjectUtil.clone(object));
                        } catch (Throwable t) {
                            if (t instanceof StackOverflowError) {
                                Launch.LOGGER.warn("Too deep. Ignoring this field.");
                            } else {
                                throw new RuntimeException(t);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        while (!Transformer.getFields().isEmpty()) {
            FieldInfo fieldinfo = (FieldInfo) Transformer.getFields().poll();
            Field field = fieldinfo.toField();
            if (field == null || cache.containsKey(field)) {
                continue;
            }
            try {
                Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
            } catch (Throwable ignored) {
            }
            try {
                cache2.put(field, ObjectUtil.clone(ObjectUtil.getStatic(field)));
            } catch (Throwable t) {
                if (t instanceof StackOverflowError) {
                    Launch.LOGGER.warn("Too deep. Ignoring this field.");
                } else {
                    throw new RuntimeException(t);
                }
            }
        }

        FieldSaveThread thread = new FieldSaveThread();
        thread.start();
    }

    private static boolean shouldIgnore(Field field) {
        boolean result = field.getType() == CreativeTabs.class || field.getType() == RegistryNamespaced.class || field.getType() == SimpleNetworkWrapper.class;
        Object object = ObjectUtil.getStatic(field);
        return result || object instanceof Item || object instanceof Block || object instanceof Potion || object instanceof Enchantment || object instanceof Logger || object instanceof MinecraftServer || (Launch.client && (object instanceof Minecraft));
    }

    private static boolean shouldIgnore(Class<?> clazz) {
        return clazz == ForgeInternalHandler.class || clazz == ForgeModContainer.class;
    }

    public synchronized static void reset() {
        Unsafe.instance.putObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset, ObjectUtil.clone(saved_listeners));

        for (String s : Transformer.getEventListeners()) {
            try {
                Class<?> clazz = Class.forName(s);

                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (field == null) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        try {
                            Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
                        } catch (Throwable ignored) {
                        }
                        Object object = ObjectUtil.getStatic(field);
                        if (cache.containsKey(field)) {
                            ObjectUtil.putStatic(field, ObjectUtil.clone(cache.get(field)));
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        Unsafe.instance.putObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2, saved_listeners_2);
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getMod() == null || container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                Class<?> clazz = container.getMod().getClass();
                for (Field field : ReflectionUtil.getFields(clazz)) {
                    if (field == null) {
                        continue;
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (shouldIgnore(field)) {
                            continue;
                        }
                        try {
                            Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
                        } catch (Throwable ignored) {
                        }
                        Object object = ObjectUtil.getStatic(field);
                        if (cache.containsKey(field)) {
                            ObjectUtil.putStatic(field, ObjectUtil.clone(cache.get(field)));
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        cache2.forEach((field, object) -> {
            try {
                Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
            } catch (Throwable ignored) {
            }
            ObjectUtil.putStatic(field, ObjectUtil.clone(object));
        });
    }

    public static void CoreDump() {
        Unsafe.instance.freeMemory(114514);
    }

    public static boolean BadGui(net.minecraft.client.gui.Gui gui) {
        if (gui == null) {
            return false;
        }
        String name = ReflectionUtil.getName(gui.getClass());
        String l = name.toLowerCase();
        return (Config.guiProtect && (Transformer.isModClass(name) || gui.getClass().getProtectionDomain() == null || gui.getClass().getProtectionDomain().getCodeSource() == null)) || l.contains("death") || l.contains("over") || l.contains("die") || l.contains("dead");
    }

    public static void DisplayDeathGui() {

    }

    public static boolean isKanadeDeathGui(Object o) {
        return ((o instanceof Minecraft));
    }

    public static boolean isKanadeDeathGuiClosed(Object o) {
        return false;
    }


    public static boolean shouldPostEvent(Event event) {
        if (Launch.client) {
            if (event instanceof GuiOpenEvent) {
                GuiOpenEvent guiOpenEvent = (GuiOpenEvent) event;
                GuiScreen gui = guiOpenEvent.getGui();
                return !(gui instanceof GuiGameOver) && !(gui instanceof GuiChat) && !(gui instanceof GuiIngameMenu) && !(gui instanceof GuiMainMenu);
            } else if (event instanceof EntityViewRenderEvent) {
                EntityViewRenderEvent cameraSetup = (EntityViewRenderEvent) event;
                return !KillItem.inList(cameraSetup.getEntity());
            }
        }
        if (event instanceof PlayerEvent) {
            PlayerEvent playerEvent = (PlayerEvent) event;
            return !KillItem.inList(playerEvent.getEntityPlayer());
        } else if (event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent) {
            net.minecraftforge.fml.common.gameevent.PlayerEvent playerEvent = (net.minecraftforge.fml.common.gameevent.PlayerEvent) event;
            return !KillItem.inList(playerEvent.player);
        } else if (event instanceof EntityEvent) {
            EntityEvent entityEvent = (EntityEvent) event;
            return !KillItem.inList(entityEvent.getEntity());
        }
        return true;
    }

    public static long GLAddress(String name) {
        if (!Launch.client) {
            return 0;
        }
        ByteBuffer buffer = MemoryUtil.encodeASCII(name);
        long addr = MemoryUtil.getAddress(buffer);
        return (long) ReflectionUtil.invoke(EarlyMethods.getFunctionAddress, null, new Object[]{addr});
    }
}