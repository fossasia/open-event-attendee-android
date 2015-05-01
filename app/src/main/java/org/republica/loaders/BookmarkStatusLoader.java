package org.republica.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import org.republica.db.DatabaseManager;
import org.republica.model.FossasiaEvent;
import org.republica.utils.ArrayUtils;


/**
 * This loader retrieves the bookmark status of an event from the database, then updates it in real time by listening to broadcasts.
 *
 * @author Christophe Beyls
 */
public class BookmarkStatusLoader extends AsyncTaskLoader<Boolean> {

    private final FossasiaEvent event;
    private final BroadcastReceiver addBookmarkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ((long) event.getId() == intent.getLongExtra(DatabaseManager.EXTRA_EVENT_ID, -1L)) {
                updateBookmark(true);
            }
        }
    };
    private final BroadcastReceiver removeBookmarksReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            long[] eventIds = intent.getLongArrayExtra(DatabaseManager.EXTRA_EVENT_IDS);
            if (ArrayUtils.indexOf(eventIds, event.getId()) != -1) {
                updateBookmark(false);
            }
        }
    };
    private Boolean isBookmarked;

    public BookmarkStatusLoader(Context context, FossasiaEvent event) {
        super(context);
        this.event = event;

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.registerReceiver(addBookmarkReceiver, new IntentFilter(DatabaseManager.ACTION_ADD_BOOKMARK));
        lbm.registerReceiver(removeBookmarksReceiver, new IntentFilter(DatabaseManager.ACTION_REMOVE_BOOKMARKS));
    }

    private void updateBookmark(Boolean result) {
        if (isStarted()) {
            cancelLoad();
        }
        deliverResult(result);
    }

    @Override
    protected void onStartLoading() {
        if (isBookmarked != null) {
            // If we currently have a result available, deliver it
            // immediately.
            super.deliverResult(isBookmarked);
        } else {
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
        isBookmarked = null;

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(addBookmarkReceiver);
        lbm.unregisterReceiver(removeBookmarksReceiver);
    }

    @Override
    public void deliverResult(Boolean data) {
        isBookmarked = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }
    }

    @Override
    public Boolean loadInBackground() {
        return DatabaseManager.getInstance().isBookmarked(event);
    }
}
