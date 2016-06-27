package org.risney.cache.android.model;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * POJO for Image
 *
 * @author marc.risney@gmail.com
 * @version 1.0
 * @since 2016-06-20
 */
public class Image {

    private int id;
    private String src;
    private int byteCount;
    private boolean cached;
    private ByteBuffer key;
    private ByteBuffer value;
    private Bitmap bitmap;

    public Image(int id, String src, ByteBuffer key,ByteBuffer value, Bitmap bitmap, boolean cached,int byteCount ) {
        this.id = id;
        this.src = src;
        this.key = key;
        this.value = value;
        this.bitmap = bitmap;
        this.cached = cached;
        this.byteCount = byteCount;
    }

    public int getId() {
        return id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public int getByteCount() {
        return byteCount;
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public ByteBuffer getKey() {
        return key;
    }

    public void setKey(ByteBuffer key) {
        this.key = key;
    }

    public ByteBuffer getValue() {
        return value;
    }

    public void setValue(ByteBuffer value) {
        this.value = value;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}

