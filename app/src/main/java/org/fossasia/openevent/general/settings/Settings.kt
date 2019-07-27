package org.fossasia.openevent.general.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("setting")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Settings(
    @Id(IntegerIdHandler::class)
    @PrimaryKey
    val id: Int? = null,
    val appName: String? = null,
    val tagline: String? = null,
    val isPaypalActivated: Boolean = false,
    val isStripeActivated: Boolean = false,
    val isOmiseActivated: Boolean = false,
    val frontendUrl: String? = null,
    val cookiePolicy: String? = null,
    val cookiePolicyLink: String? = null,
    val orderExpiryTime: Int? = null
)
