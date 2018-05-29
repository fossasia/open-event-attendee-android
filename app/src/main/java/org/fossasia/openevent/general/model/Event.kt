package org.fossasia.openevent.general.model

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Event(
        @Id(LongIdHandler::class)
        val id: Long? = null,
        val paymentCountry: String? = null,
        val paypalEmail: String? = null,
        val thumbnailImageUrl: String? = null,
        val schedulePublishedOn: String? = null,
        val paymentCurrency: String? = null,
        val organizerDescription: String? = null,
        val originalImageUrl: String? = null,
        val onsiteDetails: String? = null,
        val organizerName: String? = null,
        val largeImageUrl: String? = null,
        val timezone: String? = null,
        val deletedAt: String? = null,
        val ticketUrl: String? = null,
        val locationName: String? = null,
        val privacy: String? = null,
        val codeOfConduct: String? = null,
        val state: String? = null,
        var searchableLocationName: String? = null,
        val description: String? = null,
        val pentabarfUrl: String? = null,
        val xcalUrl: String? = null,
        val logoUrl: String? = null,
        val externalEventUrl: String? = null,
        val iconImageUrl: String? = null,
        val icalUrl: String? = null,
        val name: String? = null,
        val createdAt: String? = null,
        val bankDetails: String? = null,
        val chequeDetails: String? = null,
        val identifier: String? = null,
        val startsAt: String? = null,
        val endsAt: String? = null,
        val isComplete: Boolean = false,
        val latitude: Double? = null,
        val longitude: Double? = null,

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