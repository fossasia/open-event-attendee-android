package org.fossasia.openevent.general.auth.forgot

data class SubmitTokenResponse(
        val id: String,
        val email: String,
        var name: String
)