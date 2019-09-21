object Constants {
    val STRIPE_API_TOKEN = System.getenv(Strings.STRIPE_API_KEY) ?: "YOUR_API_KEY"
        get() = getStringWithQuotes(field)

    val MAPBOX_KEY = System.getenv(Strings.MAPBOX_KEY) ?: "pk.eyJ1IjoiYW5nbWFzMSIsImEiOiJjanNqZDd0N2YxN2Q5NDNuNTBiaGt6eHZqIn0.BCrxjW6rP_OuOuGtbhVEQg"
        get() = getStringWithQuotes(field)

    val PAYPAL_CLIENT_ID = System.getenv(Strings.PAYPAL_CLIENT_ID) ?: "YOUR_API_KEY"
        get() = getStringWithQuotes(field)

    val DEBUG_DEFAULT_BASE_URL = "https://open-event-api-dev.herokuapp.com/v1/"
        get() = getStringWithQuotes(field)

    val RELEASE_DEFAULT_BASE_URL = "https://api.eventyay.com/v1/"
        get() = getStringWithQuotes(field)

    const val DEBUG_FRONTEND_HOST = "open-event-fe.netlify.com"
    const val RELEASE_FRONTEND_HOST = "eventyay.com"

    private fun getStringWithQuotes(field: String): String {
        return "\"$field\""
    }
}
