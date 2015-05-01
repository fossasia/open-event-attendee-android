package org.republica.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import org.republica.R;
import org.republica.activities.EventDetailsActivity;
import org.republica.adapters.ScheduleAdapter;
import org.republica.db.DatabaseManager;
import org.republica.model.FossasiaEvent;
import org.republica.model.Speaker;
import org.republica.utils.VolleySingleton;

import java.util.ArrayList;

public class PersonInfoListFragment extends SmoothListFragment {

    private static final String ARG_PERSON = "person";

    private Speaker person;
    private ArrayList<FossasiaEvent> events;

    public static PersonInfoListFragment newInstance(Speaker speaker) {
        PersonInfoListFragment f = new PersonInfoListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PERSON, speaker);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        person = getArguments().getParcelable(ARG_PERSON);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.person, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more_info:
                Intent intent;

                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://fossasia.org/"));

                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.no_data));

        int contentMargin = getResources().getDimensionPixelSize(R.dimen.content_margin);
        ListView listView = getListView();
        listView.setClipToPadding(false);
        listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.header_person_info, null);

        TextView name;
        TextView designation;
        NetworkImageView speakerImage;
        TextView information;
        ImageView linkedIn;
        ImageView twitter;

        name = (TextView) headerView.findViewById(R.id.textView_speaker_name);
        designation = (TextView) headerView.findViewById(R.id.textView_speaker_designation);
        speakerImage = (NetworkImageView) headerView.findViewById(R.id.imageView_speaker_pic);
        speakerImage.setDefaultImageResId(R.drawable.default_user);
        information = (TextView) headerView.findViewById(R.id.textView_speaker_information);
        information.setVisibility(View.GONE);
        linkedIn = (ImageView) headerView.findViewById(R.id.imageView_linkedin);
        twitter = (ImageView) headerView.findViewById(R.id.imageView_twitter);
        getListView().addHeaderView(headerView, null, false);
        name.setText(person.getName());
        designation.setText(person.getDesignation());
        information.setText(person.getInformation());
        if (person.getProfilePicUrl() != null && person.getProfilePicUrl().equals("")) {
            speakerImage.setImageUrl("http://forschdb.verwaltung.uni-freiburg.de/pix/forschdb/mitarbeiter_12821_20141208121645.png", VolleySingleton.getImageLoader(getActivity().getApplicationContext()));
        } else {
            speakerImage.setImageUrl(person.getProfilePicUrl(), VolleySingleton.getImageLoader(getActivity().getApplicationContext()));
        }
        if (person.getLinkedInUrl() == null || person.getLinkedInUrl().equals("")) {
            linkedIn.setVisibility(View.GONE);
        }
        if (person.getTwitterHandle() == null || person.getTwitterHandle().equals("")) {
            twitter.setVisibility(View.GONE);
        }

        //
        if (person.getLinkedInUrl().length() == 0 || person.getLinkedInUrl().equals("")) {
            linkedIn.setVisibility(View.GONE);
        } else {
            linkedIn.setVisibility(View.VISIBLE);
            linkedIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = person.getLinkedInUrl();
                    if (URLUtil.isValidUrl(url)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().getApplicationContext().startActivity(intent);

                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Invalid linkedin handle", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (person.getTwitterHandle().length() == 0 || person.getTwitterHandle().equals("")) {
            twitter.setVisibility(View.GONE);
        } else {
            twitter.setVisibility(View.VISIBLE);
            twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String url = person.getTwitterHandle();
                    if (URLUtil.isValidUrl(url)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().getApplicationContext().startActivity(intent);

                    } else if (url.contains("@")) {
                        url = url.replace("@", "");
                        url = "http://twitter.com/" + url;
                        if (URLUtil.isValidUrl(url)) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getActivity().getApplicationContext().startActivity(intent);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Invalid twitter handle", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //

        DatabaseManager dbManager = DatabaseManager.getInstance();
        events = dbManager.getEventBySpeaker(person.getName());
        setListAdapter(new ScheduleAdapter(getActivity(), events));
        ;

    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity().getApplicationContext(), EventDetailsActivity.class);
        // Using position - 1 because first 0th position is taken by Header View
        intent.putExtra("event", events.get(position - 1));
        startActivity(intent);
        super.onListItemClick(l, v, position, id);
    }


}
