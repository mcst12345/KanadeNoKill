package kanade.kill.asm.hooks;

import kanade.kill.Launch;
import kanade.kill.timemanagement.TimeStop;
import kanade.kill.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
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

        world.profiler.startSection("entities");
        world.profiler.startSection("global");

        for (int i = 0; i < world.weatherEffects.size(); ++i) {
            if (Util.killing) {
                return;
            }
            Entity entity = world.weatherEffects.get(i);

            try {
                if (entity.updateBlocked) continue;
                ++entity.ticksExisted;
                entity.onUpdate();
            } catch (Throwable t) {
                Launch.LOGGER.warn("Catch exception when updating entity:", t);
            }

            if (entity.HatedByLife) {
                if (Util.killing) {
                    return;
                }
                world.weatherEffects.remove(i--);
            }
        }

        world.profiler.endStartSection("remove");
        world.entities.removeAll(world.unloadedEntityList);

        for (Entity entity1 : world.unloadedEntityList) {
            if (Util.killing) {
                return;
            }
            int j = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;

            if (entity1.addedToChunk && world.isChunkLoaded(j, k1, true)) {
                if (Util.killing) {
                    return;
                }
                world.getChunk(j, k1).removeEntity(entity1);
            }
        }

        for (Entity entity : world.unloadedEntityList) {
            if (Util.killing) {
                return;
            }
            world.onEntityRemoved(entity);
        }

        world.unloadedEntityList.clear();
        world.tickPlayers();
        world.profiler.endStartSection("regular");

        for (int i1 = 0; i1 < world.entities.size(); ++i1) {
            if (Util.killing) {
                return;
            }
            Entity entity2 = world.entities.get(i1);
            try {
                Entity entity3 = entity2.getRidingEntity();

                if (entity3 != null) {
                    if (!entity3.HatedByLife && entity3.isPassenger(entity2)) {
                        continue;
                    }

                    entity2.dismountRidingEntity();
                }
            } catch (Throwable ignored) {
            }

            world.profiler.startSection("tick");

            if (!entity2.HatedByLife && !(entity2 instanceof EntityPlayerMP)) {
                try {
                    net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackStart(entity2);
                    world.updateEntity(entity2);
                    net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackEnd(entity2);
                } catch (Throwable t) {
                    Launch.LOGGER.warn("Catch exception when updating entity:", t);
                }
            }

            world.profiler.endSection();
            world.profiler.startSection("remove");

            if (entity2.HatedByLife) {
                if (Util.killing) {
                    return;
                }
                int l1 = entity2.chunkCoordX;
                int i2 = entity2.chunkCoordZ;

                if (entity2.addedToChunk && world.isChunkLoaded(l1, i2, true)) {
                    world.getChunk(l1, i2).removeEntity(entity2);
                }

                world.entities.remove(i1--);
                world.onEntityRemoved(entity2);
            }

            world.profiler.endSection();
        }

        if (TimeStop.isTimeStop()) {
            return;
        }

        world.profiler.endStartSection("blockEntities");

        world.processingLoadedTiles = true; //FML Move above remove to prevent CMEs

        if (!world.tileEntitiesToBeRemoved.isEmpty()) {
            for (TileEntity tile : world.tileEntitiesToBeRemoved) {
                tile.onChunkUnload();
            }

            // forge: faster "contains" makes world removal much more efficient
            java.util.Set<TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(world.tileEntitiesToBeRemoved);
            world.tickableTileEntities.removeAll(remove);
            world.loadedTileEntityList.removeAll(remove);
            world.tileEntitiesToBeRemoved.clear();
        }

        Iterator<TileEntity> iterator = world.tickableTileEntities.iterator();

        while (iterator.hasNext()) {
            TileEntity tileentity = iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorld()) {
                BlockPos blockpos = tileentity.getPos();

                if (world.isBlockLoaded(blockpos, false) && world.worldBorder.contains(blockpos)) //Forge: Fix TE's getting an extra tick on the client side....
                {
                    try {
                        world.profiler.func_194340_a(() ->
                                String.valueOf(TileEntity.getKey(tileentity.getClass())));
                        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
                        ((ITickable) tileentity).update();
                        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
                        world.profiler.endSection();
                    } catch (Throwable t) {
                        Launch.LOGGER.warn("Catch exception when updating tile:", t);
                    }
                }
            }

            if (tileentity.isInvalid()) {
                iterator.remove();
                world.loadedTileEntityList.remove(tileentity);

                if (world.isBlockLoaded(tileentity.getPos())) {
                    //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
                    Chunk chunk = world.getChunk(tileentity.getPos());
                    if (chunk.getTileEntity(tileentity.getPos(), net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK) == tileentity)
                        chunk.removeTileEntity(tileentity.getPos());
                }
            }
        }

        world.processingLoadedTiles = false;
        world.profiler.endStartSection("pendingBlockEntities");

        if (!world.addedTileEntityList.isEmpty()) {
            for (TileEntity tileentity1 : world.addedTileEntityList) {
                if (!tileentity1.isInvalid()) {
                    if (!world.loadedTileEntityList.contains(tileentity1)) {
                        world.addTileEntity(tileentity1);
                    }

                    if (world.isBlockLoaded(tileentity1.getPos())) {
                        Chunk chunk = world.getChunk(tileentity1.getPos());
                        IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                        chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                        world.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
                    }
                }
            }

            world.addedTileEntityList.clear();
        }

        world.profiler.endSection();
        world.profiler.endSection();
    }
}
