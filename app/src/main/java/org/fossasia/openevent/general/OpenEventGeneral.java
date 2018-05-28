package org.fossasia.openevent.general;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by harsimar on 27/05/18.
 */

public class OpenEventGeneral extends Application{
    private static WeakReference<Context> context;
    public static Context getAppContext() {
        return context.get();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        context = new WeakReference<>(getApplicationContext());
        Timber.plant(new Timber.DebugTree());
    }
}
