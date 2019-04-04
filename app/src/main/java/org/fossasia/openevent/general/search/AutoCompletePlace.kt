package org.fossasia.openevent.general.search

import com.fasterxml.jackson.annotation.JsonAlias

data class AutoCompletePlaceInfo(
    val text: String,
    val place_name: String
)

data class AutoCompletePlaceSuggestions(
    @JsonAlias("features")
    val suggestions: List<AutoCompletePlaceInfo>
)


