package kanade.kill.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import kanade.kill.Launch;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.ClientReloadChunk;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public class ChunkUtil {
    public static Chunk[] getAllLoadedChunks(World world) {
        IChunkProvider provider = world.chunkProvider;
        Long2ObjectMap<Chunk> loadedChunks;
        if (Launch.client && provider instanceof ChunkProviderClient) {
            loadedChunks = ((ChunkProviderClient) provider).loadedChunks;
        } else {
            loadedChunks = ((ChunkProviderServer) provider).loadedChunks;
        }
        return loadedChunks.values().toArray(new Chunk[0]);
    }

    public static void reloadChunk(World world, Chunk chunk) {
        long pos = ChunkPos.asLong(chunk.x, chunk.z);
        IChunkProvider provider = world.chunkProvider;
        if (provider instanceof ChunkProviderServer) {
            Chunk neo = ((ChunkProviderServer) world.chunkProvider).chunkGenerator.generateChunk(chunk.x, chunk.z);
            ((ChunkProviderServer) provider).loadedChunks.put(pos, chunk);
            neo.onLoad();
            neo.populate(provider, ((ChunkProviderServer) provider).chunkGenerator);
            NetworkHandler.INSTANCE.sendMessageToAllPlayer(new ClientReloadChunk(chunk.x, chunk.z));
        }

    }
}
