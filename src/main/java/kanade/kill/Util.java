package kanade.kill;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
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
            if (entity instanceof EntityLivingBase) {
                DataParameter<Float> HEALTH = (DataParameter<Float>) Unsafe.instance.getObjectVolatile(LateFields.HEALTH_base, LateFields.HEALTH_offset);
                ((EntityDataManager) Unsafe.instance.getObjectVolatile(entity, LateFields.dataManager_offset)).set(HEALTH, 0.0f);
                if (entity instanceof EntityPlayer) {
                    if (world.playerEntities.getClass() != ArrayList.class) {
                        Unsafe.instance.putObjectVolatile(world, LateFields.playerEntities_offset, new ArrayList<>(world.playerEntities));
                    }
                    world.playerEntities.remove(entity);

                }
            }
            killing = false;
        } catch (Throwable t){
            t.printStackTrace();
            killing = false;
            throw new RuntimeException(t);
        }
    }
}
