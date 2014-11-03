package hu.denield.chatly.utils;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class Time {
    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }
}
