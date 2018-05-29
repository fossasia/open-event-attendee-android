package org.fossasia.openevent.general.model

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("user")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class User(
    @Id(IntegerIdHandler::class)
    val id: Int,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val contact: String? = null,
    val details: String? = null,
    val thumbnailImageUrl: String? = null,
    val iconImageUrl: String? = null,
    val smallImageUrl: String? = null,
    val avatarUrl: String? = null,
    val facebookUrl: String? = null,
    val twitterUrl: String? = null,
    val instagramUrl: String? = null,
    val googlePlusUrl: String? = null,
    val originalImageUrl: String? = null,

    val isVerified: Boolean = false,
    val isAdmin: Boolean,
    val isSuperAdmin: Boolean,
    val createdAt: String? = null,
    val lastAccessedAt: String? = null,
    val deletedAt: String? = null
)