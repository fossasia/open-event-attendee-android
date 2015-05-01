package org.republica.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.republica.R;
import org.republica.db.DatabaseManager;
import org.republica.loaders.SimpleCursorLoader;

public class SearchResultListFragment extends SmoothListFragment {

    private static final int EVENTS_LOADER_ID = 1;
    private static final String ARG_QUERY = "query";


    public static SearchResultListFragment newInstance(String query) {
        SearchResultListFragment f = new SearchResultListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.no_search_result));
        setListShown(false);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {


    }

    private static class TextSearchLoader extends SimpleCursorLoader {

        private final String query;

        public TextSearchLoader(Context context, String query) {
            super(context);
            this.query = query;
        }

        @Override
        protected Cursor getCursor() {
            return DatabaseManager.getInstance().getSearchResults(query);
        }
    }
}
