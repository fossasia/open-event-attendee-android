package org.republica.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.common.view.SlidingTabLayout;

import org.republica.R;
import org.republica.db.DatabaseManager;
import org.republica.model.Day;

import java.util.ArrayList;


/**
 * Created by Abhishek on 24/02/15.
 */
public class ScheduleFragment extends Fragment {

    public final static String TAG = "ScheduleFragment";

    private DayLoader daysAdapter;
    private ViewHolder holder;

    public static Fragment newInstance(String track) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle bundle = new Bundle();
        bundle.putString("TRACK", track);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String track = getArguments().getString("TRACK");


            ArrayList<Day> staticDays = new ArrayList<>();
            staticDays.add(new Day(0, "May 5"));
            staticDays.add(new Day(1, "May 6"));
            staticDays.add(new Day(2, "May 7"));


            daysAdapter = new DayLoader(getChildFragmentManager(), track, staticDays);
        }


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        DatabaseManager db = DatabaseManager.getInstance();
        String track = getArguments().getString("TRACK");
        ArrayList<Day> days = db.getDates(track);

        String subTitle = "";
        for (Day day : days) {
            if (days.indexOf(day) != 0) {
                subTitle += ", ";
            }
            subTitle += day.getDate();

        }
        ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(subTitle);
        holder = new ViewHolder();
        holder.contentView = view.findViewById(R.id.content);
        holder.emptyView = view.findViewById(android.R.id.empty);
        holder.pager = (ViewPager) view.findViewById(R.id.pager);
        holder.slidingTabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        holder.contentView.setVisibility(View.VISIBLE);
        holder.emptyView.setVisibility(View.GONE);
        if (holder.pager.getAdapter() == null) {
            holder.pager.setAdapter(daysAdapter);
        }
        holder.slidingTabs.setViewPager(holder.pager);
        for (Day day : days) {
            Log.d(this.getClass().getCanonicalName(), day.getDate());

        }
        if (days.size() > 0) {
            String[] date = days.get(0).getDate().split(" ");
            int position = Integer.parseInt(date[1]) - 13;
            holder.pager.setCurrentItem(position);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holder = null;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.directions:
                launchDirections();
                return true;
        }
        return false;
    }

    private void launchDirections() {
        // Build intent to start Google Maps directions
//        String uri = String.format(Locale.US,
//                "https://www.google.com/maps/search/%1$s/@%2$f,%3$f,17z

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com.sg/maps/place/Biopolis/@1.304256,103.79179,16z/data=!4m2!3m1!1s0x0:0x9965b36cbf8d88c3"));

        startActivity(intent);
    }

    private static class ViewHolder {
        View contentView;
        View emptyView;
        ViewPager pager;
        SlidingTabLayout slidingTabs;
    }

    private static class DayLoader extends FragmentStatePagerAdapter {

        private ArrayList<String> mPageTitle;
        private ArrayList<Day> days;
        private String track;

        public DayLoader(FragmentManager fm, String track, ArrayList<Day> days) {
            super(fm);
            mPageTitle = new ArrayList<String>();
            this.track = track;
            this.days = days;

        }

        @Override
        public Fragment getItem(int position) {

            return ScheduleListFragment.newInstance(days.get(position).getDate(), track);
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return days.get(position).getDate();
        }


    }
}
