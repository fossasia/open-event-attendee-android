package org.republica.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class LocalCacheLoader<T> extends AsyncTaskLoader<T> {

    private T mResult;

    public LocalCacheLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mResult);
        }

        if (takeContentChanged() || mResult == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        mResult = null;
    }

    @Override
    public void deliverResult(T data) {
        mResult = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }
    }
}
