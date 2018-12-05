package com.adai.gkdnavi.utils;

import android.content.Context;

import com.adai.gkdnavi.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by admin on 2016/8/10.
 */
public class TimeUtils {
    public static String getTimeStr(Context context, String pattern, String time) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date date = format.parse(time);
            Date current = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            calendar.setTime(current);
            int currentyear = calendar.get(Calendar.YEAR);
            int currentmonth = calendar.get(Calendar.MONTH);
            int currentday = calendar.get(Calendar.DAY_OF_MONTH);
            int currenthour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentmin = calendar.get(Calendar.MINUTE);
            if (currentyear > year) {
                if ((12 - month + currentmonth) <= 12 && (currentyear - year) == 1) {
                    return String.format("%d%s", (12 - month + currentmonth), context.getResources().getString(R.string.month_ago));
                }
                return String.format("%d%s", (currentyear - year), context.getResources().getString(R.string.year_ago));
            } else if (currentmonth > month) {
                if ((31 - day + currentday) < 31 && (currentmonth - month) == 1) {
                    return String.format("%d%s", (31 - day + currentday), context.getResources().getString(R.string.day_ago));
                }
                return String.format("%d%s", (currentmonth - month), context.getResources().getString(R.string.month_ago));
            } else if (currentday > day) {
                return String.format("%d%s", (currentday - day), context.getResources().getString(R.string.day_ago));
            } else if (currenthour > hour) {
                return String.format("%d%s", (currenthour - hour), context.getResources().getString(R.string.hour_ago));
            } else if (currentmin > min) {
                return String.format("%d%s", (currentmin - min), context.getResources().getString(R.string.minute_ago));
            } else {
                return context.getResources().getString(R.string.default_ago);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String secondsToHours(int remainTime) {
        String time = "";
        Integer h = remainTime / 3600;
        Integer m = (remainTime % 3600) / 60;
        Integer s = remainTime % 60;
        if (h < 10) {
            time = "0" + h.toString();
        } else {
            time = h.toString();
        }
        time = time + ":";
        if (m < 10) {
            time = time + "0" + m.toString();
        } else {
            time = time + m.toString();
        }
        time = time + ":";
        if (s < 10) {
            time = time + "0" + s.toString();
        } else {
            time = time + s.toString();
        }
        return time;
    }

    public static String secondsToMinutes(int remainTime) {
        String time;
        Integer m = (remainTime % 3600) / 60;
        Integer s = remainTime % 60;
        time = String.format(Locale.CHINESE, "%02d:%02d", m, s);
        return time;
    }
}
