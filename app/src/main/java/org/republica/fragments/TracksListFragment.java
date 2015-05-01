package org.republica.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.republica.R;
import org.republica.activities.TrackActivity;
import org.republica.db.DatabaseHelper;
import org.republica.db.DatabaseManager;


/**
 * Created by Abhishek on 01/03/15.
 */
public class TracksListFragment extends SmoothListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] columns = new String[]{DatabaseHelper.TABLE_COLUMN_NAME, DatabaseHelper.TABLE_COLOUMN_INFORMATION};
        // THE XML DEFINED VIEWS WHICH THE DATA WILL BE BOUND TO
        int[] to = new int[]{R.id.textView_track_title, R.id.textView_track_information};
        DatabaseManager db = DatabaseManager.getInstance();
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_tracks, db.getTracks(), columns, to, CursorAdapter.NO_SELECTION);
        setListAdapter(mAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String text = ((TextView) v.findViewById(R.id.textView_track_title)).getText().toString();
        Intent intent = new Intent(getActivity(), TrackActivity.class);
        intent.putExtra("TRACK", text);
        startActivity(intent);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("No data present");
    }
}
