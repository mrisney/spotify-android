package org.risney.cache.android;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * Created by marcrisney on 6/22/16.
 */
public class Image {

    private String src;
    private int size;
    private boolean cached;
    private ByteBuffer key;
    private Bitmap bitmap;

    public Image(String src, ByteBuffer key, Bitmap bitmap) {
        this.src = src;
        this.key = key;
        this.bitmap = bitmap;
    }

    public String getSrc() {
        return src;
    }
    public void setSrc(String src) {
        this.src = src;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
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

    public Bitmap getBitmap() {
        return bitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}

