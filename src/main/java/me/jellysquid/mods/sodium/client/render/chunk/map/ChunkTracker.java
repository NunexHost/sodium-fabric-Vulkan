package me.jellysquid.mods.sodium.client.render.chunk.map;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.util.math.ChunkPos;

public class ChunkTracker implements ClientChunkEventListener {

    private final Int2IntOpenHashMap chunkStatus = new Int2IntOpenHashMap();
    private final IntOpenHashSet chunkReady = new IntOpenHashSet();

    private final IntSet unloadQueue = new IntOpenHashSet();
    private final IntSet loadQueue = new IntOpenHashSet();

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
        long key = ChunkPos.toLong(x, z); // Use toLong to create a long key

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
        long key = ChunkPos.toLong(x, z); // Use toLong to create a long key

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
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                this.updateMerged(ox + x, oz + z);
            }
        }
    }

    private void updateMerged(int x, int z) {
        long key = ChunkPos.toLong(x, z); // Use toLong to create a long key

        int flags = this.chunkStatus.get(key);

        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                flags &= this.chunkStatus.get(ChunkPos.toLong(ox + x, oz + z));
            }
        }

        if (flags == ChunkStatus.FLAG_ALL) {
            if (this.chunkReady.add(key) && !this.unloadQueue.remove(key)) {
                this.loadQueue.add(key);
            }
        } else {
            if (this.chunkReady.remove(key) && !this.loadQueue.remove(key)) {
                this.unloadQueue.add(key);
            }
        }
    }

    public IntCollection getReadyChunks() {
        return IntSets.unmodifiable(this.chunkReady);
    }

    public void forEachEvent(ChunkEventHandler loadEventHandler, ChunkEventHandler unloadEventHandler) {
        forEachChunk(this.unloadQueue, unloadEventHandler);
        this.unloadQueue.clear();

        forEachChunk(this.loadQueue, loadEventHandler);
        this.loadQueue.clear();
    }

    public static void forEachChunk(IntCollection queue, ChunkEventHandler handler) {
        var iterator = queue.iterator();

        while (iterator.hasNext()) {
            var pos = iterator.next();

            int x = (int) (pos >> 32); // Shift by 32 bits for X coordinate
            int z = (int) pos; // Lower 32 bits for Z coordinate

            handler.apply(x, z);
        }
    }

    public interface ChunkEventHandler {
        void apply(int x, int z);
    }
}

