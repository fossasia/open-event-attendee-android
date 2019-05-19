package org.fossasia.openevent.general.sessions.track

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper

class TrackConverter {

    @TypeConverter
    fun toTrack(json: String): Track? =
        jacksonObjectMapper().readerFor(Track::class.java).readValue<Track>(json)

    @TypeConverter
    fun toJson(track: Track?) = ObjectMapper().writeValueAsString(track)
}
