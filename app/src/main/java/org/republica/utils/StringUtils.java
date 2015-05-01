package org.republica.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Various methods to transform strings
 *
 * @author Christophe Beyls
 */
public class StringUtils {

    private final static String TAG = "STRING_UTIL";
    /**
     * Mirror of the unicode table from 00c0 to 017f without diacritics.
     */
    private static final String tab00c0 = "AAAAAAACEEEEIIII" + "DNOOOOO\u00d7\u00d8UUUUYI\u00df" + "aaaaaaaceeeeiiii" + "\u00f0nooooo\u00f7\u00f8uuuuy\u00fey"
            + "AaAaAaCcCcCcCcDd" + "DdEeEeEeEeEeGgGg" + "GgGgHhHhIiIiIiIi" + "IiJjJjKkkLlLlLlL" + "lLlNnNnNnnNnOoOo" + "OoOoRrRrRrSsSsSs" + "SsTtTtTtUuUuUuUu"
            + "UuUuWwYyYZzZzZzF";

    private static final String ROOM_DRAWABLE_PREFIX = "room_";

    /**
     * Returns string without diacritics - 7 bit approximation.
     *
     * @param source string to convert
     * @return corresponding string without diacritics
     */
    public static String removeDiacritics(String source) {
        final int length = source.length();
        char[] result = new char[length];
        char c;
        for (int i = 0; i < length; i++) {
            c = source.charAt(i);
            if (c >= '\u00c0' && c <= '\u017f') {
                c = tab00c0.charAt((int) c - '\u00c0');
            }
            result[i] = c;
        }
        return new String(result);
    }

    /**
     * Replaces all groups of non-alphanumeric chars in source with a single replacement char.
     */
    private static String replaceNonAlphaGroups(String source, char replacement) {
        final int length = source.length();
        char[] result = new char[length];
        char c;
        boolean replaced = false;
        int size = 0;
        for (int i = 0; i < length; i++) {
            c = source.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                result[size++] = c;
                replaced = false;
            } else {
                // Skip quote
                if ((c != '’') && !replaced) {
                    result[size++] = replacement;
                    replaced = true;
                }
            }
        }
        return new String(result, 0, size);
    }

    /**
     * Removes all non-alphanumeric chars at the beginning and end of source.
     *
     * @param source
     * @return
     */
    private static String trimNonAlpha(String source) {
        int st = 0;
        int len = source.length();

        while ((st < len) && !Character.isLetterOrDigit(source.charAt(st))) {
            st++;
        }
        while ((st < len) && !Character.isLetterOrDigit(source.charAt(len - 1))) {
            len--;
        }
        return ((st > 0) || (len < source.length())) ? source.substring(st, len) : source;
    }

    /**
     * Transforms a name to a slug identifier to be used in a FOSDEM URL.
     *
     * @param source
     * @return
     */
    public static String toSlug(String source) {
        return replaceNonAlphaGroups(trimNonAlpha(removeDiacritics(source)), '_').toLowerCase(Locale.US);
    }

    public static CharSequence trimEnd(CharSequence source) {
        int pos = source.length() - 1;
        while ((pos >= 0) && Character.isWhitespace(source.charAt(pos))) {
            pos--;
        }
        pos++;
        return (pos < source.length()) ? source.subSequence(0, pos) : source;
    }

    /**
     * Converts a room name to a local drawable resource name, by stripping non-alpha chars and converting to lower case. Any letter following a digit will be
     * ignored, along with the rest of the string.
     *
     * @return
     */
    public static String roomNameToResourceName(String roomName) {
        StringBuilder builder = new StringBuilder(ROOM_DRAWABLE_PREFIX.length() + roomName.length());
        builder.append(ROOM_DRAWABLE_PREFIX);
        int size = roomName.length();
        boolean lastDigit = false;
        for (int i = 0; i < size; ++i) {
            char c = roomName.charAt(i);
            if (Character.isLetter(c)) {
                if (lastDigit) {
                    break;
                }
                builder.append(Character.toLowerCase(c));
            } else if (Character.isDigit(c)) {
                builder.append(c);
                lastDigit = true;
            }
        }
        return builder.toString();
    }


    public static Date StringToDate(String sDate, String sTime) {
        // TODO: Remove hard coding of month
        if (sTime != null) {
            sTime = sTime.replaceAll(" ", "");
            String amPm = sTime.substring(Math.max(sTime.length() - 2, 0));
            String time = sTime.substring(0, sTime.length() - 2);
            String[] hrMin = time.split(":");
            Calendar cal = Calendar.getInstance();
            String[] date = sDate.split(" ");
            int hour = Integer.parseInt(hrMin[0]);
            int min = Integer.parseInt(hrMin[1]);

            if (amPm.equals("PM") || amPm.equals("pm") || amPm.equals("Pm") || amPm.equals("pM")) {
                if (hour > 0 && hour < 12) {
                    hour += 12;
                }
            } else if (amPm.equals("AM") || amPm.equals("am") || amPm.equals("Am") || amPm.equals("aM"))
                if (hour == 12) {
                    hour = 0;
                }
            cal.set(2015, Calendar.MARCH, Integer.parseInt(date[1]), hour, min);
            return cal.getTime();
        }


        Calendar cal = Calendar.getInstance();
        String[] date = sDate.split(" ");
        cal.set(2015, Calendar.MARCH, Integer.parseInt(date[1]));
        if (sTime != null) {
        }
        return cal.getTime();
    }

    public static String replaceUnicode(String data) {
        return data.replaceAll("'", "''");
    }
}
