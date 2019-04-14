package org.fossasia.openevent.general.auth.change

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class ChangeRequestTokenResponse(val email: String, val id: Long, val name: String, val passwordChanged: Boolean)
