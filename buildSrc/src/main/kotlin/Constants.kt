object Constants {
    val STRIPE_API_TOKEN = System.getenv("STRIPE_API_TOKEN") ?: "YOUR_API_KEY"
        get() = "\"$field\""
    val MAPBOX_KEY = System.getenv("MAPBOX_KEY") ?: "pk.eyJ1IjoiYW5nbWFzMSIsImEiOiJjanNqZDd0N2YxN2Q5NDNuNTBiaGt6eHZqIn0.BCrxjW6rP_OuOuGtbhVEQg"
        get() = "\"$field\""
    val PAYPAL_CLIENT_ID = System.getenv("PAYPAL_CLIENT_ID") ?: "YOUR_API_KEY"
        get() = "\"$field\""

    val DEBUG_DEFAULT_BASE_URL = "https://open-event-api-dev.herokuapp.com/v1/"
        get() = "\"$field\""
    const val DEBUG_FRONTEND_HOST = "open-event-fe.netlify.com"

    val RELEASE_DEFAULT_BASE_URL = "https://api.eventyay.com/v1/"
        get() = "\"$field\""
    const val RELEASE_FRONTEND_HOST = "eventyay.com"
}
