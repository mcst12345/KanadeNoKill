package kanade.kill;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import scala.concurrent.util.Unsafe;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static boolean killing;
    public static void Kill(List<Entity> list) {
        for (Entity e : list) {
            Kill(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized void Kill(Entity entity) {
        try {
            killing = true;
            if (entity == null || entity.world.isRemote) return;
            World world = entity.world;
            if (world.loadedEntityList.getClass() != ArrayList.class) {
                Unsafe.instance.putObjectVolatile(world, LateFields.loadedEntityList_offset, new ArrayList<>(world.loadedEntityList));
            }
            world.loadedEntityList.remove(entity);
            Chunk chunk = world.getChunk(entity.chunkCoordX, entity.chunkCoordZ);
            ClassInheritanceMultiMap<Entity>[] entityLists = (ClassInheritanceMultiMap<Entity>[]) Unsafe.instance.getObjectVolatile(chunk, LateFields.entityLists_offset);
            for (ClassInheritanceMultiMap<Entity> map : entityLists) {
                map.remove(entity);
            }
            chunk.markDirty();

            entity.isDead = true;
            entity.addedToChunk = false;
            entity.dimension = 114514;
            Unsafe.instance.putObjectVolatile(entity, LateFields.dataManager_offset, FakeEntityDataManager.instance);
            killing = false;
        } catch (Throwable t){
            t.printStackTrace();
            killing = false;
            throw new RuntimeException(t);
        }
    }
}
