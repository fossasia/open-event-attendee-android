package org.fossasia.openevent.general

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.social.SocialLinksDao

@Database(entities = [Event::class, User::class, SocialLink::class], version = 1)
@TypeConverters(Converter::class)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun socialLinksdDao(): SocialLinksDao
}
