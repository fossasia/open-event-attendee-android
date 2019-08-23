package org.fossasia.openevent.general

import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getCardType
import org.fossasia.openevent.general.utils.Utils.cardType
import org.junit.Test
import org.junit.Assert.assertEquals

class UtilsTest {

    private fun getCardNumber(type: cardType): String {
        return when (type) {
            cardType.AMERICAN_EXPRESS -> "371449635398431"
            cardType.DINERS_CLUB -> "30569309025904"
            cardType.DISCOVER -> "6011111111111117"
            cardType.MASTER_CARD -> "5555555555554444"
            cardType.UNIONPAY -> "6200000000000005"
            cardType.VISA -> "4242424242424242"
            cardType.NONE -> ""
        }
    }

    @Test
    fun `should get american express`() {
        val cardNumber = getCardNumber(Utils.cardType.AMERICAN_EXPRESS)
        assertEquals(getCardType(cardNumber), cardType.AMERICAN_EXPRESS)
    }

    @Test
    fun `should get diners club`() {
        val cardNumber = getCardNumber(Utils.cardType.DINERS_CLUB)
        assertEquals(getCardType(cardNumber), cardType.DINERS_CLUB)
    }

    @Test
    fun `should get discover`() {
        val cardNumber = getCardNumber(Utils.cardType.DISCOVER)
        assertEquals(getCardType(cardNumber), cardType.DISCOVER)
    }

    @Test
    fun `should get master card`() {
        val cardNumber = getCardNumber(Utils.cardType.MASTER_CARD)
        assertEquals(getCardType(cardNumber), cardType.MASTER_CARD)
    }

    @Test
    fun `should get union pay`() {
        val cardNumber = getCardNumber(Utils.cardType.UNIONPAY)
        assertEquals(getCardType(cardNumber), cardType.UNIONPAY)
    }

    @Test
    fun `should get visa`() {
        val cardNumber = getCardNumber(Utils.cardType.VISA)
        assertEquals(getCardType(cardNumber), cardType.VISA)
    }
}
