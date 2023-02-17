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

public class DateUtil {
    private static Logger logger = LoggerFactory.getLogger((Class<?>) DateUtil.class);

    public static int getMonthValue(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(2) + 1;
    }

    public static int getDayValue(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(5);
    }

    public static int getHourValue(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(11);
    }

    public static String formatDateString(String date) {
        try {
            return DateFormatUtils.format(DateUtils.parseDate(date, "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss");
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
        return new StringBuffer(StringUtils.leftPad(time, 4, "0")).insert(2, ':').toString();
    }

    public static Long timeNow() {
        return Long.valueOf(Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(Long.valueOf(System.currentTimeMillis()))));
    }

    public static Long timeNow(Date date) {
        return Long.valueOf(Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(Long.valueOf(date.getTime()))));
    }

    public static String timeNow19() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(System.currentTimeMillis()));
    }

    public static Date prase19(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parse_yyyyMMddHHmmss(String date) {
        try {
            return new SimpleDateFormat("yyyyMMddHHmmss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String parse_yyyyMMddHHmmss(long date) {
        try {
            return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(date));
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getNow19() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        } catch (Exception e) {
            return null;
        }
    }
}
