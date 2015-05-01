package org.republica.widgets;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;

import org.republica.R;
import org.republica.db.DatabaseManager;

/**
 * Context menu for the bookmarks list items, available for API 11+ only.
 *
 * @author Christophe Beyls
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BookmarksMultiChoiceModeListener implements MultiChoiceModeListener {

    private AbsListView listView;

    private BookmarksMultiChoiceModeListener(AbsListView listView) {
        this.listView = listView;
    }

    public static void register(AbsListView listView) {
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        BookmarksMultiChoiceModeListener listener = new BookmarksMultiChoiceModeListener(listView);
        listView.setMultiChoiceModeListener(listener);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        updateSelectedCountDisplay(mode);
        return true;
    }

    private void updateSelectedCountDisplay(ActionMode mode) {
        int count = listView.getCheckedItemCount();
        mode.setTitle(listView.getContext().getResources().getQuantityString(R.plurals.selected, count, count));
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_mode_bookmarks, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                // Remove multiple bookmarks at once
                new RemoveBookmarksAsyncTask().execute(listView.getCheckedItemIds());
                mode.finish();
                return true;
        }
        return false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        updateSelectedCountDisplay(mode);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    private static class RemoveBookmarksAsyncTask extends AsyncTask<long[], Void, Void> {

        @Override
        protected Void doInBackground(long[]... params) {
            DatabaseManager.getInstance().removeBookmarks(params[0]);
            return null;
        }

    }
}
