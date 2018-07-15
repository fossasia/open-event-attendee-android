package org.fossasia.openevent.general.auth.forgot

data class SubmitToken(
        val token: String,
        val password: String
)