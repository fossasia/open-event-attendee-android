package org.republica.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.republica.alarms.FosdemAlarmManager;
import org.republica.services.AlarmIntentService;

/**
 * Entry point for system-generated events: boot complete and alarms.
 *
 * @author Christophe Beyls
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final String ACTION_NOTIFY_EVENT = "be.digitalia.fosdem.action.NOTIFY_EVENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_NOTIFY_EVENT.equals(action)) {

            // Forward the intent to the AlarmIntentService for background processing of the notification
            Intent serviceIntent = new Intent(context, AlarmIntentService.class);
            serviceIntent.setAction(ACTION_NOTIFY_EVENT);
            serviceIntent.setData(intent.getData());
            startWakefulService(context, serviceIntent);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {

            if (FosdemAlarmManager.getInstance().isEnabled()) {
                Intent serviceIntent = new Intent(context, AlarmIntentService.class);
                serviceIntent.setAction(AlarmIntentService.ACTION_UPDATE_ALARMS);
                serviceIntent.putExtra(AlarmIntentService.EXTRA_WITH_WAKE_LOCK, true);
                startWakefulService(context, serviceIntent);
            }
        }
    }

}
