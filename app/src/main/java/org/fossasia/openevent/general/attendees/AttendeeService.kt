package org.fossasia.openevent.general.attendees

import io.reactivex.Completable
import io.reactivex.Single
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserDao

class AttendeeService(
    private val attendeeApi: AttendeeApi,
    private val attendeeDao: AttendeeDao,
    private val userDao: UserDao
) {
    fun postAttendee(attendee: Attendee): Single<Attendee> {
        return attendeeApi.postAttendee(attendee)
                .map {
                    attendeeDao.insertAttendee(it)
                    it
                }
    }

    fun getAttendeeDetails(id: Long): Single<User> {
        return userDao.getUser(id)
    }

    fun deleteAttendee(id: Long): Completable {
        return attendeeApi.deleteAttendee(id)
    }

    fun getCustomFormsForAttendees(id: Long): Single<List<CustomForm>> {
        val filter = """[{
                |   'and':[{
                |       'name':'form',
                |       'op':'eq',
                |       'val':'attendee'
                |    },{
                |       'name':'is-included',
                |       'op':'eq',
                |       'val':true
                |    }]
                |}]""".trimMargin().replace("'", "\"")
        return attendeeApi.getCustomFormsForAttendees(id, filter)
    }
}
