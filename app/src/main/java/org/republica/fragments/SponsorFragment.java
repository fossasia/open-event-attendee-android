package org.republica.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.republica.adapters.SponsorAdapter;
import org.republica.db.DatabaseManager;
import org.republica.model.Sponsor;

/**
 * Created by manan on 25-03-2015.
 */
public class SponsorFragment extends SmoothListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseManager dbManager = DatabaseManager.getInstance();
        SponsorAdapter adapter = new SponsorAdapter(getActivity().getApplicationContext(), dbManager.getSponsors());
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Sponsor sponser = (Sponsor) v.getTag();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(sponser.getUrl()));
        startActivity(intent);
    }
}
