package org.fossasia.openevent.general

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao
import org.fossasia.openevent.general.event.Ticket
import org.fossasia.openevent.general.event.TicketsDao

@Database(entities = [Event::class, User::class, Ticket::class], version = 1)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun ticketsDao(): TicketsDao
}
