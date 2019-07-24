package org.fossasia.openevent.general.speakers

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class ListSpeakerIdConverter {

    @TypeConverter
    fun fromListSpeakerId(speakerIdList: List<SpeakerId>): String {
        return ObjectMapper().writeValueAsString(speakerIdList)
    }

    @TypeConverter
    fun toListSpeakerId(speakerIdList: String): List<SpeakerId> {
        return jacksonObjectMapper().readValue(speakerIdList, object : TypeReference<List<SpeakerId>>() {})
    }
}
