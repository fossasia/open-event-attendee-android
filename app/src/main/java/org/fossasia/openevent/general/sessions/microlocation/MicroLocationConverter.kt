package org.fossasia.openevent.general.sessions.microlocation

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class MicroLocationConverter {
    @TypeConverter
    fun toMicroLoation(json: String) =
        jacksonObjectMapper().readerFor(MicroLocation::class.java).readValue<MicroLocation>(json)

    @TypeConverter
    fun toJson(microLocation: MicroLocation?) = ObjectMapper().writeValueAsString(microLocation)
}
