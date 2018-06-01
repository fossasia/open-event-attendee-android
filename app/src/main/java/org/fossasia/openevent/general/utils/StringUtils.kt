package org.fossasia.openevent.general.utils

fun String?.nullToEmpty(): String {
    return this ?: ""
}