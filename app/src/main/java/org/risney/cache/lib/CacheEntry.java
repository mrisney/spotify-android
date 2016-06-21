package org.risney.cache.lib;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by marcrisney on 6/20/16.
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

    public int getHitCount() {
        return hitCount.get();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (entryDate ^ (entryDate >>> 32));
        result = prime * result + ((hitCount == null) ? 0 : hitCount.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + (int) (lastHitDate ^ (lastHitDate >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CacheEntry other = (CacheEntry) obj;
        if (entryDate != other.entryDate)
            return false;
        if (hitCount == null) {
            if (other.hitCount != null)
                return false;
        } else if (!hitCount.equals(other.hitCount))
            return false;
        if (id != other.id)
            return false;
        if (lastHitDate != other.lastHitDate)
            return false;
        return true;
    }
}
