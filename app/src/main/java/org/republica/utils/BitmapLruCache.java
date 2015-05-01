package org.republica.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * Created by Abhishek on 14/02/15.
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements
        ImageCache {
    public BitmapLruCache(Context context) {
        this(getDefaultLruCacheSize(context), context);
    }

    public BitmapLruCache(int sizeInKiloBytes, Context context) {
        super(sizeInKiloBytes);
    }

    public static int getDefaultLruCacheSize(Context context) {

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() /
                1024);
        final int cacheSize = maxMemory / 2;

        return cacheSize;

    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}
