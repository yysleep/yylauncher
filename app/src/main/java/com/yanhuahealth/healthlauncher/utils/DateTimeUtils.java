/**
 * 
 */
package com.yanhuahealth.healthlauncher.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author steven
 */
@SuppressLint({ "UseSparseArrays", "SimpleDateFormat" })
public class DateTimeUtils {
    
    // 常用的日期时间格式
    public static final String FMT_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String FMT_YMD = "yyyy-MM-dd";
    public static final String FMT_YM = "yyyy-MM";
    public static final String FMT_HMS = "HH:mm:ss";
    public static final String FMT_YMDHMS2 = "yyyyMMddHHmmss";
    
    // 每个时间点对应的时段
    public static final int PERIOD_MORNING = 1; // 上午 [8-11)
    public static final int PERIOD_MIDDAY = 2; // 中午 [11-13)
    public static final int PERIOD_AFTERNOON = 3; // 下午 [13-17)
    public static final int PERIOD_NIGHT = 4; // 傍晚 [17-18)
    public static final int PERIOD_EVENING = 5; // 晚上 [18-23)
    public static final int PERIOD_MIDNIGHT = 6; // 午夜 [23-1)
    public static final int PERIOD_DAWN = 7; // 凌晨 [1-6)
    public static final int PERIOD_MATINAL = 8; // 清晨 [6-8)

    public static final String[] PERIOD_OF_DAY = { "未知", "上午", "中午", "下午",
            "傍晚", "晚上", "午夜", "凌晨", "清晨" };

    /**
     * 获取指定时间点对应的时段
     */
    public static int periodOfTimeInCal(Calendar cal) {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour >= 1 && hour < 6) {
            return 7;
        } else if (hour >= 6 && hour < 8) {
            return 8;
        } else if (hour >= 8 && hour < 11) {
            return 1;
        } else if (hour >= 11 && hour < 13) {
            return 2;
        } else if (hour >= 13 && hour < 17) {
            return 3;
        } else if (hour >= 17 && hour < 18) {
            return 4;
        } else if (hour >= 18 && hour < 23) {
            return 5;
        } else if (hour >= 23 || hour < 1) {
            return 6;
        }

        return 0;
    }

    /**
     * 获取指定时间(单位: 毫秒)对应的每日时段
     */
    public static int periodOfTime(long t) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(t);
        return periodOfTimeInCal(cal);
    }

    /**
     * 获取当前时间对应的时段
     */
    public static int periodOfCurrent() {
        return periodOfTimeInCal(Calendar.getInstance());
    }

    /**
     * 获取当前时间对应的时段-字符串表示
     */
    public static String periodNameOfCurrent() {
        return PERIOD_OF_DAY[periodOfTimeInCal(Calendar.getInstance())];
    }

    /**
     * 返回字符串格式的 年月日, 格式示例: 2014 年 10 月 18 日
     */
    public static String formatYMDOfCurr() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        StringBuilder fmtYMD = new StringBuilder();
        fmtYMD.append(year).append(" 年 ").append(month).append(" 月 ")
                .append(dayOfMonth).append(" 日");
        return fmtYMD.toString();
    }

    /**
     * 返回当前时间的 FMT_YMDHMS2 格式的字符串表示
     */
    public static String getFmtYmdhms2OfCurr() {
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat sDateFormat = new SimpleDateFormat(FMT_YMDHMS2);
        return sDateFormat.format(now);
    }

    /**
     * 返回字符串格式的 时分, 格式示例: 下午 4 点 30 分
     */
    public static String formatHMOfCurr() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        StringBuilder fmtYMD = new StringBuilder();
        fmtYMD.append(PERIOD_OF_DAY[periodOfCurrent()]).append(" ")
                .append(hour).append(" 点 ").append(minute).append(" 分");
        return fmtYMD.toString();
    }

    public static final int WEEK = 7;
    public static final int MONTH = 30;
    public static final int YEAR = 365;
    public static final int ALL = 800;

    public static final int PERIOD_WEEK = 1;
    public static final int PERIOD_MONTH = 2;
    public static final int PERIOD_YEAR = 3;
    public static final int PERIOD_ALL = 4;

    /**
     * 返回字符串格式 yyyy-MM-dd HH:mm:ss 的日期时间
     */
    public static String getTimeStr(int befor) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, befor);
        Date d = cal.getTime();
        SimpleDateFormat sDateFormat = new SimpleDateFormat(FMT_YMDHMS);
        String date = sDateFormat.format(d);
        return date;
    }
    
    /**
     * 返回字符串格式为 yyyy-MM-dd 的日期时间
     */
    public static String getTimeStrYMD(int befor) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, befor);
        Date d = cal.getTime();
        SimpleDateFormat sDateFormat = new SimpleDateFormat(FMT_YMD);
        String date = sDateFormat.format(d);
        return date;
    }
    
    /**
     * 返回指定时间的字符串格式（ yyyy-MM-dd）
     */
    public static String getTimeStrYMD(Date d) {
       SimpleDateFormat sDateFormat = new SimpleDateFormat(FMT_YMD);
        String date = sDateFormat.format(d);
        return date;
    }
    
    /**
     * 转换指定 YYYY-MM-DD 对应的 日期
     */
    public static Date convertFromStrYMD(String ymd) {
        if (ymd == null) {
            return null;
        }
        
        SimpleDateFormat formatter = new SimpleDateFormat(FMT_YMD);
        try {
            return formatter.parse(ymd);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 转换指定 YMDHMS 对应的日期时间
     */
    public static Date convertFromStrYMDHMS(String ymdhms) {
        if (ymdhms == null) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(FMT_YMDHMS);
        try {
            return formatter.parse(ymdhms);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 返回指定时间的字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getTimeStr(Date d) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(FMT_YMDHMS);
        String date = sDateFormat.format(d);
        return date;
    }

    /**
     * 返回整型格式的 日期时间
     * 
     * before = 0 表示获取当前时间
     */
    public static long getTimeLong(int before) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, before);
        return cal.getTime().getTime();
    }

    /**
     * 以整型返回当前时间
     */
    public static long nowLong() {
        return getTimeLong(0);
    }

    /**
     * 以整型返回距离当前一周前的时间
     */
    public static long beforeWeekLong() {
        return getTimeLong(-WEEK);
    }

    /**
     * 以整型返回距离当前一个月前的时间
     */
    public static long beforeMonthLong() {
        return getTimeLong(-MONTH);
    }

    /**
     * 以整型返回距离当前一年前的时间
     */
    public static long beforeYearLong() {
        return getTimeLong(-YEAR);
    }

    /**
     * 以整型返回距离当前所有数据
     */
    public static long beforeAllLong() {
        return getTimeLong(-ALL);
    }

    /**
     * 以整型返回指定周期前的时间
     */
    public static long beforeLong(int periodType) {
        switch (periodType) {
        case PERIOD_WEEK:
            return beforeWeekLong();
        case PERIOD_MONTH:
            return beforeMonthLong();
        case PERIOD_YEAR:
            return beforeYearLong();
        case PERIOD_ALL:
            return beforeAllLong();
        }
        return periodType;
    }
    
    /**
     * 生日转年龄
     */
    public static int birthdayToAge(String birth) {
        
        if (birth == null) {
            return -1;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_YMD);
        try {
            return birthdayToAge(sdf.parse(birth));
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 生日转年龄
     */
    public static int birthdayToAge(Date birth) {
        
        if (birth == null) {
            return -1;
        }
        
        Calendar calBirth = Calendar.getInstance();
        calBirth.setTime(birth);
        int birthYear = calBirth.get(Calendar.YEAR);
        
        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        
        return nowYear - birthYear;
    }
    
    /**
     * 年龄转生日，格式为 yyyy-01-01
     */
    public static String ageToBirthday(int age) {
        if (age <= 0) {
            return null;
        }
        
        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, -age);
        now.set(Calendar.MONTH, Calendar.JANUARY);
        now.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat(FMT_YMD).format(now.getTime());
    }
}
