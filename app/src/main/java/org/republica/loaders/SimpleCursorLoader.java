package org.republica.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * A CursorLoader that doesn't need a ContentProvider.
 *
 * @author Christophe Beyls
 */
public abstract class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {
    private final ForceLoadContentObserver mObserver;

    private Cursor mCursor;

    public SimpleCursorLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        Cursor cursor = getCursor();
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
            cursor.registerContentObserver(mObserver);
        }
        return cursor;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the data. When the result is ready the callbacks will be called on the UI thread. If a previous load has been completed
     * and is still valid the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        // Retry a refresh the next time the loader is started
        onContentChanged();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    protected abstract Cursor getCursor();
}
