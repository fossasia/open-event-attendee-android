package org.republica.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility class to perform HTTP requests.
 *
 * @author Christophe Beyls
 */
public class HttpUtils {

    private static final int DEFAULT_TIMEOUT = 10000;

    static {
        // HTTP connection reuse was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        // Bypass hostname verification
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        // Trust all HTTPS certificates
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        }};
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream get(Context context, String path) throws IOException {
        return get(context, new URL(path), null, null, null).inputStream;
    }

    public static HttpResult get(Context context, String path, String lastModified,
                                 String progressAction, String progressExtra) throws IOException {
        return get(context, new URL(path), lastModified, progressAction, progressExtra);
    }

    public static HttpResult get(final Context context, URL url, String lastModified,
                                 final String progressAction, final String progressExtra) throws IOException {
        HttpResult result = new HttpResult();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(DEFAULT_TIMEOUT);
        connection.setConnectTimeout(DEFAULT_TIMEOUT);
        if (lastModified != null) {
            connection.addRequestProperty("If-Modified-Since", lastModified);
        }
        connection.connect();

        result.lastModified = connection.getHeaderField("Last-Modified");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect();

            if ((responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) && (lastModified != null)) {
                // Cached result is still valid; return an empty response
                return result;
            }

            throw new IOException("Server returned response code: " + responseCode);
        }

        final int length = connection.getContentLength();
        result.inputStream = new BufferedInputStream(connection.getInputStream());
        if ((progressAction == null) || (length == -1)) {
            // No progress support
            return result;
        }

        // Broadcast the progression in percents, with a precision of 1/10 of the total file size
        result.inputStream = new ByteCountInputStream(result.inputStream,
                new ByteCountInputStream.ByteCountListener() {

                    private LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

                    @Override
                    public void onNewCount(int byteCount) {
                        // Cap percent to 100
                        int percent = (byteCount >= length) ? 100 : byteCount * 100 / length;
                        lbm.sendBroadcast(new Intent(progressAction).putExtra(progressExtra, percent));
                    }
                }, length / 10);
        return result;
    }

    public static class HttpResult {
        // Will be null when the local content is up-to-date
        public InputStream inputStream;
        public String lastModified;
    }
}
