package org.republica.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Abhishek on 14/02/15.
 */
public class VolleySingleton {
    private static RequestQueue mQueue;
    private static ImageLoader mImageLoader;
    private static VolleySingleton mInstance = null;


    private VolleySingleton(Context mContext) {
        mQueue = Volley.newRequestQueue(mContext);
    }

    public static ImageLoader getImageLoader(Context context) {
        if (mImageLoader == null) {

            //mImageLoader = new ImageLoader(ImageUtil.getReqQueue(context), new DiskBitmapCache(context.getExternalCacheDir()));
            mImageLoader = new ImageLoader(VolleySingleton.getReqQueue(context), new BitmapLruCache(context));

        }

        return mImageLoader;

    }

    public static VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }


    public static RequestQueue getReqQueue(Context context) {
        if (mQueue == null)
            mQueue = Volley.newRequestQueue(context);


        return mQueue;
    }


}