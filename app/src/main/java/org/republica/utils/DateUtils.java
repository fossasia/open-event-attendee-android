package org.republica.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    private static final TimeZone BELGIUM_TIME_ZONE = TimeZone.getTimeZone("GMT+1");

    private static final DateFormat TIME_DATE_FORMAT = withBelgiumTimeZone(SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault()));

    public static TimeZone getBelgiumTimeZone() {
        return BELGIUM_TIME_ZONE;
    }

    public static DateFormat withBelgiumTimeZone(DateFormat format) {
        format.setTimeZone(BELGIUM_TIME_ZONE);
        return format;
    }

    public static DateFormat getTimeDateFormat() {
        return TIME_DATE_FORMAT;
    }
}
