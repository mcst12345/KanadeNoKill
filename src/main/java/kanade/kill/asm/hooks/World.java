package kanade.kill.asm.hooks;

import kanade.kill.Launch;
import kanade.kill.timemanagement.TimeStop;
import kanade.kill.util.EntityUtil;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Iterator;

public class World {
    public static void UpdateEntities(net.minecraft.world.World world) {
        if (Util.killing && (Launch.client && Minecraft.dead)) {
            return;
        }

        if (world.entities.getClass() != ArrayList.class)
            world.entities = new ArrayList<>(world.entities);
        for (Entity entity : world.protects) {
            if (!world.entities.contains(entity))
                world.entities.add(entity);
        }

        world.entities.removeIf(EntityUtil::isDead);

        world.theProfiler.startSection("entities");
        world.theProfiler.startSection("global");
        int i;
        net.minecraft.entity.Entity entity;

        for (i = 0; i < world.weatherEffects.size(); ++i) {
            entity = (net.minecraft.entity.Entity) world.weatherEffects.get(i);

            try {
                ++entity.ticksExisted;
                entity.onUpdate();
            } catch (Throwable t) {
                Launch.LOGGER.warn("Catch exception when updating entity:", t);
            }

            if (entity.HatedByLife) {
                world.weatherEffects.remove(i--);
            }
        }

        world.theProfiler.endStartSection("remove");
        world.entities.removeAll(world.unloadedEntityList);
        int j;
        int l;

        for (i = 0; i < world.unloadedEntityList.size(); ++i) {
            entity = (net.minecraft.entity.Entity) world.unloadedEntityList.get(i);
            j = entity.chunkCoordX;
            l = entity.chunkCoordZ;

            if (entity.addedToChunk && world.chunkExists(j, l)) {
                world.getChunkFromChunkCoords(j, l).removeEntity(entity);
            }
        }

        for (i = 0; i < world.unloadedEntityList.size(); ++i) {
            world.onEntityRemoved((net.minecraft.entity.Entity) world.unloadedEntityList.get(i));
        }

        world.unloadedEntityList.clear();
        world.theProfiler.endStartSection("regular");

        for (i = 0; i < world.entities.size(); ++i) {
            entity = world.entities.get(i);

            if (entity.ridingEntity != null) {
                if (!entity.ridingEntity.HatedByLife && entity.ridingEntity.riddenByEntity == entity) {
                    continue;
                }

                entity.ridingEntity.riddenByEntity = null;
                entity.ridingEntity = null;
            }

            world.theProfiler.startSection("tick");

            if (!entity.HatedByLife) {
                try {
                    world.updateEntity(entity);
                } catch (Throwable t) {
                    Launch.LOGGER.warn("Catch exception when updating entity:", t);
                }
            }

            world.theProfiler.endSection();
            world.theProfiler.startSection("remove");

            if (entity.HatedByLife) {
                j = entity.chunkCoordX;
                l = entity.chunkCoordZ;

                if (entity.addedToChunk && world.chunkExists(j, l)) {
                    world.getChunkFromChunkCoords(j, l).removeEntity(entity);
                }

                world.entities.remove(i--);
                world.onEntityRemoved(entity);
            }

            world.theProfiler.endSection();
        }

        if (TimeStop.isTimeStop()) {
            return;
        }

        world.theProfiler.endStartSection("blockEntities");
        world.field_147481_N = true;
        Iterator<?> iterator = world.loadedTileEntityList.iterator();

        while (iterator.hasNext()) {
            TileEntity tileentity = (TileEntity) iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorldObj() && world.blockExists(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord)) {
                try {
                    tileentity.updateEntity();
                } catch (Throwable t) {
                    Launch.LOGGER.warn("Catch exception when updating tile:", t);
                }
            }

            if (tileentity.isInvalid()) {
                iterator.remove();

                if (world.chunkExists(tileentity.xCoord >> 4, tileentity.zCoord >> 4)) {
                    Chunk chunk = world.getChunkFromChunkCoords(tileentity.xCoord >> 4, tileentity.zCoord >> 4);

                    if (chunk != null) {
                        chunk.removeInvalidTileEntity(tileentity.xCoord & 15, tileentity.yCoord, tileentity.zCoord & 15);
                    }
                }
            }
        }

        if (!world.field_147483_b.isEmpty()) {
            for (Object tile : world.field_147483_b) {
                ((TileEntity) tile).onChunkUnload();
            }
            world.loadedTileEntityList.removeAll(world.field_147483_b);
            world.field_147483_b.clear();
        }

        world.field_147481_N = false;

        world.theProfiler.endStartSection("pendingBlockEntities");

        if (!world.addedTileEntityList.isEmpty()) {
            for (int k = 0; k < world.addedTileEntityList.size(); ++k) {
                TileEntity tileentity1 = (TileEntity) world.addedTileEntityList.get(k);

                if (!tileentity1.isInvalid()) {
                    if (!world.loadedTileEntityList.contains(tileentity1)) {
                        world.loadedTileEntityList.add(tileentity1);
                    }
                } else {
                    if (world.chunkExists(tileentity1.xCoord >> 4, tileentity1.zCoord >> 4)) {
                        Chunk chunk1 = world.getChunkFromChunkCoords(tileentity1.xCoord >> 4, tileentity1.zCoord >> 4);

                        if (chunk1 != null) {
                            chunk1.removeInvalidTileEntity(tileentity1.xCoord & 15, tileentity1.yCoord, tileentity1.zCoord & 15);
                        }
                    }
                }
            }

            world.addedTileEntityList.clear();
        }

        world.theProfiler.endSection();
        world.theProfiler.endSection();
    }
}
