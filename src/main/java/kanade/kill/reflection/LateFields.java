package kanade.kill.reflection;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import kanade.kill.Launch;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;

public class LateFields {
    public static final long loadedEntityList_offset;
    public static final long entityLists_offset;
    public static final long dataManager_offset;
    public static final long playerEntities_offset;
    public static final long HatedByLife_offset;
    public static final long Event_Bus_offset;
    public static final Object Event_Bus_base;
    public static final long listeners_offset;
    public static final long listenerOwners_offset;
    public static final long modClassLoader_offset;
    public static final Object listeners_base;
    public static final long listeners_offset_2;
    public static final long currentScreen_offset;
    public static final long entities_offset;

    static {
        try {
            Field field = ReflectionUtil.getField(World.class, "field_72996_f");
            loadedEntityList_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(World.class, "field_73010_i");
            playerEntities_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Chunk.class, "field_76645_j");
            entityLists_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Chunk.class, "entities");
            entities_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Entity.class, "field_70180_af");
            dataManager_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Entity.class, "HatedByLife");
            HatedByLife_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(MinecraftForge.class, "Event_bus");
            Event_Bus_offset = Unsafe.instance.staticFieldOffset(field);
            Event_Bus_base = Unsafe.instance.staticFieldBase(field);
            field = ReflectionUtil.getField(EventBus.class, "listeners");
            listeners_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(EventBus.class, "listenerOwners");
            listenerOwners_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Loader.class, "modClassLoader");
            modClassLoader_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Event.class, "listeners");
            listeners_base = Unsafe.instance.staticFieldBase(field);
            listeners_offset_2 = Unsafe.instance.staticFieldOffset(field);
            if (Launch.client) {
                field = ReflectionUtil.getField(Minecraft.class, "field_71462_r");
                currentScreen_offset = Unsafe.instance.objectFieldOffset(field);
            } else {
                currentScreen_offset = -1;
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
