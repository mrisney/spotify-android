package org.risney.cache.lib;

/**
 * Created by marcrisney on 6/20/16.
 */
import java.nio.ByteBuffer;

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