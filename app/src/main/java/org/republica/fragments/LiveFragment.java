package org.republica.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.republica.R;

public class LiveFragment extends Fragment {

    private LivePagerAdapter livePagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        livePagerAdapter = new LivePagerAdapter(getChildFragmentManager(), getResources());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
//
//        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
//        pager.setAdapter(livePagerAdapter);
//        SlidingTabLayout slidingTabs = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
//        slidingTabs.setViewPager(pager);

        return view;
    }

    private static class LivePagerAdapter extends FragmentPagerAdapter {

        private final Resources resources;

        public LivePagerAdapter(FragmentManager fm, Resources resources) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return resources.getString(R.string.next);
                case 1:
                    return resources.getString(R.string.now);
            }
            return null;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            // Hack to allow the non-primary fragments to start properly
            if (object != null) {
                ((Fragment) object).setUserVisibleHint(false);
            }
        }
    }
}
