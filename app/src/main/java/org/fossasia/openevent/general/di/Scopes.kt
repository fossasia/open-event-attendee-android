package org.fossasia.openevent.general.di

/**
 * Enum class to collect all possible Fragment scopes for Koin DI
 * in one place. This list is expected to grow as Scopes are used in more
 * fragments.
 */
enum class Scopes {
    EVENTS_FRAGMENT,
    SIMILAR_EVENTS_FRAGMENT,
    FAVORITE_FRAGMENT,
    SEARCH_RESULTS_FRAGMENT
}
