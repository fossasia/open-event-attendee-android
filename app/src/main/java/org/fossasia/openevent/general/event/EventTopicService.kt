package org.fossasia.openevent.general.event

import io.reactivex.Flowable

class EventTopicService(private val eventApi: EventTopicApi) {

    fun getSimilarEvents(id: Long): Flowable<List<Event>> {
        return eventApi.getEventsUnderTopicId(id).toFlowable()
    }

    fun getEventTopic(id: Long): Flowable<EventTopic> {
        return eventApi.getEventTopic(id).toFlowable()
    }

    fun getEventTopicOfEvent(id: Long): Flowable<EventTopic> {
        return eventApi.getEventTopicOfEvent(id).toFlowable()
    }

    fun getEventTopics(): Flowable<List<EventTopic>> {
        return eventApi.getEventTopics().toFlowable()
    }
}