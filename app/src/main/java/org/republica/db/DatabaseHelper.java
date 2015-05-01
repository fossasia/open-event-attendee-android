package org.republica.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String EVENTS_TABLE_NAME = "events";
    public static final String EVENTS_TITLES_TABLE_NAME = "events_titles";
    public static final String PERSONS_TABLE_NAME = "persons";
    public static final String EVENTS_PERSONS_TABLE_NAME = "events_persons";
    public static final String LINKS_TABLE_NAME = "links";
    public static final String TRACKS_TABLE_NAME = "tracks_new";
    public static final String DAYS_TABLE_NAME = "days";
    public static final String BOOKMARKS_TABLE_NAME = "bookmarks";
    public static final String TABLE_NAME_KEY_SPEAKERS = "key_speakers";
    private static final String TABLE_KEY_SPEAKERS = "CREATE TABLE " + TABLE_NAME_KEY_SPEAKERS
            + " (id INTEGER PRIMARY KEY, name TEXT, designation TEXT, information TEXT, twitter_handle TEXT,"
            + " linkedin_url TEXT, profile_pic_url TEXT, is_key_speaker INTEGER, UNIQUE(name));";
    public static final String TABLE_NAME_SPONSOR = "sponsors";
    public static final String TABLE_NAME_SCHEDULE = "schedule";

    private static final String TABLE_SCHEDULE = "CREATE TABLE " + TABLE_NAME_SCHEDULE
            + " (id INTEGER PRIMARY KEY, title TEXT, sub_title TEXT, date TEXT, day TEXT, start_time TEXT, abstract_text TEXT, description TEXT, venue TEXT, track TEXT, moderator TEXT);";

    public static final String TABLE_NAME_SPEAKER_EVENT_RELATION = "speaker_event_relation";
    private static final String TABLE_SPEAKER_EVENT_RELATION = "CREATE TABLE " + TABLE_NAME_SPEAKER_EVENT_RELATION
            + " (speaker TEXT, event_id INTEGER, event TEXT);";
    public static final String TABLE_NAME_TRACK = "tracks";
    public static final String TABLE_COLUMN_NAME = "track_name";
    public static final String TABLE_COLOUMN_INFORMATION = "information";
    private static final String TABLE_TRACKS = "CREATE TABLE " + TABLE_NAME_TRACK
            + " (_id INTEGER, " + TABLE_COLUMN_NAME + " TEXT, " + TABLE_COLOUMN_INFORMATION + " TEXT);";


    public static final String TABLE_NAME_TRACK_VENUE = "track_venue";
    private static final String TABLE_TRACK_VENUE = "CREATE TABLE " + TABLE_NAME_TRACK_VENUE
            + " (id INTEGER, track TEXT, venue TEXT, map TEXT);";

    public static final String TABLE_NAME_VENUE = "venue";
    private static final String TABLE_VENUE = "CREATE TABLE " + TABLE_NAME_VENUE
            + " (track TEXT, venue TEXT, map TEXT, room TEXT, link TEXT, address TEXT, how_to_reach TEXT);";

    private static final String DATABASE_NAME = "fosdem.sqlite5";
    private static final int DATABASE_VERSION = 1;




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Events
        database.execSQL("CREATE TABLE "
                + EVENTS_TABLE_NAME
                + " (id INTEGER PRIMARY KEY, day_index INTEGER NOT NULL, start_time INTEGER, end_time INTEGER, room_name TEXT, slug TEXT, track_id INTEGER, abstract TEXT, description TEXT);");
        database.execSQL("CREATE INDEX event_day_index_idx ON " + EVENTS_TABLE_NAME + " (day_index)");
        database.execSQL("CREATE INDEX event_start_time_idx ON " + EVENTS_TABLE_NAME + " (start_time)");
        database.execSQL("CREATE INDEX event_end_time_idx ON " + EVENTS_TABLE_NAME + " (end_time)");
        database.execSQL("CREATE INDEX event_track_id_idx ON " + EVENTS_TABLE_NAME + " (track_id)");
        // Secondary table with fulltext index on the titles
        database.execSQL("CREATE VIRTUAL TABLE " + EVENTS_TITLES_TABLE_NAME + " USING fts3(title TEXT, subtitle TEXT);");

        //SPONSORS
        database.execSQL("CREATE TABLE " + TABLE_NAME_SPONSOR
                + " (id INTEGER , name TEXT, img TEXT, url TEXT);");

        // Persons
        database.execSQL("CREATE VIRTUAL TABLE " + PERSONS_TABLE_NAME + " USING fts3(name TEXT);");

        // Events-to-Persons
        database.execSQL("CREATE TABLE " + EVENTS_PERSONS_TABLE_NAME
                + " (event_id INTEGER NOT NULL, person_id INTEGER NOT NULL, PRIMARY KEY(event_id, person_id));");
        database.execSQL("CREATE INDEX event_person_person_id_idx ON " + EVENTS_PERSONS_TABLE_NAME + " (person_id)");

        // Links
        database.execSQL("CREATE TABLE " + LINKS_TABLE_NAME + " (event_id INTEGER NOT NULL, url TEXT NOT NULL, description TEXT);");
        database.execSQL("CREATE INDEX link_event_id_idx ON " + LINKS_TABLE_NAME + " (event_id)");

        // Tracks
        database.execSQL("CREATE TABLE " + TRACKS_TABLE_NAME + " (id INTEGER PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL);");
        database.execSQL("CREATE UNIQUE INDEX track_main_idx ON " + TRACKS_TABLE_NAME + " (name, type)");

        // Days
        database.execSQL("CREATE TABLE " + DAYS_TABLE_NAME + " (_index INTEGER PRIMARY KEY, date INTEGER NOT NULL);");

        // Bookmarks
        database.execSQL("CREATE TABLE " + BOOKMARKS_TABLE_NAME + " (event_id INTEGER PRIMARY KEY);");
        database.execSQL(TABLE_KEY_SPEAKERS);
        database.execSQL(TABLE_SCHEDULE);
        database.execSQL(TABLE_SPEAKER_EVENT_RELATION);
        database.execSQL(TABLE_TRACKS);
        database.execSQL(TABLE_TRACK_VENUE);
        database.execSQL(TABLE_VENUE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Nothing to upgrade yet
    }
}
