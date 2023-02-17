package net.xcharge.sdk.server.coder.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class DateUtil {
    private static Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public static int getMonthValue(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(2) + 1;
        return month;
    }

    public static int getDayValue(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(5);
        return day;
    }

    public static int getHourValue(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(11);
        return hour;
    }

    public static String formatDateString(String date) {
        try {
            Date date1 = DateUtils.parseDate(date, "yyyyMMddHHmmss");
            return DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            logger.error("", (Throwable) e);
            return null;
        }
    }

    public static String getDateString(long currentTimeMillis) {
        try {
            return DateFormatUtils.format(currentTimeMillis, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            logger.error("", (Throwable) e);
            return null;
        }
    }

    public static String formatTime(String time) {
        String result = StringUtils.leftPad(time, 4, "0");
        StringBuffer sb = new StringBuffer(result);
        return sb.insert(2, ':').toString();
    }

    public static Long timeNow() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return Long.valueOf(Long.parseLong(df.format(Long.valueOf(System.currentTimeMillis()))));
    }

    public static Long timeNow(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return Long.valueOf(Long.parseLong(df.format(Long.valueOf(date.getTime()))));
    }

    public static String timeNow19() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(Long.valueOf(System.currentTimeMillis()));
    }

    public static Date prase19(String date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return df.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parse_yyyyMMddHHmmss(String date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            return df.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String parse_yyyyMMddHHmmss(long date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d = new Date(date);
            return df.format(d);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getNow19() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return df.format(new Date());
        } catch (Exception e) {
            return null;
        }
    }
}