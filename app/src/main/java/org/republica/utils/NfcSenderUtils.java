package org.republica.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;

import org.republica.utils.NfcUtils.CreateNfcAppDataCallback;

import java.nio.charset.Charset;

/**
 * NFC helper methods for Android Beam foreground push. This class wraps API 14+ code.
 *
 * @author Christophe Beyls
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class NfcSenderUtils {

    public static boolean setAppDataPushMessageCallbackIfAvailable(Activity activity, final CreateNfcAppDataCallback callback) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter == null) {
            return false;
        }
        final String packageName = activity.getPackageName();
        adapter.setNdefPushMessageCallback(new CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                byte[] appData = callback.createNfcAppData();
                if (appData == null) {
                    return null;
                }
                NdefRecord[] records = new NdefRecord[]{createMimeRecord("application/" + packageName, appData),
                        NdefRecord.createApplicationRecord(packageName)};
                return new NdefMessage(records);
            }

        }, activity);
        return true;
    }

    private static NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
    }
}
