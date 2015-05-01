package org.republica.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.republica.R;
import org.republica.fragments.PersonInfoListFragment;
import org.republica.model.Speaker;

public class PersonInfoActivity extends ActionBarActivity {

    public static final String SPEAKER = "speaker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_extended_title);
        Speaker person = getIntent().getParcelableExtra(SPEAKER);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(person.getName());

        if (savedInstanceState == null) {
            Fragment f = PersonInfoListFragment.newInstance(person);
            getSupportFragmentManager().beginTransaction().add(R.id.content, f).addToBackStack(null).commit();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
