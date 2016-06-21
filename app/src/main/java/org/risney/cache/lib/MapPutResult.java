package org.risney.cache.lib;


import java.nio.ByteBuffer;

/**
 * MapPutResult - used during Eviction process in ImageCache, while using
 * java.util.concurrent.locks.ReadWriteLock and java.util.concurrent.locks.ReentrantReadWriteLock;
 * to determine whether or not to maintain presence in Cache.
 * <p>
 *     A read-write lock allows for a greater level of concurrency in accessing shared data than that permitted by a mutual exclusion lock.
 *     Drawn to this pattern, as it seems the cleanest use of concurrency, and The read lock may be held simultaneously by multiple reader threads, so long as there are no writers.
 *     The write lock is exclusive. Kind of like a Singleton design, but cleaner, and easier to mainatin code.
 * </p>
 *
 * @author marc.risney@gmail.com
 * @version 1.0
 * @since 2016-06-20
 */
public class MapPutResult {

    private final boolean successful;
    private final ByteBuffer key, value;
    private final ByteBuffer existingValue;
    private final ByteBuffer evictedKey, evictedValue;

    public MapPutResult(final boolean successful, final ByteBuffer key, final ByteBuffer value,
                        final ByteBuffer existingValue, final ByteBuffer evictedKey, final ByteBuffer evictedValue) {
        this.successful = successful;
        this.key = key;
        this.value = value;
        this.existingValue = existingValue;
        this.evictedKey = evictedKey;
        this.evictedValue = evictedValue;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public ByteBuffer getKey() {
        return key;
    }

    public ByteBuffer getValue() {
        return value;
    }

    public ByteBuffer getExistingValue() {
        return existingValue;
    }

    public ByteBuffer getEvictedKey() {
        return evictedKey;
    }

    public ByteBuffer getEvictedValue() {
        return evictedValue;
    }
}