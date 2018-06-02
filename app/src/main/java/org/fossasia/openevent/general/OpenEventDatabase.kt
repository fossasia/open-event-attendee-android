package org.fossasia.openevent.general

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database
import android.content.Context
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao

@Database(entities = arrayOf(Event::class, User::class), version = 1)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    companion object {
        private var INSTANCE: OpenEventDatabase? = null

        internal fun getEventDatabase(context: Context): OpenEventDatabase? {
            if (INSTANCE == null) {
                synchronized(OpenEventDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                OpenEventDatabase::class.java, "open_event_database")
                                .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}
