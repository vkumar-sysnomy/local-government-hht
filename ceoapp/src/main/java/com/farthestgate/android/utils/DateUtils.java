package com.farthestgate.android.utils;

import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2/25/2016.
 */
public class DateUtils {
    public static final String ISO_DATE_TIME_FORMAT ="yyyy-MM-dd'T'HH:mm:ss";
    public static final SimpleDateFormat ISO8601_DATE_TIME_FORMAT = new SimpleDateFormat(ISO_DATE_TIME_FORMAT);
    public static final String DATE_FORMAT ="dd/MM/yyyy";
    public static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs



    public static String getCurrentDate() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    public static String getISO8601DateTime(){
        return ISO8601_DATE_TIME_FORMAT.format(new Date());
    }

    public static String getFormatedDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static Date getDate(String date, String format) {
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat(format).parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return parsedDate;

    }

    public static String changeDateFormat(String date, String fromFormat, String toFormat) {

        if(date == null || date.isEmpty()){
            return "";
        }

        String reformattedStr = null;
       /* SimpleDateFormat fromUser = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");*/

        SimpleDateFormat fromUser = new SimpleDateFormat(fromFormat);
        SimpleDateFormat myFormat = new SimpleDateFormat(toFormat);
        try {
            reformattedStr = myFormat.format(fromUser.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reformattedStr;
    }

    public static int daysBetween(Date d2, Date d1) {
        if (d2 != null && d1 != null) {
            int day = (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
            SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
            String d2Time = localDateFormat.format(d2);
            String d1Time = localDateFormat.format(d1);
            int time = d2Time.compareTo(d1Time);
            if (time > 0 || time < 0) {
                return ++day;
            } else {
                return day;
            }
        } else {
            return 0;
        }
    }

    public static boolean compareToDay(Date expDate, Date todays) {
        /*if (date1 == null || date2 == null) {
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date1).compareTo(sdf.format(date2));*/

        if (expDate == null || todays == null) {
            return false;
        } else if(expDate.before(todays)){
            return true;
        }
        return false;
    }

    public static long getDateInMilis(){
        Date currentDate = getDate(getCurrentDate(), DATE_FORMAT);
        return currentDate.getTime();
    }

    public static boolean isWithinRange(Date startDate, Date endDate) {
        Date currentDate = getDate(getCurrentDate(), "dd/MM/yyyy");
        return !(currentDate.before(startDate) || currentDate.after(endDate));
    }

    public static long getCurrentDateMinusDateInMilis(long dateInMilis){
        long currentDateInMilis = new Date().getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(currentDateInMilis- dateInMilis);
        return diffInMinutes;
    }

    /**
     * @return true if the supplied when is today else false
     */
    public static boolean isToday(long when) {
        Time time = new Time();
        time.set(when);

        int thenYear = time.year;
        int thenMonth = time.month;
        int thenMonthDay = time.monthDay;

        time.set(System.currentTimeMillis());
        return (thenYear == time.year)
                && (thenMonth == time.month)
                && (thenMonthDay == time.monthDay);
    }

    public static String getFormattedDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static long getDateInMillis(){
        Date currentDate = getDate(getCurrentDate(), DATE_FORMAT);
        return currentDate.getTime();
    }

}
