package org.fossasia.openevent.general.auth

import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("user")
data class UserId(
    @Id(IntegerIdHandler::class)
    val id: Long
)
