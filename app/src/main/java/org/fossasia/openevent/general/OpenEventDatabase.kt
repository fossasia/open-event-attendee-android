package org.fossasia.openevent.general

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.attendees.AttendeeDao
import org.fossasia.openevent.general.attendees.AttendeeIdConverter
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao
import org.fossasia.openevent.general.event.EventIdConverter
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.topic.EventTopicIdConverter
import org.fossasia.openevent.general.event.topic.EventTopicsDao
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderDao
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.social.SocialLinksDao
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketIdConverter
import org.fossasia.openevent.general.ticket.TicketsDao

@Database(entities = [Event::class, User::class, SocialLink::class, Ticket::class, Attendee::class, EventTopic::class, Order::class], version = 1)
@TypeConverters(EventIdConverter::class, EventTopicIdConverter::class, TicketIdConverter::class, AttendeeIdConverter::class)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun ticketsDao(): TicketsDao

    abstract fun socialLinksDao(): SocialLinksDao

    abstract fun attendeesDao(): AttendeeDao

    abstract fun eventTopicsDao(): EventTopicsDao

    abstract fun orderDao(): OrderDao

}
