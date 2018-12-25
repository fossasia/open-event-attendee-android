package org.fossasia.openevent.general.auth

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("user")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class User(
    @Id(IntegerIdHandler::class)
    @PrimaryKey
    val id: Long,
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
    val isAdmin: Boolean? = false,
    val isSuperAdmin: Boolean? = false,
    val createdAt: String? = null,
    val lastAccessedAt: String? = null,
    val deletedAt: String? = null
)
