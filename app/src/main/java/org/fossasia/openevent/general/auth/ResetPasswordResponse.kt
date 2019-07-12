package org.fossasia.openevent.general.auth

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id

class ResetPasswordResponse(
    @Id(LongIdHandler::class)
    val id: Long? = null,
    val email: String? = null,
    val name: String? = null
)
