package org.fossasia.openevent.general

import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao

@Database(entities = arrayOf(Event::class, User::class), version = 1)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao
}
