package hu.denield.chatly.util;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class Time {
    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy.MM.dd", cal).toString();
        return date;
    }

    public static String getTime(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("HH:mm:ss", cal).toString();
        return date;
    }
}
