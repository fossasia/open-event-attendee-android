package org.fossasia.openevent.general.model

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Attributes(
        val identifier: String,
        val name: String,
        val organizerDescription: String? = null,
        val startsAt: String,
        val originalImageUrl: String? = null,
        val description: String? = null
)
