package org.risney.cache.lib;

import java.nio.ByteBuffer;

public class MapCacheEntry extends CacheEntry {

    private final ByteBuffer key;
    private final ByteBuffer value;

    public MapCacheEntry(final ByteBuffer key, final ByteBuffer value) {
        this.key = key;
        this.value = value;
    }

    public ByteBuffer getKey() {
        return key;
    }

    public ByteBuffer getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return 2938476 + key.hashCode() * value.hashCode();

    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof MapCacheEntry) {
            final MapCacheEntry that = ((MapCacheEntry) obj);
            return key.equals(that.key) && value.equals(that.value);
        }

        return false;
    }
}

