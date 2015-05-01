package org.republica.fragments;

import android.os.Build;
import android.support.v4.app.ListFragment;

/**
 * ListFragment which disables the fade animation under certain conditions for more smoothness.
 */
public class SmoothListFragment extends ListFragment {

    @Override
    public void setListShown(boolean shown) {
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) && isResumed()) {
            super.setListShown(shown);
        } else {
            setListShownNoAnimation(shown);
        }
    }
}
