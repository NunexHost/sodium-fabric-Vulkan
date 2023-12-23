package me.jellysquid.mods.sodium.client.render.chunk.map;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.util.math.ChunkPos;

public class ChunkTracker implements ClientChunkEventListener {

    private static final int MAX_CHUNK_STATUS_FLAGS = ChunkStatus.FLAG_ALL - 1;

    private final Long2IntOpenHashMap chunkStatus = new Long2IntOpenHashMap();
    private final LongOpenHashSet chunkReady = new LongOpenHashSet();

    private final LongSet unloadQueue = new LongOpenHashSet();
    private final LongSet loadQueue = new LongOpenHashSet();

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

        var prev = this.chunkStatus.get(key);
        var cur = prev | flags;

        if (prev == cur) {
            return;
        }

        this.chunkStatus.put(key, cur);

        this.updateNeighbors(x, z);
    }

    @Override
    public void onChunkStatusRemoved(int x, int z, int flags) {
        var key = ChunkPos.toLong(x, z);

        var prev = this.chunkStatus.get(key);
        int cur = prev & ~flags;

        if (prev == cur) {
            return;
        }

        if (cur == this.chunkStatus.defaultReturnValue()) {
            this.chunkStatus.remove(key);
        } else {
            this.chunkStatus.put(key, cur);
        }

        this.updateNeighbors(x, z);
    }

    private void updateNeighbors(int x, int z) {
        var neighbors = new LongOpenHashSet();

        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                if (ox != 0 || oz != 0) {
                    neighbors.add(ChunkPos.toLong(x + ox, z + oz));
                }
            }
        }

        for (var neighbor : neighbors) {
            this.updateMerged(ChunkPos.getPackedX(neighbor), ChunkPos.getPackedZ(neighbor));
        }
    }

    private void updateMerged(int x, int z) {
        var key = ChunkPos.toLong(x, z);

        int flags = this.chunkStatus.get(key);

        for (var neighbor : this.getNeighbors(x, z)) {
            flags &= this.chunkStatus.get(neighbor);
        }

        if (flags == MAX_CHUNK_STATUS_FLAGS) {
            if (this.chunkReady.add(key) && !this.unloadQueue.remove(key)) {
                this.loadQueue.add(key);
            }
        } else {
            if (this.chunkReady.remove(key) && !this.loadQueue.remove(key)) {
                this.unloadQueue.add(key);
            }
        }
    }

    public LongCollection getReadyChunks() {
        return LongSets.unmodifiable(this.chunkReady);
    }

    public void forEachEvent(ChunkEventHandler loadEventHandler, ChunkEventHandler unloadEventHandler) {
        forEachChunk(this.unloadQueue, unloadEventHandler);
        this.unloadQueue.clear();

        forEachChunk(this.loadQueue, loadEventHandler);
        this.loadQueue.clear();
    }

    private void forEachChunk(LongCollection queue, ChunkEventHandler handler) {
        var iterator = queue.iterator();

        while (iterator.hasNext()) {
            var pos = iterator.nextLong();

            var x = ChunkPos.getPackedX(pos);
            var z = ChunkPos.getPackedZ(pos);

            handler.apply(x, z);
        }
    }

    public static void forEachChunk(LongCollection queue, ChunkEventHandler handler) {
        var iterator = queue.iterator();

        while (iterator.hasNext()) {
            var pos = iterator.nextLong();

            var x = ChunkPos.getPackedX(pos);
            var z = ChunkPos.getPackedZ(pos);

            handler.apply(x, z);
        }
    }

    public interface ChunkEventHandler {
        void apply(int x, int z);
    }
} 
