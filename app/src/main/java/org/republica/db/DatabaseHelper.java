package org.republica.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String EVENTS_TABLE_NAME = "events";
    public static final String BOOKMARKS_TABLE_NAME = "bookmarks";
    public static final String TABLE_NAME_KEY_SPEAKERS = "key_speakers";
    public static final String TABLE_NAME_SPONSOR = "sponsors";
    public static final String TABLE_NAME_SCHEDULE = "schedule";

    public static final String TABLE_NAME_SPEAKER_EVENT_RELATION = "speaker_event_relation";
    public static final String TABLE_NAME_TRACK = "tracks";
    public static final String TABLE_COLUMN_NAME = "track_name";
    public static final String TABLE_COLOUMN_INFORMATION = "information";

    public static final String TABLE_NAME_TRACK_VENUE = "track_venue";

    public static final String TABLE_NAME_VENUE = "venue";

    private static final String DATABASE_NAME = "repub.sqlite4";
    private static final int DATABASE_VERSION = 1;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE "
                + EVENTS_TABLE_NAME
                + " (id INTEGER PRIMARY KEY, day_index INTEGER NOT NULL, start_time INTEGER, end_time INTEGER, room_name TEXT, slug TEXT, track_id INTEGER, abstract TEXT, description TEXT);");
        database.execSQL("CREATE TABLE " + TABLE_NAME_SPONSOR
                + " (id INTEGER , name TEXT, img TEXT, url TEXT);");
        database.execSQL("CREATE TABLE " + BOOKMARKS_TABLE_NAME + " (event_id INTEGER PRIMARY KEY);");
        database.execSQL("CREATE TABLE " + TABLE_NAME_KEY_SPEAKERS
                + " (id INTEGER PRIMARY KEY, name TEXT, designation TEXT, information TEXT, twitter_handle TEXT,"
                + " linkedin_url TEXT, profile_pic_url TEXT, is_key_speaker INTEGER, UNIQUE(name));");
        database.execSQL("CREATE TABLE " + TABLE_NAME_SCHEDULE
                + " (id INTEGER PRIMARY KEY, title TEXT, sub_title TEXT, date TEXT, day TEXT, start_time TEXT, abstract_text TEXT, description TEXT, venue TEXT, track TEXT, moderator TEXT);");
        database.execSQL("CREATE TABLE " + TABLE_NAME_SPEAKER_EVENT_RELATION
                + " (speaker TEXT, event_id INTEGER, event TEXT);");
        database.execSQL("CREATE TABLE " + TABLE_NAME_TRACK
                + " (_id INTEGER, " + TABLE_COLUMN_NAME + " TEXT, " + TABLE_COLOUMN_INFORMATION + " TEXT);");
        database.execSQL("CREATE TABLE " + TABLE_NAME_TRACK_VENUE
                + " (id INTEGER, track TEXT, venue TEXT, map TEXT);");
        database.execSQL("CREATE TABLE " + TABLE_NAME_VENUE
                + " (track TEXT, venue TEXT, map TEXT, room TEXT, link TEXT, address TEXT, how_to_reach TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Nothing to upgrade yet
    }
}
