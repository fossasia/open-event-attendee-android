package org.fossasia.openevent.general.location

/**
 * Thrown when there isn't a location source available.
 * */
class NoLocationSourceException : Exception()

/**
 * Thrown when the user hasn't granted permission necessary to complete an action.
 * */
class LocationPermissionException : Exception()
