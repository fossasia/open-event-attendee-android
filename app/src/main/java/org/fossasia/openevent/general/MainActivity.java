package org.fossasia.openevent.general;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import org.fossasia.openevent.general.utils.ConstantStrings;
import org.fossasia.openevent.general.utils.SharedPreferencesUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.navigation)
    protected BottomNavigationView navigation;

    private ActionBar toolbar;
    private String TOKEN = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_events:
                    toolbar.setTitle("Events");
                    fragment = new EventsFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_profile:
                    if (TOKEN != null) {
                        toolbar.setTitle("Profile");
                        fragment = new ProfileFragment();
                        loadFragment(fragment);
                    } else {
                        Toast.makeText(getApplicationContext(), "You need to login first!" , Toast.LENGTH_LONG).show();
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        toolbar = getSupportActionBar();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar.hide();
        getSupportActionBar().setTitle("Events");
        TOKEN = SharedPreferencesUtil.getString(ConstantStrings.TOKEN, null);

        EventsFragment eventsFragment = new EventsFragment();
        loadFragment(eventsFragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}