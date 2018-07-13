package org.fossasia.openevent.general.utils

import timber.log.Timber
import java.util.*

object CurrencyUtils {
    private var currencyLocaleMap = HashMap<Currency, Locale>()

    init {
        for (locale in Locale.getAvailableLocales()) {
            try {
                val currency = Currency.getInstance(locale)
                currencyLocaleMap.put(currency, locale)
            } catch (e: NullPointerException) {
                Timber.d(e, "Failed!")
            }
        }
    }

    fun getCurrencySymbol(currencyCode: String?): String? {
        val currency = Currency.getInstance(currencyCode)
        return currency.getSymbol(currencyLocaleMap[currency])
    }
}