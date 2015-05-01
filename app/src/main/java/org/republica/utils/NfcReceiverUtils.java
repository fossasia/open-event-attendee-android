package org.republica.utils;

import android.annotation.TargetApi;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Parcelable;

/**
 * NFC helper methods for receiving data sent by NfcSenderUtils. This class wraps API 10+ code.
 *
 * @author Christophe Beyls
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
class NfcReceiverUtils {

    public static boolean hasAppData(Intent intent) {
        return NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction());
    }

    public static byte[] extractAppData(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        return msg.getRecords()[0].getPayload();
    }
}
