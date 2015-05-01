package org.republica.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.republica.R;
import org.republica.fragments.FossasiaEventDetailsFragment;
import org.republica.model.FossasiaEvent;
import org.republica.utils.NfcUtils;
import org.republica.utils.NfcUtils.CreateNfcAppDataCallback;

/**
 * Displays a single event passed as a complete Parcelable object in extras.
 *
 * @author Christophe Beyls
 */
public class EventDetailsActivity extends ActionBarActivity implements CreateNfcAppDataCallback {

    public static final String EXTRA_EVENT = "event";

    private FossasiaEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);


        FossasiaEvent event = getIntent().getParcelableExtra(EXTRA_EVENT);
        String map = getIntent().getStringExtra("MAP");
        bar.setTitle(event.getTitle());
        bar.setSubtitle(event.getSubTitle());

        // The event has been passed as parameter, it can be displayed immediately
        initEvent(event);

        if (savedInstanceState == null) {
            Fragment f = FossasiaEventDetailsFragment.newInstance(event, map);
            getSupportFragmentManager().beginTransaction().add(R.id.content, f).commit();
        }

    }

    /**
     * Initialize event-related configuration after the event has been loaded.
     */
    private void initEvent(FossasiaEvent event) {
        this.event = event;
        // Enable up navigation only after getting the event details
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Enable Android Beam
        NfcUtils.setAppDataPushMessageCallbackIfAvailable(this, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public byte[] createNfcAppData() {
        return String.valueOf(event.getId()).getBytes();
    }

}
