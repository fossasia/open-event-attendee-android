package org.fossasia.openevent.general.event

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Event(
        @Id(LongIdHandler::class)
        val id: Long,
        val paymentCountry: String?,
        val paypalEmail: String?,
        val thumbnailImageUrl: String?,
        val schedulePublishedOn: String?,
        val paymentCurrency: String?,
        val organizerDescription: String?,
        val originalImageUrl: String?,
        val onsiteDetails: String?,
        val organizerName: String?,
        val largeImageUrl: String?,
        val timezone: String,
        val deletedAt: String?,
        val ticketUrl: String?,
        val locationName: String?,
        val privacy: String = "public",
        val codeOfConduct: String?,
        val state: String?,
        val searchableLocationName: String?,
        val description: String?,
        val pentabarfUrl: String?,
        val xcalUrl: String?,
        val logoUrl: String?,
        val externalEventUrl: String?,
        val iconImageUrl: String?,
        val icalUrl: String?,
        val name: String,
        val createdAt: String?,
        val bankDetails: String?,
        val chequeDetails: String?,
        val identifier: String,
        val startsAt: String,
        val endsAt: String,
        val isComplete: Boolean = false,
        val latitude: Double?,
        val longitude: Double?,

        val canPayByStripe: Boolean = false,
        val canPayByCheque: Boolean = false,
        val canPayByBank: Boolean = false,
        val canPayByPaypal: Boolean = false,
        val canPayOnsite: Boolean = false,
        val isSponsorsEnabled: Boolean = false,
        val hasOrganizerInfo: Boolean = false,
        val isSessionsSpeakersEnabled: Boolean = false,
        val isTicketingEnabled: Boolean = false,
        val isTaxEnabled: Boolean = false,
        val isMapShown: Boolean = false
)