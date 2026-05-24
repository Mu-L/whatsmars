package org.hongxi.whatsmars.common.util;

import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 日期时间工具类，基于 Java 8+ java.time 包
 * 线程安全
 */
public class DateUtils {

    /**
     * 日期格式 yyyy-MM-dd
     */
    public final static String DATE_FORMAT = "yyyy-MM-dd";
    public final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    
    /**
     * 日期格式 yyyyMMdd
     */
    public final static String DATE_FORMAT2 = "yyyyMMdd";
    public final static DateTimeFormatter DATE_FORMATTER2 = DateTimeFormatter.ofPattern(DATE_FORMAT2);
    
    /**
     * 日期格式 yyyy/MM/dd
     */
    public final static String DATE_FORMAT3 = "yyyy/MM/dd";
    public final static DateTimeFormatter DATE_FORMATTER3 = DateTimeFormatter.ofPattern(DATE_FORMAT3);
    
    /**
     * 时间格式 HH:mm:ss
     */
    public final static String TIME_FORMAT = "HH:mm:ss";
    public final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);
    
    /**
     * 日期时间格式 yyyy-MM-dd HH:mm:ss
     */
    public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    /**
     * yyyy-MM
     */
    public final static String DATE_SHORT_FORMAT = "yyyy-MM";
    public final static DateTimeFormatter DATE_SHORT_FORMATTER = DateTimeFormatter.ofPattern(DATE_SHORT_FORMAT);
    
    /**
     * yyyyMM
     */
    public final static String DATE_SHORT_FORMAT2 = "yyyyMM";
    public final static DateTimeFormatter DATE_SHORT_FORMATTER2 = DateTimeFormatter.ofPattern(DATE_SHORT_FORMAT2);

    public static String DATETIME_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";//yyyy-MM-dd HH:mm:ss
    public static String DATE_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2}";//yyyy-MM-dd
    public static String SHORT_DATE_FORMAT_REGEX = "\\d{4}-\\d{2}";//yyyy-MM
    public static String DATE_FORMAT2_REGEX = "\\d{4}\\d{2}\\d{2}";//yyyyMMdd
    public static String SHORT_DATE_FORMAT2_REGEX = "\\d{4}\\d{2}";//yyyyMM
    
    /**
     * 获取时间日期格式化 DateTimeFormatter
     *
     * @param pattern 格式
     * @return DateTimeFormatter 实例
     */
    public static DateTimeFormatter getFormatter(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return null;
        }
        return DateTimeFormatter.ofPattern(pattern);
    }

    /**
     * 将 String 类型日期解析为 LocalDate
     *
     * @param dateString String 类型日期
     * @param pattern    格式
     * @return LocalDate 或 null
     */
    public static LocalDate parseLocalDate(String dateString, String pattern) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, getFormatter(pattern));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 将 String 类型日期解析为 LocalDateTime
     *
     * @param dateString String 类型日期
     * @param pattern    格式
     * @return LocalDateTime 或 null
     */
    public static LocalDateTime parseLocalDateTime(String dateString, String pattern) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, getFormatter(pattern));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 格式化 LocalDate 为 String
     *
     * @param date    LocalDate 实例
     * @param pattern 格式
     * @return 格式化后的字符串或 null
     */
    public static String format(LocalDate date, String pattern) {
        if (null == date) {
            return null;
        }
        return getFormatter(pattern).format(date);
    }
    
    /**
     * 格式化 LocalDateTime 为 String
     *
     * @param dateTime LocalDateTime 实例
     * @param pattern  格式
     * @return 格式化后的字符串或 null
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (null == dateTime) {
            return null;
        }
        return getFormatter(pattern).format(dateTime);
    }

    /**
     * 将 String 类型日期重新格式化为 String 类型
     *
     * @param dateString    原始日期字符串
     * @param srcPattern    原始格式
     * @param targetPattern 目标格式
     * @return 重新格式化后的字符串
     */
    public static String reformat(String dateString, String srcPattern, String targetPattern) {
        LocalDateTime date = parseLocalDateTime(dateString, srcPattern);
        if (date == null) {
            // 尝试解析为 LocalDate
            LocalDate localDate = parseLocalDate(dateString, srcPattern);
            if (localDate != null) {
                return format(localDate, targetPattern);
            }
            return null;
        }
        return format(date, targetPattern);
    }

    /**
     * 获取当前时间的前一天（00:00:00）
     *
     * @return 昨天的 LocalDateTime
     */
    public static LocalDateTime getYesterday() {
        return LocalDate.now().minusDays(1).atStartOfDay();
    }

    /**
     * 获取本月的第一天（00:00:00）
     *
     * @return 本月第一天的 LocalDateTime
     */
    public static LocalDateTime getFirstDayByMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    /**
     * 获取上一个月的第一天（00:00:00）
     *
     * @return 上一个月第一天的 LocalDateTime
     */
    public static LocalDateTime getFirstDayByLastMonth() {
        return LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
    }

    /**
     * 根据月份获取当年该月份的第一天（00:00:00）
     *
     * @param month 月份，1-12
     * @return 指定月份第一天的 LocalDateTime
     */
    public static LocalDateTime getFirstDayByMonth(int month) {
        return LocalDate.now().withMonth(month).withDayOfMonth(1).atStartOfDay();
    }

    /**
     * 根据日期和月份获取该月份的第一天（00:00:00）
     *
     * @param date  日期
     * @param month 月份，1-12
     * @return 指定月份第一天的 LocalDateTime
     */
    public static LocalDateTime getFirstDayByMonth(LocalDate date, int month) {
        if (date == null) {
            return null;
        }
        return date.withMonth(month).withDayOfMonth(1).atStartOfDay();
    }

    /**
     * 计算两个日期之间相隔的月份
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 月份列表，格式为 yyyy-MM
     */
    public static List<String> intervalMonths(LocalDate startDate, LocalDate endDate) {
        List<String> monthList = new ArrayList<>();
        if (startDate == null || endDate == null) {
            return monthList;
        }
        
        LocalDate temp = startDate.withDayOfMonth(startDate.lengthOfMonth() - 1);
        
        while (!temp.isBefore(startDate) && !temp.isAfter(endDate)) {
            monthList.add(DATE_SHORT_FORMATTER.format(temp));
            temp = temp.plusMonths(1).withDayOfMonth(temp.plusMonths(1).lengthOfMonth() - 1);
        }
        
        return monthList;
    }

    /**
     * 传入时间和当前时间比较，最大获取上个月
     *
     * @param date 日期
     * @return LocalDateTime，实际日期格式为 yyyyMM01 00:00:00.0
     */
    public static LocalDateTime maxLastMonth(LocalDate date) {
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        if (thisMonth.compareTo(date) <= 0) {
            return thisMonth.minusMonths(1).atStartOfDay();
        }
        return date.atStartOfDay();
    }

    /**
     * 增加或减少月份
     *
     * @param date  日期
     * @param months 月份数，正数表示增加，负数表示减少
     * @return LocalDateTime
     */
    public static LocalDateTime getMonth(LocalDate date, int months) {
        if (date == null) {
            return null;
        }
        return date.plusMonths(months).atStartOfDay();
    }

    /**
     * 获取某个日期所在月份的第一天（00:00:00）
     *
     * @param date 日期
     * @return LocalDateTime
     */
    public static LocalDateTime getFirstDayByMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1).atStartOfDay();
    }

    /**
     * 计算两个日期之间相隔的天数
     *
     * @param beforeDate 之前的日期
     * @param afterDate  之后的日期
     * @return 天数
     */
    public static long betweenDays(LocalDate beforeDate, LocalDate afterDate) {
        if (null == beforeDate || null == afterDate) {
            throw new NullPointerException("date can't be null");
        }
        if (beforeDate.isAfter(afterDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(beforeDate, afterDate);
    }
    
    // ========== 为了兼容性保留的方法，建议逐步迁移到新 API ==========
    
    /**
     * 将 java.util.Date 转换为 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    /**
     * 将 java.util.Date 转换为 LocalDate
     */
    public static LocalDate toLocalDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
    
    /**
     * 将 LocalDateTime 转换为 java.util.Date
     */
    public static java.util.Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return java.util.Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 将 LocalDate 转换为 java.util.Date
     */
    public static java.util.Date toDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return java.util.Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 兼容旧方法：将 string 类型 date 格式化为 date 类型
     */
    @Deprecated
    public static java.util.Date getDateFormat(String dateString, String pattern) {
        LocalDate localDate = parseLocalDate(dateString, pattern);
        if (localDate != null) {
            return toDate(localDate);
        }
        LocalDateTime localDateTime = parseLocalDateTime(dateString, pattern);
        if (localDateTime != null) {
            return toDate(localDateTime);
        }
        return null;
    }
    
    /**
     * 兼容旧方法：将 date 格式化为 string
     */
    @Deprecated
    public static String getStringFormat(java.util.Date date, String pattern) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date);
        return format(localDateTime, pattern);
    }
    
    /**
     * 兼容旧方法：将 string 类型 date 重新格式化为 string 类型
     */
    @Deprecated
    public static String getStringReformat(String dateString, String srcPattern, String targetPattern) {
        return reformat(dateString, srcPattern, targetPattern);
    }
}
