package org.fossasia.openevent.general

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.attendees.AttendeeDao
import org.fossasia.openevent.general.attendees.AttendeeIdConverter
import org.fossasia.openevent.general.attendees.ListAttendeeIdConverter
import org.fossasia.openevent.general.attendees.forms.CustomForm
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
import org.fossasia.openevent.general.ticket.TicketDao
import org.fossasia.openevent.general.ticket.TicketIdConverter

@Database(entities = [Event::class, User::class, SocialLink::class, Ticket::class, Attendee::class,
    EventTopic::class, Order::class, CustomForm::class], version = 2)
@TypeConverters(EventIdConverter::class, EventTopicIdConverter::class, TicketIdConverter::class,
    AttendeeIdConverter::class, ListAttendeeIdConverter::class)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun ticketDao(): TicketDao

    abstract fun socialLinksDao(): SocialLinksDao

    abstract fun attendeeDao(): AttendeeDao

    abstract fun eventTopicsDao(): EventTopicsDao

    abstract fun orderDao(): OrderDao
}
