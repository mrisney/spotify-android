package org.risney.cache.lib;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * CacheEntry is the POJO representation, with properties used in the EvictionPolicy
 * these properties are use to evaluate which CacheEntry gets evicted, when the threshold of
 * the cache is reached, if a new EvictionPolicy was created, presumably a new property would be
 * added here, so that it could be compared to its neighboring Entries in the Cache, for eviction
 * @author marc.risney@gmail.com
 * @version 1.0
 * @since 2016-06-20
 *
 */

public class CacheEntry {

    private final long id;
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0L);
    private final long entryDate;
    private final AtomicInteger hitCount = new AtomicInteger(0);

    private volatile long lastHitDate;
    private volatile int size;

    public CacheEntry() {
        entryDate = System.currentTimeMillis();
        lastHitDate = entryDate;
        id = ID_GENERATOR.getAndIncrement();
        size = 0;
    }

    public long getEntryDate() {
        return entryDate;
    }

    public long getLastHitDate() {
        return lastHitDate;
    }

    public int getHits() {
        return hitCount.intValue();
    }

    public int getHitCount(){
        return hitCount.intValue();
    }
    public void resetHits(){
        hitCount.set(0);
        lastHitDate = entryDate;
    }
    public void hit() {
        hitCount.getAndIncrement();
        lastHitDate = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
