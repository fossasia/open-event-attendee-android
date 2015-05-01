package org.republica.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.republica.R;

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

}
