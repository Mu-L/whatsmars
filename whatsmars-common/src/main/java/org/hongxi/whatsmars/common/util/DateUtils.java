package org.hongxi.whatsmars.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具类，基于 Java 8+ java.time 包
 * 线程安全
 */
public class DateUtils {

    /**
     * 日期格式 yyyy-MM-dd
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    
    /**
     * 日期格式 yyyyMMdd
     */
    public static final String DATE_FORMAT2 = "yyyyMMdd";
    public static final DateTimeFormatter DATE_FORMATTER2 = DateTimeFormatter.ofPattern(DATE_FORMAT2);
    
    /**
     * 日期格式 yyyy/MM/dd
     */
    public static final String DATE_FORMAT3 = "yyyy/MM/dd";
    public static final DateTimeFormatter DATE_FORMATTER3 = DateTimeFormatter.ofPattern(DATE_FORMAT3);
    
    /**
     * 时间格式 HH:mm:ss
     */
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);
    
    /**
     * 日期时间格式 yyyy-MM-dd HH:mm:ss
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    /**
     * yyyy-MM
     */
    public static final String DATE_SHORT_FORMAT = "yyyy-MM";
    public static final DateTimeFormatter DATE_SHORT_FORMATTER = DateTimeFormatter.ofPattern(DATE_SHORT_FORMAT);
    
    /**
     * yyyyMM
     */
    public static final String DATE_SHORT_FORMAT2 = "yyyyMM";
    public static final DateTimeFormatter DATE_SHORT_FORMATTER2 = DateTimeFormatter.ofPattern(DATE_SHORT_FORMAT2);

    public static final String DATETIME_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"; // yyyy-MM-dd HH:mm:ss
    public static final String DATE_FORMAT_REGEX = "\\d{4}-\\d{2}-\\d{2}"; // yyyy-MM-dd
    public static final String SHORT_DATE_FORMAT_REGEX = "\\d{4}-\\d{2}"; // yyyy-MM
    public static final String DATE_FORMAT2_REGEX = "\\d{4}\\d{2}\\d{2}"; // yyyyMMdd
    public static final String SHORT_DATE_FORMAT2_REGEX = "\\d{4}\\d{2}"; // yyyyMM

    /**
     * 将 String 类型日期解析为 LocalDate
     *
     * @param dateString String 类型日期
     * @param pattern    格式
     * @return LocalDate
     */
    public static LocalDate parseLocalDate(String dateString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateString, formatter);
    }
    
    /**
     * 将 String 类型日期解析为 LocalDateTime
     *
     * @param dateString String 类型日期
     * @param pattern    格式
     * @return LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(String dateString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateString, formatter);
    }
    
    /**
     * 格式化 LocalDate 为 String
     *
     * @param date    LocalDate 实例
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }
    
    /**
     * 格式化 LocalDateTime 为 String
     *
     * @param dateTime LocalDateTime 实例
     * @param pattern  格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 获取本月的第一天（00:00:00）
     *
     * @return 本月第一天的 LocalDateTime
     */
    public static LocalDateTime getFirstDayByMonth() {
        return getFirstDayByMonth(LocalDate.now());
    }

    /**
     * 获取某个日期所在月份的第一天（00:00:00）
     *
     * @param date 日期
     * @return LocalDateTime
     */
    public static LocalDateTime getFirstDayByMonth(LocalDate date) {
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
        if (beforeDate == null || afterDate == null) {
            throw new NullPointerException("date can't be null");
        }
        if (beforeDate.isAfter(afterDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(beforeDate, afterDate);
    }
}
