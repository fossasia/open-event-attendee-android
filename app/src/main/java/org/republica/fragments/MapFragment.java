package org.republica.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.republica.R;
import org.republica.widgets.TouchImageView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class MapFragment extends Fragment {

    private static final double DESTINATION_LATITUDE = 52.50003;
    private static final double DESTINATION_LONGITUDE = 13.37560;
    private static final String DESTINATION_NAME = "STATION-Berlin";
    private GoogleMap mMap;
    TouchImageView mapImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mapImageView = (TouchImageView) rootView.findViewById(R.id.map_image_view);
        mapImageView.setImageResource(R.drawable.eventmap_tues_wed);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.directions:
                launchDirections();
                return true;
            case R.id.day_select:
                selectDay();
                return true;
        }
        return false;
    }

    private void selectDay() {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(getResources().getString(R.string.select_map));
        DateFormatSymbols dfs = new DateFormatSymbols(getResources().getConfiguration().locale);
        String[] types = {dfs.getWeekdays()[3], dfs.getWeekdays()[4], dfs.getWeekdays()[5]};
        b.setItems(types, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        mapImageView.setImageResource(R.drawable.eventmap_tues_wed);
                        break;
                    case 1:
                        mapImageView.setImageResource(R.drawable.eventmap_tues_wed);
                        break;
                    case 2:
                        mapImageView.setImageResource(R.drawable.eventmap_thurs);
                        break;
                }
            }

        });

        b.show();

    }

    private void launchDirections() {
        // Build intent to start Google Maps directions
        String uri = String.format(Locale.US,
                "https://www.google.com/maps/search/%1$s/@%2$f,%3$f,17z",
                DESTINATION_NAME, DESTINATION_LATITUDE, DESTINATION_LONGITUDE);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

        startActivity(intent);
    }
}
