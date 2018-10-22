package org.fossasia.openevent.general.ticket

import android.arch.persistence.room.TypeConverter

class TicketIdConverter {

    @TypeConverter
    fun fromTicketId(ticketId: TicketId): Long {
        return ticketId.id
    }

    @TypeConverter
    fun toTicketId(id: Long): TicketId {
        return TicketId(id)
    }
}
