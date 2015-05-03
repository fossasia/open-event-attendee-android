package org.republica.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.republica.model.Day;
import org.republica.model.FossasiaEvent;
import org.republica.model.Person;
import org.republica.model.Speaker;
import org.republica.model.Sponsor;
import org.republica.model.Venue;
import org.republica.utils.StringUtils;

import java.util.ArrayList;

/**
 * Here comes the badass SQL.
 *
 * @author Christophe Beyls
 */
public class DatabaseManager {

    public static final String ACTION_SCHEDULE_REFRESHED = "be.digitalia.fosdem.action.SCHEDULE_REFRESHED";
    public static final String ACTION_ADD_BOOKMARK = "be.digitalia.fosdem.action.ADD_BOOKMARK";
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_START_TIME = "event_start";
    public static final String ACTION_REMOVE_BOOKMARKS = "be.digitalia.fosdem.action.REMOVE_BOOKMARKS";
    public static final String EXTRA_EVENT_IDS = "event_ids";
    public static final int PERSON_NAME_COLUMN_INDEX = 1;
    private static final Uri URI_TRACKS = Uri.parse("sqlite://be.digitalia.fosdem/tracks");
    private static final Uri URI_EVENTS = Uri.parse("sqlite://be.digitalia.fosdem/events");
    private static final String DB_PREFS_FILE = "database";
    private static final String LAST_UPDATE_TIME_PREF = "last_update_time";
    private static final String[] COUNT_PROJECTION = new String[]{"count(*)"};
    // Ignore conflicts in case of existing person
    private static DatabaseManager instance;
    private Context context;
    private DatabaseHelper helper;

    private DatabaseManager(Context context) {
        this.context = context;
        helper = new DatabaseHelper(context);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    private static long queryNumEntries(SQLiteDatabase db, String table, String selection, String[] selectionArgs) {
        Cursor cursor = db.query(table, COUNT_PROJECTION, selection, selectionArgs, null, null, null);
        try {
            cursor.moveToFirst();
            return cursor.getLong(0);
        } finally {
            cursor.close();
        }
    }

    private static void bindString(SQLiteStatement statement, int index, String value) {
        if (value == null) {
            statement.bindNull(index);
        } else {
            statement.bindString(index, value);
        }
    }

    private static void clearDatabase(SQLiteDatabase db) {
//        db.delete(DatabaseHelper.EVENTS_TITLES_TABLE_NAME, null, null);
//        db.delete(DatabaseHelper.PERSONS_TABLE_NAME, null, null);
//        db.delete(DatabaseHelper.EVENTS_PERSONS_TABLE_NAME, null, null);
//        db.delete(DatabaseHelper.LINKS_TABLE_NAME, null, null);
//        db.delete(DatabaseHelper.TRACKS_TABLE_NAME, null, null);
//        db.delete(DatabaseHelper.DAYS_TABLE_NAME, null, null);
        // Deleting Fossasia tables
        db.delete(DatabaseHelper.TABLE_NAME_KEY_SPEAKERS, null, null);
        db.delete(DatabaseHelper.TABLE_NAME_SCHEDULE, null, null);
        db.delete(DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, null, null);
        db.delete(DatabaseHelper.TABLE_NAME_TRACK, null, null);
        db.delete(DatabaseHelper.TABLE_NAME_SPONSOR, null, null);

    }

    public ArrayList<FossasiaEvent> getBookmarkEvents() {

        ArrayList<FossasiaEvent> bookmarkedEvents = new ArrayList<>();
        Cursor cursor = helper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.BOOKMARKS_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                bookmarkedEvents.add(getEventById(cursor.getInt(0)));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return bookmarkedEvents;

    }

    public static long toEventId(Cursor cursor) {
        return cursor.getLong(0);
    }

    public static long toEventStartTimeMillis(Cursor cursor) {
        return cursor.isNull(1) ? -1L : cursor.getLong(1);
    }

    public static Person toPerson(Cursor cursor, Person person) {
        if (person == null) {
            person = new Person();
        }
        person.setId(cursor.getLong(0));
        person.setName(cursor.getString(1));

        return person;
    }

    public static Person toPerson(Cursor cursor) {
        return toPerson(cursor, null);
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(DB_PREFS_FILE, Context.MODE_PRIVATE);
    }

    public void performInsertQueries(ArrayList<String> queries) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        for (String query : queries) {
            Log.d(this.getClass().getCanonicalName(), query);
            db.execSQL(query);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public void clearDatabase() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            clearDatabase(db);

            db.setTransactionSuccessful();
            getSharedPreferences().edit().remove(LAST_UPDATE_TIME_PREF).commit();
        } finally {
            db.endTransaction();

            context.getContentResolver().notifyChange(URI_TRACKS, null);
            context.getContentResolver().notifyChange(URI_EVENTS, null);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SCHEDULE_REFRESHED));
        }
    }

    public ArrayList<Day> getDates(String track) {

        ArrayList<Day> days = new ArrayList<Day>();
        String query = "SELECT date FROM schedule WHERE track='%s' GROUP BY date";
        Cursor cursor = helper.getReadableDatabase().rawQuery(String.format(query, track), null);
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                days.add(new Day(count, cursor.getString(0)));
                count++;
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return days;
    }

    public FossasiaEvent getEventById(int id) {
        FossasiaEvent temp = null;
        Cursor cursor = helper.getReadableDatabase().rawQuery("SELECT * FROM schedule WHERE id=" + id, null);
        String title;
        String subTitle;
        String date;
        String day;
        String startTime;
        String abstractText;
        String description;
        String venue;
        String track;
        String moderator;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                title = cursor.getString(1);
                subTitle = cursor.getString(2);
                date = cursor.getString(3);
                day = cursor.getString(4);
                startTime = cursor.getString(5);
                abstractText = cursor.getString(6);
                description = cursor.getString(7);
                venue = cursor.getString(8);
                track = cursor.getString(9);
                moderator = cursor.getString(10);
                Cursor cursorSpeaker = helper.getReadableDatabase().rawQuery(String.format("SELECT speaker FROM %s WHERE event_id=%d", DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, id), null);
                ArrayList<String> speakers = new ArrayList<String>();
                if (cursorSpeaker.moveToFirst()) {
                    do {
                        speakers.add(cursorSpeaker.getString(0));
                    }
                    while (cursorSpeaker.moveToNext());
                }

                temp = new FossasiaEvent(id, title, subTitle, speakers, date, day, date + " " + startTime, abstractText, description, venue, track, moderator);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return temp;
    }

    public ArrayList<FossasiaEvent> getEventsByDate(String selectDate) {


        Cursor cursor = helper.getReadableDatabase().rawQuery("SELECT * FROM schedule WHERE date='" + selectDate + "'", null);
        ArrayList<FossasiaEvent> fossasiaEventList = new ArrayList<FossasiaEvent>();
        int id;
        String title;
        String subTitle;
        String date;
        String day;
        String startTime;
        String abstractText;
        String description;
        String venue;
        String track;
        String moderator;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                title = cursor.getString(1);
                subTitle = cursor.getString(2);
                date = cursor.getString(3);
                day = cursor.getString(4);
                startTime = cursor.getString(5);
                abstractText = cursor.getString(6);
                description = cursor.getString(7);
                venue = cursor.getString(8);
                track = cursor.getString(9);
                moderator = cursor.getString(10);
                Cursor cursorSpeaker = helper.getReadableDatabase().rawQuery(String.format("SELECT speaker FROM %s WHERE event_id=%d", DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, id), null);
                ArrayList<String> speakers = new ArrayList<String>();
                if (cursorSpeaker.moveToFirst()) {
                    do {
                        speakers.add(cursorSpeaker.getString(0));
                    }
                    while (cursorSpeaker.moveToNext());
                }

                fossasiaEventList.add(new FossasiaEvent(id, title, subTitle, speakers, date, day, startTime, abstractText, description, venue, track, moderator));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return fossasiaEventList;
    }

    public ArrayList<FossasiaEvent> getEventBySpeaker(String name) {

        Cursor cursorEvents = helper.getReadableDatabase().rawQuery(String.format("SELECT event FROM %s WHERE speaker='%s'", DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, name), null);
        ArrayList<String> events = new ArrayList<String>();
        if (cursorEvents.moveToFirst()) {
            do {
                events.add(cursorEvents.getString(0));
            }
            while (cursorEvents.moveToNext());
        }
        cursorEvents.close();
        ArrayList<FossasiaEvent> fossasiaEventList = new ArrayList<FossasiaEvent>();

        for (String event : events) {

            Cursor cursor = helper.getReadableDatabase().rawQuery("SELECT * FROM schedule WHERE title='" + StringUtils.removeDiacritics(event) + "'", null);
            int id;
            String title;
            String subTitle;
            String date;
            String day;
            String startTime;
            String abstractText;
            String description;
            String venue;
            String track;
            String moderator;
            if (cursor.moveToFirst()) {
                do {
                    id = cursor.getInt(0);
                    title = cursor.getString(1);
                    subTitle = cursor.getString(2);
                    date = cursor.getString(3);
                    day = cursor.getString(4);
                    startTime = cursor.getString(5);
                    abstractText = cursor.getString(6);
                    description = cursor.getString(7);
                    venue = cursor.getString(8);
                    track = cursor.getString(9);
                    moderator = cursor.getString(10);
                    Cursor cursorSpeaker = helper.getReadableDatabase().rawQuery(String.format("SELECT speaker FROM %s WHERE event_id=%d", DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, id), null);
                    ArrayList<String> speakers = new ArrayList<String>();
                    if (cursorSpeaker.moveToFirst()) {
                        do {
                            speakers.add(cursorSpeaker.getString(0));
                        }
                        while (cursorSpeaker.moveToNext());
                    }

                    fossasiaEventList.add(new FossasiaEvent(id, title, subTitle, speakers, date, day, date + " " + startTime, abstractText, description, venue, track, moderator));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fossasiaEventList;
    }


    public ArrayList<FossasiaEvent> getEventsByDateandTrack(String selectDate, String track) {
        Cursor cursor = helper.getReadableDatabase().rawQuery("SELECT * FROM schedule WHERE date='" + selectDate + "' AND track='" + track + "'", null);
        ArrayList<FossasiaEvent> fossasiaEventList = new ArrayList<FossasiaEvent>();
        int id;
        String title;
        String subTitle;
        String date;
        String day;
        String startTime;
        String abstractText;
        String description;
        String venue;
        String moderator;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                title = cursor.getString(1);
                subTitle = cursor.getString(2);
                date = cursor.getString(3);
                day = cursor.getString(4);
                startTime = cursor.getString(5);
                abstractText = cursor.getString(6);
                description = cursor.getString(7);
                venue = cursor.getString(8);
                track = cursor.getString(9);
                moderator = cursor.getString(10);

                Cursor cursorSpeaker = helper.getReadableDatabase().rawQuery(String.format("SELECT speaker FROM %s WHERE event_id=%d", DatabaseHelper.TABLE_NAME_SPEAKER_EVENT_RELATION, id), null);
                ArrayList<String> speakers = new ArrayList<String>();
                if (cursorSpeaker.moveToFirst()) {
                    do {
                        speakers.add(cursorSpeaker.getString(0));
                    }
                    while (cursorSpeaker.moveToNext());
                }

                fossasiaEventList.add(new FossasiaEvent(id, title, subTitle, speakers, date, day, startTime, abstractText, description, venue, track, moderator));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return fossasiaEventList;
    }


    public ArrayList<Speaker> getSpeakers(boolean fetchKeySpeaker) {
        Cursor cursor = helper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME_KEY_SPEAKERS, null, null, null, null, null, null);
        ArrayList<Speaker> speakers = new ArrayList<Speaker>();
        int id;
        String name;
        String designation;
        String profilePicUrl;
        String information;
        String twitterHandle;
        String linkedInUrl;
        int isKeySpeaker;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                name = cursor.getString(1);
                designation = cursor.getString(2);
                information = cursor.getString(3);
                twitterHandle = cursor.getString(4);
                linkedInUrl = cursor.getString(5);
                profilePicUrl = cursor.getString(6);
                isKeySpeaker = cursor.getInt(7);
                if (isKeySpeaker == 1 && fetchKeySpeaker) {
                    speakers.add(new Speaker(id, name, information, linkedInUrl, twitterHandle, designation, profilePicUrl, isKeySpeaker));
                } else if (isKeySpeaker == 0 && !fetchKeySpeaker) {
                    speakers.add(new Speaker(id, name, information, linkedInUrl, twitterHandle, designation, profilePicUrl, isKeySpeaker));
                }
            }
            while (cursor.moveToNext());

        }
        cursor.close();
        return speakers;
    }


    public Cursor getTracks() {
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_NAME_TRACK, null);
        return cursor;
    }

    public ArrayList<Sponsor> getSponsors() {

        Cursor cursor = helper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME_SPONSOR, null, null, null, null, null, null);
        ArrayList<Sponsor> sponsors = new ArrayList<Sponsor>();
        int id;
        String name;
        String img;
        String url;

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                name = cursor.getString(1);
                img = cursor.getString(2);
                url = cursor.getString(3);
                sponsors.add(new Sponsor(id, name, img, url));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        return sponsors;
    }



    public boolean isBookmarked(FossasiaEvent event) {
        String[] selectionArgs = new String[]{String.valueOf(event.getId())};
        return queryNumEntries(helper.getReadableDatabase(), DatabaseHelper.BOOKMARKS_TABLE_NAME, "event_id = ?", selectionArgs) > 0L;
    }

    public String getTrackMapUrl(String track) {
        Cursor cursor = helper.getReadableDatabase().rawQuery(String.format("SELECT map FROM %s WHERE track='%s'", DatabaseHelper.TABLE_NAME_TRACK_VENUE, track), null);
        String map = "htttp://maps.google.com/";
        if (cursor.moveToFirst()) {
            map = cursor.getString(0);
        }
        cursor.close();
        return map;
    }

    public boolean addBookmark(FossasiaEvent event) {
        boolean complete = false;

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("event_id", event.getId());
            long result = db.insert(DatabaseHelper.BOOKMARKS_TABLE_NAME, null, values);

            // If the bookmark is already present
            if (result == -1L) {
                return false;
            }

            db.setTransactionSuccessful();
            complete = true;
            return true;
        } finally {
            db.endTransaction();

            if (complete) {
                context.getContentResolver().notifyChange(URI_EVENTS, null);

                Intent intent = new Intent(ACTION_ADD_BOOKMARK).putExtra(EXTRA_EVENT_ID, event.getId());
                // TODO: For now commented this, must implement String to date converter.
//                Date startTime = event.getStartTime();
//                if (startTime != null) {
//                    intent.putExtra(EXTRA_EVENT_START_TIME, startTime.getTime());
//                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    }


    public boolean removeBookmark(FossasiaEvent event) {
        return removeBookmarks(new long[]{event.getId()});
    }

    public boolean removeBookmark(long eventId) {
        return removeBookmarks(new long[]{eventId});
    }

    public boolean removeBookmarks(long[] eventIds) {
        int length = eventIds.length;
        if (length == 0) {
            throw new IllegalArgumentException("At least one bookmark id to remove must be passed");
        }
        String[] stringEventIds = new String[length];
        for (int i = 0; i < length; ++i) {
            stringEventIds[i] = String.valueOf(eventIds[i]);
        }

        boolean isComplete = false;

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String whereClause = "event_id IN (" + TextUtils.join(",", stringEventIds) + ")";
            int count = db.delete(DatabaseHelper.BOOKMARKS_TABLE_NAME, whereClause, null);

            if (count == 0) {
                return false;
            }

            db.setTransactionSuccessful();
            isComplete = true;
            return true;
        } finally {
            db.endTransaction();

            if (isComplete) {
                context.getContentResolver().notifyChange(URI_EVENTS, null);

                Intent intent = new Intent(ACTION_REMOVE_BOOKMARKS).putExtra(EXTRA_EVENT_IDS, eventIds);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    }

    public Venue getVenueFromTrack(String track) {
        String query = "SELECT * FROM %s WHERE track='%s'";
        Venue ven = null;
        Cursor cursor = helper.getReadableDatabase().rawQuery(String.format(query, DatabaseHelper.TABLE_NAME_VENUE, track), null);
        if (cursor.moveToFirst()) {
            //tract TEXT, venue TEXT, map TEXT, room TEXT, link TEXT, address TEXT, how_to_reach TEXT        }
            String venue = cursor.getString(1);
            String map = cursor.getString(2);
            String room = cursor.getString(3);
            String link = cursor.getString(4);
            String address = cursor.getString(5);
            String howToReach = cursor.getString(6);
            ven = new Venue(track, venue, map, room, link, address, howToReach);
        }
        return ven;
    }
}
