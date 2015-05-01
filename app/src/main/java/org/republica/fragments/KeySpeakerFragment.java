package org.republica.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.republica.activities.PersonInfoActivity;
import org.republica.adapters.SpeakerAdapter;
import org.republica.db.DatabaseManager;
import org.republica.model.Speaker;

/**
 * Created by Abhishek on 14/02/15.
 */
public class KeySpeakerFragment extends SmoothListFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseManager dbManager = DatabaseManager.getInstance();
        SpeakerAdapter adapter = new SpeakerAdapter(getActivity().getApplicationContext(), dbManager.getSpeakers(true));
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


