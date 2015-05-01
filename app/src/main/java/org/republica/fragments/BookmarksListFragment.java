package org.republica.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.republica.R;
import org.republica.activities.EventDetailsActivity;
import org.republica.adapters.ScheduleAdapter;
import org.republica.db.DatabaseManager;
import org.republica.model.FossasiaEvent;
import org.republica.widgets.BookmarksMultiChoiceModeListener;

import java.util.ArrayList;

/**
 * Bookmarks list, optionally filterable.
 *
 * @author Christophe Beyls
 */
public class BookmarksListFragment extends SmoothListFragment {

    private static final String PREF_UPCOMING_ONLY = "bookmarks_upcoming_only";

    private boolean upcomingOnly;

    private MenuItem filterMenuItem;
    private MenuItem upcomingOnlyMenuItem;
    private ArrayList<FossasiaEvent> events;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseManager db = DatabaseManager.getInstance();
        setListAdapter(new ScheduleAdapter(getActivity(), events = db.getBookmarkEvents()));

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            BookmarksMultiChoiceModeListener.register(getListView());
        }

        setEmptyText(getString(R.string.no_bookmark));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bookmarks, menu);
        filterMenuItem = menu.findItem(R.id.filter);
        upcomingOnlyMenuItem = menu.findItem(R.id.upcoming_only);
        updateOptionsMenu();
    }

    private void updateOptionsMenu() {
        if (filterMenuItem != null) {
            filterMenuItem.setIcon(upcomingOnly ?
                    R.drawable.ic_filter_list_selected_white_24dp
                    : R.drawable.ic_filter_list_white_24dp);
            upcomingOnlyMenuItem.setChecked(upcomingOnly);
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        filterMenuItem = null;
        upcomingOnlyMenuItem = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upcoming_only:
                upcomingOnly = !upcomingOnly;
                updateOptionsMenu();
                getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(PREF_UPCOMING_ONLY, upcomingOnly).commit();
                return true;
        }
        return false;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        long idNew = (long) v.getTag();
        Toast.makeText(getActivity(), "Position: " + idNew, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity().getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("event", events.get(position));
        startActivity(intent);
        super.onListItemClick(l, v, position, id);
    }


}
