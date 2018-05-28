package org.fossasia.openevent.general.model

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Attributes(var identifier: String? = null,
                      var name: String? = null,
                      var organizerDescription: String? = null,
                      var startsAt: String? = null,
                      var originalImageUrl: String? = null,
                      var description: String? = null)
