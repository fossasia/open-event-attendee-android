package org.republica.fragments;

/**
 * Created by Abhishek on 01/03/15.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.republica.activities.PersonInfoActivity;
import org.republica.adapters.SpeakerAdapter;
import org.republica.db.DatabaseManager;
import org.republica.model.Speaker;

public class SpeakerFragment extends SmoothListFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseManager dbManager = DatabaseManager.getInstance();
        SpeakerAdapter adapter = new SpeakerAdapter(getActivity().getApplicationContext(), dbManager.getSpeakers(false));
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Speaker speaker = (Speaker) v.getTag();
        Intent intent = new Intent(getActivity().getApplicationContext(), PersonInfoActivity.class);
        intent.putExtra(PersonInfoActivity.SPEAKER, speaker);
        startActivity(intent);
    }
}


