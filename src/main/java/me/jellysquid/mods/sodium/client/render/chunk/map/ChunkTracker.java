
package me.jellysquid.mods.sodium.client.render.chunk.map;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.util.math.ChunkPos;

public class ChunkTracker implements ClientChunkEventListener {

private final LongBitSet chunkStatus = new LongBitSet();

private final LongSet unloadQueue = new LongOpenHashSet<>();
private final LongSet loadQueue = new LongOpenHashSet<>();

public ChunkTracker() {

}

@Override
public void updateMapCenter(int chunkX, int chunkZ) {

}

@Override
public void updateLoadDistance(int loadDistance) {

}

@Override
public void onChunkStatusAdded(int x, int z, int flags) {
    var key = ChunkPos.toLong(x, z);

    chunkStatus.set(key, flags);

    updateNeighbors(x, z);
}

@Override
public void onChunkStatusRemoved(int x, int z, int flags) {
    var key = ChunkPos.toLong(x, z);

    chunkStatus.clear(key, flags);

    updateNeighbors(x, z);
}

private void updateNeighbors(int x, int z) {
    for (int ox = -1; ox <= 1; ox++) {
        for (int oz = -1; oz <= 1; oz++) {
            updateMerged(ox + x, oz + z);
        }
    }
}

private void updateMerged(int x, int z) {
    var key = ChunkPos.toLong(x, z);

    var flags = chunkStatus.get(key);

    for (int ox = -1; ox <= 1; ox++) {
        for (int oz = -1; oz <= 1; oz++) {
            flags &= chunkStatus.get(ChunkPos.toLong(ox + x, oz + z));
        }
    }

    if (flags == ChunkStatus.FLAG_ALL) {
        if (!loadQueue.contains(key)) {
            loadQueue.add(key);
        }
    } else {
        if (unloadQueue.contains(key)) {
            unloadQueue.remove(key);
        }
    }
}

public LongCollection getReadyChunks() {
    return chunkStatus.getSet();
}

public void forEachEvent(ChunkEventHandler loadEventHandler, ChunkEventHandler unloadEventHandler) {
    forEachChunk(loadQueue, loadEventHandler);
    forEachChunk(unloadQueue, unloadEventHandler);

    loadQueue.clear();
    unloadQueue.clear();
}

public static void forEachChunk(LongCollection queue, ChunkEventHandler handler) {
    queue.forEach(handler);
}

public interface ChunkEventHandler {
    void apply(int x, int z);
}
}
