package org.fossasia.openevent.general.event

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.topic.EventTopicApi
import org.fossasia.openevent.general.event.topic.EventTopicsDao

class EventService(private val eventApi: EventApi, private val eventDao: EventDao, private val eventTopicApi: EventTopicApi, private val eventTopicsDao: EventTopicsDao) {

    fun getEvents(): Flowable<List<Event>> {
        val eventsFlowable = eventDao.getAllEvents()
        return eventsFlowable.switchMap {
            if (it.isNotEmpty())
                eventsFlowable
            else
                eventApi.getEvents()
                        .map {
                            eventDao.insertEvents(it)
                            eventTopicsDao.insertEventTopics(getEventTopicList(it))
                        }
                        .toFlowable()
                        .flatMap {
                            eventsFlowable
                        }
        }
    }

    fun getEventTopicList(eventsList: List<Event>): List<EventTopic> {
        val eventTopicList = ArrayList<EventTopic>()

        Observable.just(eventsList)
                .flatMap{ Observable.fromIterable(eventsList) }
                .filter { it.eventTopic != null }
                .subscribe { event ->
                    val eventTopic = event.eventTopic
                    eventTopic?.let {
                        eventTopicList.add(it)
                    }
                }

        return eventTopicList
    }

    fun getEventTopics(): Flowable<List<EventTopic>> {
        return eventTopicsDao.getAllEventTopics()
    }

    fun getSearchEvents(eventName: String): Single<List<Event>> {
        return eventApi.searchEvents("name", eventName)
                .map {
                    eventDao.insertEvents(it)
                    it
                }
    }

    fun getFavoriteEvents(): Flowable<List<Event>> {
        return eventDao.getFavoriteEvents()
    }

    fun getEventsByLocation(locationName: String): Single<List<Event>> {
        return eventApi.searchEvents("name", locationName).map {
            eventDao.insertEvents(it)
            eventTopicsDao.insertEventTopics(getEventTopicList(it))
            it
        }
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun setFavorite(eventId: Long, favourite: Boolean): Completable {
        return Completable.fromAction {
            eventDao.setFavorite(eventId, favourite)
        }
    }

    fun getSimilarEvents(id: Long): Flowable<List<Event>> {
        val eventsFlowable = eventDao.getAllSimilarEvents(id)
        return eventsFlowable.switchMap {
            if (it.isNotEmpty())
                eventsFlowable
            else
                eventTopicApi.getEventsUnderTopicId(id)
                        .toFlowable()
                        .map {
                            eventDao.insertEvents(it)
                        }
                        .flatMap {
                            eventsFlowable
                        }
        }
    }
}