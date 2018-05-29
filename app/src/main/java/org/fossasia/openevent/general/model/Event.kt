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
        var id: Long? = null,
        var paymentCountry: String? = null,
        var paypalEmail: String? = null,
        var thumbnailImageUrl: String? = null,
        var schedulePublishedOn: String? = null,
        var paymentCurrency: String? = null,
        var organizerDescription: String? = null,
        var originalImageUrl: String? = null,
        var onsiteDetails: String? = null,
        var organizerName: String? = null,
        var largeImageUrl: String? = null,
        var timezone: String? = null,
        var deletedAt: String? = null,
        var ticketUrl: String? = null,
        var locationName: String? = null,
        var privacy: String? = null,
        var codeOfConduct: String? = null,
        var state: String? = null,
        var searchableLocationName: String? = null,
        var description: String? = null,
        var pentabarfUrl: String? = null,
        var xcalUrl: String? = null,
        var logoUrl: String? = null,
        var externalEventUrl: String? = null,
        var iconImageUrl: String? = null,
        var icalUrl: String? = null,
        var name: String? = null,
        var createdAt: String? = null,
        var bankDetails: String? = null,
        var chequeDetails: String? = null,
        var identifier: String? = null,
        var startsAt: String? = null,
        var endsAt: String? = null,
        var isComplete: Boolean = false,
        var latitude: Double? = null,
        var longitude: Double? = null,

        var canPayByStripe: Boolean = false,
        var canPayByCheque: Boolean = false,
        var canPayByBank: Boolean = false,
        var canPayByPaypal: Boolean = false,
        var canPayOnsite: Boolean = false,
        var isSponsorsEnabled: Boolean = false,
        var hasOrganizerInfo: Boolean = false,
        var isSessionsSpeakersEnabled: Boolean = false,
        var isTicketingEnabled: Boolean = false,
        var isTaxEnabled: Boolean = false,
        var isMapShown: Boolean = false
)