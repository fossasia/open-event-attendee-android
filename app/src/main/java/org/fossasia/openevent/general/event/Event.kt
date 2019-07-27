package org.fossasia.openevent.general.event

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.subtopic.EventSubTopic
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.types.EventType
import org.fossasia.openevent.general.speakercall.SpeakersCall

@Type("event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Event(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String,
    val identifier: String,
    val startsAt: String,
    val endsAt: String,
    val timezone: String,
    val privacy: String = "public",
    val paymentCountry: String? = null,
    val paypalEmail: String? = null,
    val thumbnailImageUrl: String? = null,
    val schedulePublishedOn: String? = null,
    val paymentCurrency: String? = null,
    val ownerDescription: String? = null,
    val originalImageUrl: String? = null,
    val onsiteDetails: String? = null,
    val ownerName: String? = null,
    val largeImageUrl: String? = null,
    val deletedAt: String? = null,
    val ticketUrl: String? = null,
    val locationName: String? = null,
    val codeOfConduct: String? = null,
    val state: String? = null,
    val searchableLocationName: String? = null,
    val description: String? = null,
    val pentabarfUrl: String? = null,
    val xcalUrl: String? = null,
    val logoUrl: String? = null,
    val externalEventUrl: String? = null,
    val iconImageUrl: String? = null,
    val icalUrl: String? = null,
    val createdAt: String? = null,
    val bankDetails: String? = null,
    val chequeDetails: String? = null,
    val isComplete: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val refundPolicy: String? = null,

    val canPayByStripe: Boolean = false,
    val canPayByCheque: Boolean = false,
    val canPayByBank: Boolean = false,
    val canPayByPaypal: Boolean = false,
    val canPayOnsite: Boolean = false,
    val isSponsorsEnabled: Boolean = false,
    val hasOwnerInfo: Boolean = false,
    val isSessionsSpeakersEnabled: Boolean = false,
    val isTicketingEnabled: Boolean = false,
    val isTaxEnabled: Boolean = false,
    val isMapShown: Boolean = false,
    var favorite: Boolean = false,
    var favoriteEventId: Long? = null,
    @ColumnInfo(index = true)
    @Relationship("event-topic", resolve = true)
    val eventTopic: EventTopic? = null,
    @ColumnInfo(index = true)
    @Relationship("event-type", resolve = true)
    val eventType: EventType? = null,
    @ColumnInfo(index = true)
    @Relationship("event-sub-topic", resolve = true)
    val eventSubTopic: EventSubTopic? = null,
    @ColumnInfo(index = true)
    @Relationship("speakers-call", resolve = true)
    val speakersCall: SpeakersCall? = null

)
