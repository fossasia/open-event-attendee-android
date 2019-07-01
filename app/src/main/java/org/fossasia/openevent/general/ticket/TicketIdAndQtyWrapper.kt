package org.fossasia.openevent.general.ticket

import java.io.Serializable

/**
 * A wrapper class around a list of Ticket Ids and Quantities.
 * This class allows for passing this data between frgments in a typesafe manner.
 *
 * @param value The list of ids and quantities
 */
data class TicketIdAndQtyWrapper(val value: List<Triple<Int, Int, Float>>) : Serializable
