package org.risney.cache.lib;

import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.risney.cache.lib.policies.EvictionPolicy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ImageCache implements MapCache to provide most of the methods of a HashMap, as a container to cache
 * in memory strong references to Key,Value sets - comprised of ByteBuffers.
 * Using the Builder Pattern, configuring the cache requires an EvictionPolicy, maxImages and maxBytes are optional
 * as there are defaults of
 * <b>DEFAULT_MAX_IMAGES : 33</b> and
 * <b>DEFAULT_MAX_BYTES : 1024 * 1024 (1 megabyte)</b>
 * <p/>
 *
 * <code>
 *
 *      // set the initial size to 5 images, and 50KB
 *      final int MAX_IMAGES = 5;
 *      final int MAX_BYTES = 50000;
 *
 *      // uses builder pattern
 *      ImageCache imageCache = new ImageCache.Builder(EvictionPolicy.LRU)
 *                                             .maxBytes(MAX_BYTES)
 *                                             .maxImages(MAX_IMAGES)
 *                                             .build();
 *
 *
 *      String testFile = "src/test/resources/test100k.db";
 *      ByteBuffer testBytesValue = ConversionUtils.readToBuffer(testFile);
 *
 *      ByteBuffer keyOne = ConversionUtils.stringToByteBuffer("one");
 *      ByteBuffer keyTwo = ConversionUtils.stringToByteBuffer("two");
 *      imageCache.put(keyOne, ByteBuffer.wrap(bytes));
 *      imageCache.put(keyTwo, ByteBuffer.wrap(bytes));
 *      ...
 *
 * </code>
 * The standard "put" and "get" methods are available. "putIfAbsent" is present, as it allows a hitcount to be recorded
 * on the CacheEntry. for Least Recently Used LRU - Cache Eviction, that is a factor in determing which CacheEntry should be
 * evicted in event of threshold breach.
 *
 * @author marc.risney@gmail.com
 * @version 1.0
 * @since 2016-06-20
 */

public class ImageCache implements MapCache {

    private static final String TAG = ImageCache.class.getSimpleName();
    private static final int DEFAULT_MAX_IMAGES = 50;

    // default to 1 megabyte
    private static final int DEFAULT_MAX_BYTES = 1024 * 1024;

    private final EvictionPolicy evictionPolicy; // required
    private final Map<ByteBuffer, MapCacheEntry> cache;
    private final SortedMap<MapCacheEntry, ByteBuffer> inverseCacheMap;
    private final ReadWriteLock rwLock;
    private final Lock readLock;
    private final Lock writeLock;
    protected int maxImages = DEFAULT_MAX_IMAGES; // optional
    protected int maxBytes = DEFAULT_MAX_BYTES; // optional

    /**
     * The total size of bytes stored in cache.
     */
    private long totalCacheSize;

    /**
     * This Class uses a builder pattern to create a cache, a HashMap, with limits on number of entries, and number iof bytes.
     * On creation, an eviction policy is assigned, and if subsequent entries are placed into cache, the algorithm chooses which cache entry to evict
     * class method.
     */

    private ImageCache(Builder builder) {
        this.evictionPolicy = builder.evictionPolicy;
        this.maxImages = builder.maxImages;
        this.maxBytes = builder.maxBytes;
        this.cache = new HashMap();
        this.rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();
        this.inverseCacheMap = new ConcurrentSkipListMap(evictionPolicy.getComparator());
        this.totalCacheSize = 0;

    }

    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    public int getDefaultMaxImages() {
        return DEFAULT_MAX_IMAGES;
    }

    public int getDefaultMaxBytes() {
        return DEFAULT_MAX_BYTES;
    }

    public int getMaxImages() {
        return maxImages;
    }

    public int getMaxBytes() {
        return maxBytes;
    }

    /**
     * The total size of bytes stored in cache.
     */
    public long getTotalCacheSize() {
        return totalCacheSize;
    }

    public synchronized long getEntryCount() {
        return cache.entrySet().size();
    }

    public synchronized void clear() {
        cache.clear();
        totalCacheSize = 0;
    }

    /**
     * @return MapPutResult private method that uses the comparators, size and number of bytes to evaluate whether to
     * evict an entry. This is essentially the secret sauce.
     * current cache size, and number of entries are updated accordingly.
     */

    private MapCacheEntry evict() {

        Log.d(TAG, "Current bytes in cache :  " + FileUtils.byteCountToDisplaySize(totalCacheSize));
        Log.d(TAG, "Number of images in cache :  " + cache.size());

        if ((cache.size() <= maxImages) && (totalCacheSize < maxBytes)) {
            Log.d(TAG, "No eviction");
            return null;
        }

        final MapCacheEntry entryToEvict = inverseCacheMap.firstKey();
        final ByteBuffer valueToEvict = inverseCacheMap.remove(entryToEvict);
        totalCacheSize -= cache.get(valueToEvict).getValue().capacity();
        cache.remove(valueToEvict);

        Log.d(TAG, "Evicting entry with id " + entryToEvict.getId() + " from cache");
        Log.d(TAG, "Number of images now in cache : " + cache.size());
        Log.d(TAG, "Bytes in cache : " + FileUtils.byteCountToDisplaySize(totalCacheSize));

        return entryToEvict;
    }

    /**
     * @return MapPutResult put a Key/Value of ByteBuffer into cache, if present, overrides current value.
     * hit value is recorded, and size is recorded.
     */

    @Override
    public MapPutResult putIfAbsent(final ByteBuffer key, final ByteBuffer value) {
        writeLock.lock();
        try {
            final MapCacheEntry entry = cache.get(key);
            if (entry == null) {
                // Entry is null. Adding ...
                final MapCacheEntry evicted = evict();
                final MapCacheEntry newEntry = new MapCacheEntry(key, value);
                cache.put(key, newEntry);
                inverseCacheMap.put(newEntry, key);

                // Set the size
                totalCacheSize += value.capacity();
                newEntry.setSize(value.capacity());

                if (evicted == null) {
                    return new MapPutResult(true, key, value, null, null, null);
                } else {
                    return new MapPutResult(true, key, value, null, evicted.getKey(), evicted.getValue());
                }
            }

            // Entry is not null. Increment hit count and return result
            // indicating that entry was not added.
            inverseCacheMap.remove(entry);
            entry.hit();

            inverseCacheMap.put(entry, key);

            return new MapPutResult(false, key, value, entry.getValue(), null, null);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @return MapPutResult put a Key/Value of ByteBuffer into cache, if present, overrides current value.
     * No hit value is recorded
     */

    @Override
    public MapPutResult put(final ByteBuffer key, final ByteBuffer value) {

        writeLock.lock();
        try {

            String fileSize = FileUtils.byteCountToDisplaySize(value.capacity());
            Log.d(TAG, "Size of entry put into cache " + fileSize);
            // evict if we need to in order to make room for a new entry.

            final MapCacheEntry entry = new MapCacheEntry(key, value);
            final MapCacheEntry existing = cache.put(key, entry);

            // Set the size
            totalCacheSize += value.capacity();
            entry.setSize(key.capacity() + value.capacity());

            final MapCacheEntry evicted = evict();
            inverseCacheMap.put(entry, key);

            final ByteBuffer existingValue = (existing == null) ? null : existing.getValue();
            final ByteBuffer evictedKey = (evicted == null) ? null : evicted.getKey();
            final ByteBuffer evictedValue = (evicted == null) ? null : evicted.getValue();



            return new MapPutResult(true, key, value, existingValue, evictedKey, evictedValue);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @return boolean check to see if cache contains a key (ByteBuffer).
     */

    @Override
    public boolean containsKey(final ByteBuffer key) {
        readLock.lock();
        try {
            final MapCacheEntry entry = cache.get(key);
            if (entry == null) {
                return false;
            }

            inverseCacheMap.remove(entry);
            entry.hit();
            inverseCacheMap.put(entry, key);

            return true;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return get a ByteBuffer value from  ByteBuffer key.
     */

    @Override
    public ByteBuffer get(final ByteBuffer key) {
        readLock.lock();
        try {
            final MapCacheEntry entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            inverseCacheMap.remove(entry);
            entry.hit();
            inverseCacheMap.put(entry, key);

            return entry.getValue();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return remove a ByteBuffer value from  ByteBuffer key.
     */
    @Override
    public ByteBuffer remove(ByteBuffer key) throws IOException {
        writeLock.lock();
        try {
            final MapCacheEntry entry = cache.remove(key);

            if (entry == null) {
                return null;
            }
            inverseCacheMap.remove(entry);
            // Set the size
            totalCacheSize -= entry.getValue().capacity();
            return entry.getValue();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * This is the builder method for the optional max number of images and max byte size.
     */
    public static class Builder {
        private final EvictionPolicy evictionPolicy;
        protected int maxImages;
        protected int maxBytes;

        public Builder(EvictionPolicy evictionPolicy) {
            this.evictionPolicy = evictionPolicy;
            this.maxImages = DEFAULT_MAX_IMAGES;
            this.maxBytes = DEFAULT_MAX_BYTES;
        }

        public Builder maxImages(int maxImages) {
            this.maxImages = maxImages;
            return this;
        }

        public Builder maxBytes(int maxBytes) {
            this.maxBytes = maxBytes;
            return this;
        }

        public ImageCache build() {
            return new ImageCache(this);
        }
    }
}