package org.risney.cache.lib;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface for ImageCache
 *
 * @author marc.risney@gmail.com
 * @version 1.0
 * @since 2016-06-20
 */

public interface MapCache {

    MapPutResult putIfAbsent(ByteBuffer key, ByteBuffer value) throws IOException;

    MapPutResult put(ByteBuffer key, ByteBuffer value) throws IOException;

    boolean containsKey(ByteBuffer key) throws IOException;

    ByteBuffer get(ByteBuffer key) throws IOException;

    ByteBuffer remove(ByteBuffer key) throws IOException;

    int size() throws IOException;

    int getNumberOfBytes() throws IOException;
}