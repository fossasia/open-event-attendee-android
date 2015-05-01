package org.republica.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import org.republica.db.DatabaseManager;
import org.republica.fragments.SettingsFragment;
import org.republica.receivers.AlarmReceiver;

/**
 * A service to schedule or unschedule alarms in the background, keeping the app responsive.
 *
 * @author Christophe Beyls
 */
public class AlarmIntentService extends IntentService {

    public static final String ACTION_UPDATE_ALARMS = "be.digitalia.fosdem.action.UPDATE_ALARMS";
    public static final String EXTRA_WITH_WAKE_LOCK = "with_wake_lock";
    public static final String ACTION_DISABLE_ALARMS = "be.digitalia.fosdem.action.DISABLE_ALARMS";

    private AlarmManager alarmManager;

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Ask for the last unhandled intents to be redelivered if the service dies early.
        // This ensures we handle all events, in order.
        setIntentRedelivery(true);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getAlarmPendingIntent(long eventId) {
        Intent intent = new Intent(this, AlarmReceiver.class).setAction(AlarmReceiver.ACTION_NOTIFY_EVENT).setData(
                Uri.parse(String.valueOf(eventId)));
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {

            case ACTION_UPDATE_ALARMS: {

                // Create/update all alarms
                long delay = getDelay();
                long now = System.currentTimeMillis();
                Cursor cursor = DatabaseManager.getInstance().getBookmarks(now);
                try {
                    while (cursor.moveToNext()) {
                        long eventId = DatabaseManager.toEventId(cursor);
                        long notificationTime = DatabaseManager.toEventStartTimeMillis(cursor) - delay;
                        PendingIntent pi = getAlarmPendingIntent(eventId);
                        if (notificationTime < now) {
                            // Cancel pending alarms that where scheduled between now and delay, if any
                            alarmManager.cancel(pi);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pi);
                        }
                    }
                } finally {
                    cursor.close();
                }

                // Release the wake lock setup by AlarmReceiver, if any
                if (intent.getBooleanExtra(EXTRA_WITH_WAKE_LOCK, false)) {
                    AlarmReceiver.completeWakefulIntent(intent);
                }

                break;
            }
            case ACTION_DISABLE_ALARMS: {

                // Cancel alarms of every bookmark in the future
                Cursor cursor = DatabaseManager.getInstance().getBookmarks(System.currentTimeMillis());
                try {
                    while (cursor.moveToNext()) {
                        long eventId = DatabaseManager.toEventId(cursor);
                        alarmManager.cancel(getAlarmPendingIntent(eventId));
                    }
                } finally {
                    cursor.close();
                }

                break;
            }
            case DatabaseManager.ACTION_ADD_BOOKMARK: {

                long delay = getDelay();
                long eventId = intent.getLongExtra(DatabaseManager.EXTRA_EVENT_ID, -1L);
                long startTime = intent.getLongExtra(DatabaseManager.EXTRA_EVENT_START_TIME, -1L);
                // Only schedule future events. If they start before the delay, the alarm will go off immediately
                if ((startTime == -1L) || (startTime < System.currentTimeMillis())) {
                    return;
                }
                alarmManager.set(AlarmManager.RTC_WAKEUP, startTime - delay, getAlarmPendingIntent(eventId));

                break;
            }
            case DatabaseManager.ACTION_REMOVE_BOOKMARKS: {

                // Cancel matching alarms, might they exist or not
                long[] eventIds = intent.getLongArrayExtra(DatabaseManager.EXTRA_EVENT_IDS);
                for (long eventId : eventIds) {
                    alarmManager.cancel(getAlarmPendingIntent(eventId));
                }

                break;
            }
            case AlarmReceiver.ACTION_NOTIFY_EVENT: {

                long eventId = Long.parseLong(intent.getDataString());
//                Event event = DatabaseManager.getInstance().getEvent(eventId);
//                if (event != null) {
//                    PendingIntent eventPendingIntent = TaskStackBuilder
//                            .create(this)
//                            .addNextIntent(new Intent(this, MainActivity.class))
//                            .addNextIntent(
//                                    new Intent(this, EventDetailsActivity.class).setData(Uri.parse(String.valueOf(event
//                                            .getId())))).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                    int defaultFlags = Notification.DEFAULT_SOUND;
//                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//                    if (sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFICATIONS_VIBRATE, false)) {
//                        defaultFlags |= Notification.DEFAULT_VIBRATE;
//                    }
//
//                    String personsSummary = event.getPersonsSummary();
//                    String trackName = event.getTrack().getName();
//                    String contentText;
//                    CharSequence bigText;
//                    if (TextUtils.isEmpty(personsSummary)) {
//                        contentText = trackName;
//                        bigText = event.getSubTitle();
//                    } else {
//                        contentText = String.format("%1$s - %2$s", trackName, personsSummary);
//                        String subTitle = event.getSubTitle();
//                        SpannableString spannableBigText;
//                        if (TextUtils.isEmpty(subTitle)) {
//                            spannableBigText = new SpannableString(personsSummary);
//                        } else {
//                            spannableBigText = new SpannableString(String.format("%1$s\n%2$s", subTitle, personsSummary));
//                        }
//                        // Set the persons summary in italic
//                        spannableBigText.setSpan(new StyleSpan(Typeface.ITALIC),
//                                spannableBigText.length() - personsSummary.length(), spannableBigText.length(),
//                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        bigText = spannableBigText;
//                    }
//
//                    int notificationColor = getResources().getColor(R.color.color_primary);
//
//                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                            .setSmallIcon(R.drawable.ic_stat_fosdem)
//                            .setColor(notificationColor)
//                            .setWhen(event.getStartTime().getTime())
//                            .setContentTitle(event.getTitle())
//                            .setContentText(contentText)
//                            .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText).setSummaryText(trackName))
//                            .setContentInfo(event.getRoomName())
//                            .setContentIntent(eventPendingIntent)
//                            .setAutoCancel(true)
//                            .setDefaults(defaultFlags)
//                            .setPriority(NotificationCompat.PRIORITY_HIGH)
//                            .setCategory(NotificationCompat.CATEGORY_EVENT);
//
//                    // Blink the LED with FOSDEM color if enabled in the options
//                    if (sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFICATIONS_LED, false)) {
//                        notificationBuilder.setLights(notificationColor, 1000, 5000);
//                    }
//
//                    // Android Wear extensions
//                    NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
//
//                    // Add an optional action button to show the room map image
//                    String roomName = event.getRoomName();
//                    int roomImageResId = getResources().getIdentifier(StringUtils.roomNameToResourceName(roomName),
//                            "drawable", getPackageName());
//                    if (roomImageResId != 0) {
//                        // The room name is the unique Id of a RoomImageDialogActivity
//                        Intent mapIntent = new Intent(this, RoomImageDialogActivity.class).setFlags(
//                                Intent.FLAG_ACTIVITY_NEW_TASK).setData(Uri.parse(roomName));
//                        mapIntent.putExtra(RoomImageDialogActivity.EXTRA_ROOM_NAME, roomName);
//                        mapIntent.putExtra(RoomImageDialogActivity.EXTRA_ROOM_IMAGE_RESOURCE_ID, roomImageResId);
//                        PendingIntent mapPendingIntent = PendingIntent.getActivity(this, 0, mapIntent,
//                                PendingIntent.FLAG_UPDATE_CURRENT);
//                        CharSequence mapTitle = getString(R.string.room_map);
//                        notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_place_white_24dp, mapTitle,
//                                mapPendingIntent));
//                        // Use bigger action icon for wearable notification
//                        wearableExtender.addAction(new NotificationCompat.Action(R.drawable.ic_place_white_wear, mapTitle,
//                                mapPendingIntent));
//                    }
//
//                    notificationBuilder.extend(wearableExtender);
//
//                    NotificationManagerCompat.from(this).notify((int) eventId, notificationBuilder.build());
//                }
//
//                AlarmReceiver.completeWakefulIntent(intent);
//                break;
            }
        }
    }

    private long getDelay() {
        String delayString = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsFragment.KEY_PREF_NOTIFICATIONS_DELAY, "0");
        // Convert from minutes to milliseconds
        return Long.parseLong(delayString) * 1000L * 60L;
    }
}
