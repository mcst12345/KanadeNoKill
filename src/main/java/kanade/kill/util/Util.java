package kanade.kill.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
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
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.lwjgl.MemoryUtil;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;

import static kanade.kill.ModMain.tooltip;

@SuppressWarnings({"unused", "raw"})
public class Util {
    public static final List<Runnable> tasks = new ArrayList<>();
    public static final Map<Field, Object> cache = new HashMap<>();
    public static final Map<Field, Object> cache2 = new HashMap<>();
    static final Set<UUID> Dead = new HashSet<>();
    static final Map<Field, Long> offsetCache = new HashMap<>();
    static final Map<Field, Object> baseCache = new HashMap<>();
    public static boolean killing;
    private static Object saved_listeners;
    private static Object saved_listeners_2;
    static final Map<UUID, Map<Integer, ItemStack>> item = new HashMap<>();

    public static boolean NoRemove(Object item) {
        return item == ModMain.kill_item || item == ModMain.death_item || (item instanceof ItemStack && NoRemove(((ItemStack) item).getITEM()));
    }


    public synchronized static void save() {
        Launch.LOGGER.info("Coping event listeners in EventBus.");
        saved_listeners = ObjectUtil.clone(Unsafe.instance.getObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset), 0);
        Launch.LOGGER.info("Coping static fields in event listeners.");
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
                            cache.put(field, ObjectUtil.clone(o, 0));
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

        Launch.LOGGER.info("Coping event listeners in Event.");
        saved_listeners_2 = ObjectUtil.clone(Unsafe.instance.getObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2), 0);
        Launch.LOGGER.info("Coping static fields in mod instances.");
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                Launch.LOGGER.info("Mod:" + container.getModId());
                if (container.getMod() == null) {
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

                            cache.put(field, ObjectUtil.clone(object, 0));
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
        Launch.LOGGER.info("Saving fields which the transformer found.");
        while (!Transformer.getFields().isEmpty()) {
            FieldInfo fieldinfo = Transformer.getFields().poll();
            Field field = fieldinfo.toField();
            if (field == null || cache.containsKey(field)) {
                continue;
            }
            try {
                Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
            } catch (Throwable ignored) {
            }
            try {
                cache2.put(field, ObjectUtil.clone(ObjectUtil.getStatic(field), 0));
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
        Launch.LOGGER.info("Resetting cached fields.");
        Launch.LOGGER.info("Resetting event listeners in EventBus.");
        Unsafe.instance.putObjectVolatile(MinecraftForge.Event_bus, LateFields.listeners_offset, ObjectUtil.clone(saved_listeners, 0));
        Launch.LOGGER.info("Resetting static fields in event listeners.");

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
                        Object object = ObjectUtil.getStatic(field);
                        if (cache.containsKey(field)) {
                            Launch.LOGGER.info("Replacing.");
                            ObjectUtil.putStatic(field, ObjectUtil.clone(cache.get(field), 0));
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        Launch.LOGGER.info("Resetting event listeners in Event.");
        Unsafe.instance.putObjectVolatile(LateFields.listeners_base, LateFields.listeners_offset_2, saved_listeners_2);
        Launch.LOGGER.info("Resetting mod static fields.");
        try {
            for (ModContainer container : Loader.instance().getActiveModList()) {
                if (container.getMod() == null || container.getModId().equals("mcp") || container.getModId().equals("minecraft") || container.getModId().equals("FML") || container.getModId().equals("kanade") || container.getModId().equals("forge")) {
                    continue;
                }
                Launch.LOGGER.info("Mod:" + container.getModId());
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
                            Launch.LOGGER.info("Replacing.");
                            ObjectUtil.putStatic(field, ObjectUtil.clone(cache.get(field), 0));
                        }
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        Launch.LOGGER.info("Resetting fields which the transformer found.");
        cache2.forEach((field, object) -> {
            try {
                Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName() + ":" + ObjectUtil.getStatic(field));
            } catch (Throwable ignored) {
            }
            Launch.LOGGER.info("Replacing.");
            ObjectUtil.putStatic(field, ObjectUtil.clone(object, 0));
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
        return (Config.guiProtect && (Transformer.isModClass(name) || gui.getClass().getProtectionDomain() == null || gui.getClass().getProtectionDomain().getCodeSource() == null)) || gui.getClass() == ModMain.GUI || l.contains("death") || l.contains("over") || l.contains("die") || l.contains("dead");
    }

    public static void DisplayDeathGui() {

    }

    public static boolean isKanadeDeathGui(Object o) {
        return o != null && (o.getClass() == ModMain.GUI || (o instanceof Minecraft && Unsafe.instance.getObjectVolatile(o, LateFields.currentScreen_offset).getClass() == ModMain.GUI));
    }

    public static boolean isKanadeDeathGuiClosed(Object o) {
        return false;
    }


    public static boolean shouldPostEvent(Event event) {
        if (Launch.client) {
            if (event instanceof GuiOpenEvent) {
                GuiOpenEvent guiOpenEvent = (GuiOpenEvent) event;
                GuiScreen gui = guiOpenEvent.gui;
                return !ModMain.GUI.isInstance(gui) && !(gui instanceof GuiGameOver) && !(gui instanceof GuiChat) && !(gui instanceof GuiIngameMenu) && !(gui instanceof GuiMainMenu);
            } else if (event instanceof EntityViewRenderEvent) {
                EntityViewRenderEvent cameraSetup = (EntityViewRenderEvent) event;
                return !KillItem.inList(cameraSetup.entity);
            }
        }

        if (event instanceof PlayerEvent) {
            PlayerEvent playerEvent = (PlayerEvent) event;
            if (KillItem.inList(playerEvent.entityPlayer)) {
                if (playerEvent instanceof ItemTooltipEvent) {
                    ToolTip((ItemTooltipEvent) event);
                }
                return false;
            }
        } else if (event instanceof cpw.mods.fml.common.gameevent.PlayerEvent) {
            cpw.mods.fml.common.gameevent.PlayerEvent playerEvent = (cpw.mods.fml.common.gameevent.PlayerEvent) event;
            return !KillItem.inList(playerEvent.player);
        } else if (event instanceof EntityEvent) {
            EntityEvent entityEvent = (EntityEvent) event;
            return !KillItem.inList(entityEvent.entity);
        }
        return true;
    }

    public static void CheckWorlds(MinecraftServer server) {
        if (server.worldServers.length != 0) {
            boolean flag = true;
            WorldServer[] var4 = server.worldServers;
            int var5 = var4.length;

            for (WorldServer worldServer : var4) {
                if (worldServer.getClass() != WorldServer.class) {
                    server.worldServers = server.backup;
                    flag = false;
                    break;
                }
            }

            if (flag) {
                server.backup = server.worldServers;
            }
        }
    }

    public static long GLAddress(String name) {
        if (!Launch.client) {
            return 0;
        }
        ByteBuffer buffer = MemoryUtil.encodeASCII(name);
        long addr = MemoryUtil.getAddress(buffer);
        return (long) ReflectionUtil.invoke(EarlyMethods.getFunctionAddress, null, new Object[]{addr});
    }

    public static void ToolTip(ItemTooltipEvent event) {
        switch (tooltip) {
            case 0: {
                event.toolTip.add("§f僕らは命に嫌われている。");
                break;
            }
            case 1: {
                event.toolTip.add("§fもう一回、もう一回。「私は今日も転がります。」と");
                break;
            }
            case 2: {
                event.toolTip.add("§fアイデンティティ 唸れ 君一人のせい");
                break;
            }
            case 3: {
                event.toolTip.add("§f君は今日もステイ");
                break;
            }
            case 4: {
                event.toolTip.add("§f乙女解剖であそぼうよ");
                break;
            }
            case 5: {
                event.toolTip.add("§fだから妄想感傷代償連盟");
                break;
            }
            case 6: {
                event.toolTip.add("§fロキロキのロックンロックンロール");
                break;
            }
            case 7: {
                event.toolTip.add("§fWelcome to the メルティランド");
                break;
            }
            case 8: {
                event.toolTip.add("§f溶けていく    命が溶けていく");
                break;
            }
            case 9: {
                event.toolTip.add("§f冷たい第三の心臓が  たしたちを見つめていた");
                break;
            }
            case 10: {
                event.toolTip.add("§f感度良好 5-2-4");
                break;
            }
            case 11: {
                event.toolTip.add("§f今後千年草も生えない 砂の惑星さ");
                break;
            }
            case 12: {
                event.toolTip.add("§fらい らい 羅刹と骸");
                break;
            }
            case 13: {
                event.toolTip.add("§f残弾、既に無くなった 此処で一度引き返そうか");
                break;
            }
            case 14: {
                event.toolTip.add("§f一瞬だけ忘れないでよね");
                break;
            }
            case 15: {
                event.toolTip.add("§f真夜中に告ぐ 音の警告");
                break;
            }
            case 16: {
                event.toolTip.add("§f二人きりこの儘愛し合えるさ―。");
                break;
            }
            case 17: {
                event.toolTip.add("§fフラッシュバック・蝉の声・");
                break;
            }
            case 18: {
                event.toolTip.add("§fそう 君は友達");
                break;
            }
            case 19: {
                event.toolTip.add("§fあの夜から");
                break;
            }
            case 20: {
                event.toolTip.add("§f“Gott ist tot”");
                break;
            }
            case 21: {
                event.toolTip.add("§f愛や厭 愛や厭");
                break;
            }
            case 22: {
                event.toolTip.add("§fあなたには僕が見えるか？");
                break;
            }
        }
    }

}