package com.xcharge.common.utils;

import android.util.Log;
import com.xcharge.common.bean.JsonBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;

public class TimeUtils {
    public static final String dstoffset2timezoneid = "{\"-10:00\":\"US/Aleutian\",\"-09:00\":\"US/Alaska\",\"-08:00\":\"US/Pacific\",\"-07:00\":\"US/Mountain\",\"-06:00\":\"US/Indiana-Starke\",\"-05:00\":\"US/Michigan\",\"-04:00\":\"Chile/Continental\",\"-03:30\":\"Canada/Newfoundland\",\"-03:00\":\"Brazil/East\",\"-01:00\":\"Atlantic/Azores\",\"+00:00\":\"WET\",\"+01:00\":\"Poland\",\"+02:00\":\"Turkey\",\"+03:30\":\"Iran\",\"+04:00\":\"Asia/Baku\",\"+09:30\":\"Australia/Yancowinna\",\"+10:00\":\"Australia/Victoria\",\"+10:30\":\"Australia/Lord_Howe\",\"+12:00\":\"Pacific/Fiji\"}";
    public static final Map dstoffset2timezoneidMap = JsonBean.jsonToMap(dstoffset2timezoneid);
    public static final String offset2timezoneid = "{\"-12:00\":\"Etc/GMT+12\",\"-11:00\":\"US/Samoa\",\"-10:00\":\"US/Hawaii\",\"-09:30\":\"Pacific/Marquesas\",\"-09:00\":\"Pacific/Gambier\",\"-08:00\":\"Pacific/Pitcairn\",\"-07:00\":\"US/Arizona\",\"-06:00\":\"Pacific/Galapagos\",\"-05:00\":\"Jamaica\",\"-04:30\":\"America/Caracas\",\"-04:00\":\"Etc/GMT+4\",\"-03:00\":\"Etc/GMT+3\",\"-02:00\":\"Etc/GMT+2\",\"-01:00\":\"Etc/GMT+1\",\"+00:00\":\"Zulu\",\"+01:00\":\"Etc/GMT-1\",\"+02:00\":\"Libya\",\"+03:00\":\"Indian/Mayotte\",\"+04:00\":\"W-SU\",\"+04:30\":\"Asia/Kabul\",\"+05:00\":\"Indian/Maldives\",\"+05:30\":\"Asia/Kolkata\",\"+06:00\":\"Indian/Chagos\",\"+06:30\":\"Indian/Cocos\",\"+07:00\":\"Indian/Christmas\",\"+08:00\":\"Singapore\",\"+09:00\":\"ROK\",\"+09:30\":\"Australia/North\",\"+10:00\":\"Pacific/Yap\",\"+11:00\":\"Pacific/Ponape\",\"+11:30\":\"Pacific/Norfolk\",\"+12:00\":\"Pacific/Wallis\"}";
    public static final Map offset2timezoneidMap = JsonBean.jsonToMap(offset2timezoneid);

    public static long getTomorrowAt(int hour, int minite, int second) {
        Calendar today = new GregorianCalendar();
        today.add(5, 1);
        today.set(today.get(1), today.get(2), today.get(5), hour, minite, second);
        return today.getTimeInMillis();
    }

    public static String getHHmmFormat(long timestamp) {
        return new SimpleDateFormat("HH:mm").format(Long.valueOf(timestamp));
    }

    public static long getXCloudFormat(long timestamp, String offset) {
        if (TextUtils.isEmpty(offset)) {
            offset = "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        if (!TextUtils.isEmpty(offset)) {
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT" + offset));
        }
        return Long.parseLong(dateFormat.format(Long.valueOf(timestamp)));
    }

    public static long getTsFromXCloudFormat(String dateTime, String offset) {
        try {
            if (TextUtils.isEmpty(offset)) {
                offset = "";
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            if (!TextUtils.isEmpty(offset)) {
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT" + offset));
            }
            return dateFormat.parse(dateTime).getTime();
        } catch (Exception e) {
            Log.w("TimeUtils.getTsFromXCloudFormat", Log.getStackTraceString(e));
            return 0;
        }
    }

    public static String getISO8601Format(long timestamp, String offset) {
        if (TextUtils.isEmpty(offset)) {
            offset = "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" + offset);
        if (!TextUtils.isEmpty(offset)) {
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT" + offset));
        }
        return dateFormat.format(Long.valueOf(timestamp));
    }

    public static long getTsFromISO8601Format(String dateTime) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT" + dateTime.substring(19)));
            return dateFormat.parse(dateTime.substring(0, 19)).getTime();
        } catch (Exception e) {
            Log.w("TimeUtils.getTsFromISO8601Format", Log.getStackTraceString(e));
            return 0;
        }
    }

    public static long getDataTime(long timestamp, String HHmm) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timestamp);
        String yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(String.valueOf(yyyyMMdd) + StringUtils.SPACE + HHmm).getTime();
        } catch (ParseException e) {
            Log.w("TimeUtils.getDataTime", Log.getStackTraceString(e));
            return timestamp;
        }
    }

    public static String getTimezoneId(String tz, boolean useDaylightTime) {
        String id;
        if (useDaylightTime) {
            id = (String) dstoffset2timezoneidMap.get(tz);
            if (TextUtils.isEmpty(id)) {
                Log.w("TimeUtils.getTimezoneId", "not support DaylightTime for timezone: " + tz);
                id = (String) offset2timezoneidMap.get(tz);
                LogUtils.syslog("not support DaylightTime for timezone: " + tz + ", use normal timezone id: " + id);
            }
        } else {
            id = (String) offset2timezoneidMap.get(tz);
            if (TextUtils.isEmpty(id)) {
                Log.w("TimeUtils.getTimezoneId", "only support DaylightTime for timezone: " + tz);
                id = (String) dstoffset2timezoneidMap.get(tz);
                LogUtils.syslog("only support DaylightTime for timezone: " + tz + ", use DaylightTime timezone id: " + id);
            }
        }
        if (!TextUtils.isEmpty(id)) {
            return id;
        }
        Log.w("TimeUtils.getTimezoneId", "try to get available id for timezone: " + tz);
        String id2 = getAvailableTimezoneId(tz);
        LogUtils.syslog("get available id: " + id2 + " for timezone: " + tz + ", and this timezone is not in any predefined list");
        return id2;
    }

    public static String getAvailableTimezoneId(String tz) {
        Integer offsetMillis = getTimezoneOffset(tz);
        if (offsetMillis != null) {
            String[] tzIds = TimeZone.getAvailableIDs(offsetMillis.intValue());
            if (tzIds != null && tzIds.length > 0) {
                return tzIds[0];
            }
            Log.w("TimeUtils.getAvailableTimezoneId", "unavailable id for timezone: " + tz);
        } else {
            Log.w("TimeUtils.getAvailableTimezoneId", "illegal timezone: " + tz);
        }
        return null;
    }

    public static Integer getTimezoneOffset(String tz) {
        try {
            if (tz.startsWith("+") || tz.startsWith("-")) {
                String[] offset = tz.substring(1).split(":");
                if (offset.length == 2) {
                    int hour = Integer.parseInt(offset[0]);
                    int minute = Integer.parseInt(offset[1]);
                    if (hour >= 0 && hour <= 12 && minute >= 0 && minute < 60) {
                        int offsetMillis = (hour * 60 * 60 * 1000) + (minute * 60 * 1000);
                        if (tz.startsWith("-")) {
                            offsetMillis *= -1;
                        }
                        return Integer.valueOf(offsetMillis);
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean setSystemCountry(String country) {
        if (FileUtils.execShell("setprop persist.sys.country " + country.toUpperCase(), 4) == 0) {
            return true;
        }
        return false;
    }

    public static boolean setSystemTimeZone(String timezone) {
        if (FileUtils.execShell("setprop persist.sys.timezone " + timezone, 4) == 0) {
            return true;
        }
        return false;
    }

    public static boolean updateSystemTime(long ts) {
        if (FileUtils.execShell("date -s " + new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date(ts)), 4) == 0) {
            return true;
        }
        return false;
    }

    public static long getHHmmSeconds(long timestamp) {
        try {
            String[] time = getHHmmFormat(timestamp).split(":");
            if (time.length == 2) {
                return (Long.parseLong(time[0]) * 3600) + (Long.parseLong(time[1]) * 60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getWeek(long timestamp) {
        try {
            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            return c.get(7);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
