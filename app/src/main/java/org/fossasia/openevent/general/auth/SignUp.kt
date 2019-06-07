package org.fossasia.openevent.general.auth

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("user")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class SignUp(
    @Id(IntegerIdHandler::class)
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null
)
