package org.fossasia.openevent.general.utils

import org.fossasia.openevent.general.utils.StringUtils.isEmpty

class Error {

    var title: String? = null
    var detail: String? = null
    var pointer: String? = null
    var code: String? = null

    override fun toString(): String {

        if (isEmpty(title)) {
            if (!isEmpty(detail)) {
                return (if (isEmpty(pointer)) {
                    detail
                } else {
                    "$detail - $pointer"
                }).toString()
            }
        } else {
            return if (isEmpty(pointer)) {
                "$title: $detail"
            } else {
                "$title: $detail - $pointer"
            }
        }

        return ""
    }
}
