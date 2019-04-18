package org.fossasia.openevent.general.speakers

import androidx.lifecycle.LiveData
import io.reactivex.Single

class SpeakerService(

    private val speakerApi: SpeakerApi,
    private val speakerDao: SpeakerDao,
    private val speakerWithEventDao: SpeakerWithEventDao

) {
    fun fetchSpeakersForEvent(id: Long): Single<List<Speaker>> {
        return speakerApi.getSpeakerforEvent(id).doOnSuccess { speakerList ->
            speakerList.forEach {
                speakerDao.insertSpeaker(it)
                speakerWithEventDao.insert(SpeakerWithEvent(id, it.id))
            }
        }
    }

    fun fetchSpeakersFromDb(id: Long): LiveData<List<Speaker>> {
        return speakerWithEventDao.getSpeakerWithEventId(id)
    }
}
