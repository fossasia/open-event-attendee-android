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
import org.fossasia.openevent.general.auth.UserIdConverter
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao
import org.fossasia.openevent.general.event.EventIdConverter
import org.fossasia.openevent.general.event.subtopic.EventSubTopicConverter
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.topic.EventTopicConverter
import org.fossasia.openevent.general.event.topic.EventTopicsDao
import org.fossasia.openevent.general.event.types.EventTypeConverter
import org.fossasia.openevent.general.feedback.Feedback
import org.fossasia.openevent.general.feedback.FeedbackDao
import org.fossasia.openevent.general.notification.Notification
import org.fossasia.openevent.general.notification.NotificationDao
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderDao
import org.fossasia.openevent.general.sessions.Session
import org.fossasia.openevent.general.sessions.SessionDao
import org.fossasia.openevent.general.sessions.microlocation.MicroLocationConverter
import org.fossasia.openevent.general.sessions.sessiontype.SessionTypeConverter
import org.fossasia.openevent.general.sessions.track.TrackConverter
import org.fossasia.openevent.general.settings.Settings
import org.fossasia.openevent.general.settings.SettingsDao
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.social.SocialLinksDao
import org.fossasia.openevent.general.speakercall.SpeakersCallConverter
import org.fossasia.openevent.general.speakercall.SpeakersCall
import org.fossasia.openevent.general.speakercall.Proposal
import org.fossasia.openevent.general.speakercall.SpeakersCallDao
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerDao
import org.fossasia.openevent.general.speakers.SpeakerWithEvent
import org.fossasia.openevent.general.speakers.SpeakerWithEventDao
import org.fossasia.openevent.general.speakers.ListSpeakerIdConverter
import org.fossasia.openevent.general.sponsor.Sponsor
import org.fossasia.openevent.general.sponsor.SponsorDao
import org.fossasia.openevent.general.sponsor.SponsorWithEvent
import org.fossasia.openevent.general.sponsor.SponsorWithEventDao
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketDao
import org.fossasia.openevent.general.ticket.TicketIdConverter

@Database(entities = [Event::class, User::class, SocialLink::class, Ticket::class, Attendee::class,
    EventTopic::class, Order::class, CustomForm::class, Speaker::class, SpeakerWithEvent::class, Sponsor::class,
    SponsorWithEvent::class, Session::class, SpeakersCall::class, Feedback::class, Notification::class,
    Settings::class, Proposal::class], version = 8)
@TypeConverters(EventIdConverter::class, EventTopicConverter::class, EventTypeConverter::class,
    EventSubTopicConverter::class, TicketIdConverter::class, MicroLocationConverter::class, UserIdConverter::class,
    AttendeeIdConverter::class, ListAttendeeIdConverter::class, SessionTypeConverter::class, TrackConverter::class,
    SpeakersCallConverter::class, ListSpeakerIdConverter::class)
abstract class OpenEventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun ticketDao(): TicketDao

    abstract fun socialLinksDao(): SocialLinksDao

    abstract fun attendeeDao(): AttendeeDao

    abstract fun speakerDao(): SpeakerDao

    abstract fun speakerWithEventDao(): SpeakerWithEventDao

    abstract fun eventTopicsDao(): EventTopicsDao

    abstract fun orderDao(): OrderDao

    abstract fun sponsorDao(): SponsorDao

    abstract fun sponsorWithEventDao(): SponsorWithEventDao

    abstract fun sessionDao(): SessionDao

    abstract fun speakersCallDao(): SpeakersCallDao

    abstract fun feedbackDao(): FeedbackDao

    abstract fun notificationDao(): NotificationDao

    abstract fun settingsDao(): SettingsDao
}
