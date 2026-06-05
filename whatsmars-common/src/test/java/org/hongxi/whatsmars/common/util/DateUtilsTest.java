package org.hongxi.whatsmars.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateUtils 日期格式转换单元测试
 */
public class DateUtilsTest {

    // ==================== parseLocalDate ====================

    @Test
    public void testParseLocalDate_yyyyMMdd_dash() {
        LocalDate date = DateUtils.parseLocalDate("2026-06-05", DateUtils.DATE_FORMAT);
        assertEquals(2026, date.getYear());
        assertEquals(6, date.getMonthValue());
        assertEquals(5, date.getDayOfMonth());
    }

    @Test
    public void testParseLocalDate_yyyyMMdd_compact() {
        LocalDate date = DateUtils.parseLocalDate("20260605", DateUtils.DATE_FORMAT2);
        assertEquals(LocalDate.of(2026, 6, 5), date);
    }

    @Test
    public void testParseLocalDate_yyyyMMdd_slash() {
        LocalDate date = DateUtils.parseLocalDate("2026/06/05", DateUtils.DATE_FORMAT3);
        assertEquals(LocalDate.of(2026, 6, 5), date);
    }

    // ==================== parseLocalDateTime ====================

    @Test
    public void testParseLocalDateTime() {
        LocalDateTime dateTime = DateUtils.parseLocalDateTime("2026-06-05 14:30:00", DateUtils.DATETIME_FORMAT);
        assertEquals(2026, dateTime.getYear());
        assertEquals(6, dateTime.getMonthValue());
        assertEquals(5, dateTime.getDayOfMonth());
        assertEquals(14, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
        assertEquals(0, dateTime.getSecond());
    }

    @Test
    public void testParseLocalDateTime_withTime() {
        LocalDateTime dateTime = DateUtils.parseLocalDateTime("2026-12-31 23:59:59", DateUtils.DATETIME_FORMAT);
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59, 59), dateTime);
    }

    // ==================== format ====================

    @Test
    public void testFormatLocalDate() {
        LocalDate date = LocalDate.of(2026, 6, 5);

        assertEquals("2026-06-05", DateUtils.format(date, DateUtils.DATE_FORMAT));
        assertEquals("20260605", DateUtils.format(date, DateUtils.DATE_FORMAT2));
        assertEquals("2026/06/05", DateUtils.format(date, DateUtils.DATE_FORMAT3));
        assertEquals("2026-06", DateUtils.format(date, DateUtils.DATE_SHORT_FORMAT));
        assertEquals("202606", DateUtils.format(date, DateUtils.DATE_SHORT_FORMAT2));
    }

    @Test
    public void testFormatLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 5, 14, 30, 45);

        assertEquals("2026-06-05 14:30:45", DateUtils.format(dateTime, DateUtils.DATETIME_FORMAT));
        assertEquals("14:30:45", DateUtils.format(dateTime, DateUtils.TIME_FORMAT));
        assertEquals("2026-06-05", DateUtils.format(dateTime, DateUtils.DATE_FORMAT));
    }

    // ==================== format + parse 往返 ====================

    @Test
    public void testFormatAndParseRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2026, 1, 15, 8, 30, 0);
        String formatted = DateUtils.format(original, DateUtils.DATETIME_FORMAT);
        LocalDateTime parsed = DateUtils.parseLocalDateTime(formatted, DateUtils.DATETIME_FORMAT);
        assertEquals(original, parsed);
    }

    @Test
    public void testFormatAndParseLocalDateRoundTrip() {
        LocalDate original = LocalDate.of(2026, 12, 31);
        String formatted = DateUtils.format(original, DateUtils.DATE_FORMAT);
        LocalDate parsed = DateUtils.parseLocalDate(formatted, DateUtils.DATE_FORMAT);
        assertEquals(original, parsed);
    }

    // ==================== getFirstDayByMonth ====================

    @Test
    public void testGetFirstDayByMonth() {
        LocalDate date = LocalDate.of(2026, 6, 15);
        LocalDateTime firstDay = DateUtils.getFirstDayByMonth(date);

        assertEquals(2026, firstDay.getYear());
        assertEquals(6, firstDay.getMonthValue());
        assertEquals(1, firstDay.getDayOfMonth());
        assertEquals(0, firstDay.getHour());
        assertEquals(0, firstDay.getMinute());
        assertEquals(0, firstDay.getSecond());
    }

    @Test
    public void testGetFirstDayByMonth_january() {
        LocalDate date = LocalDate.of(2026, 1, 31);
        LocalDateTime firstDay = DateUtils.getFirstDayByMonth(date);
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0, 0), firstDay);
    }

    @Test
    public void testGetFirstDayByMonth_december() {
        LocalDate date = LocalDate.of(2026, 12, 25);
        LocalDateTime firstDay = DateUtils.getFirstDayByMonth(date);
        assertEquals(LocalDateTime.of(2026, 12, 1, 0, 0, 0), firstDay);
    }

    // ==================== betweenDays ====================

    @Test
    public void testBetweenDays() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        assertEquals(30, DateUtils.betweenDays(start, end));
    }

    @Test
    public void testBetweenDays_sameDay() {
        LocalDate date = LocalDate.of(2026, 6, 5);
        assertEquals(0, DateUtils.betweenDays(date, date));
    }

    @Test
    public void testBetweenDays_reversedOrder() {
        // before > after 时返回 0
        LocalDate start = LocalDate.of(2026, 12, 31);
        LocalDate end = LocalDate.of(2026, 1, 1);
        assertEquals(0, DateUtils.betweenDays(start, end));
    }

    @Test
    public void testBetweenDays_crossMonth() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        LocalDate end = LocalDate.of(2026, 3, 15);
        assertEquals(59, DateUtils.betweenDays(start, end)); // 1月剩余16天 + 2月28天 + 3月15天 = 59
    }

    @Test
    public void testBetweenDays_crossYear() {
        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        assertEquals(61, DateUtils.betweenDays(start, end)); // 12月30天 + 1月31天 = 61
    }

    @Test
    public void testBetweenDays_nullArgument() {
        assertThrows(NullPointerException.class, () -> DateUtils.betweenDays(null, LocalDate.now()));
        assertThrows(NullPointerException.class, () -> DateUtils.betweenDays(LocalDate.now(), null));
    }

    // ==================== 格式常量验证 ====================

    @Test
    public void testFormatConstants() {
        assertEquals("yyyy-MM-dd", DateUtils.DATE_FORMAT);
        assertEquals("yyyyMMdd", DateUtils.DATE_FORMAT2);
        assertEquals("yyyy/MM/dd", DateUtils.DATE_FORMAT3);
        assertEquals("HH:mm:ss", DateUtils.TIME_FORMAT);
        assertEquals("yyyy-MM-dd HH:mm:ss", DateUtils.DATETIME_FORMAT);
        assertEquals("yyyy-MM", DateUtils.DATE_SHORT_FORMAT);
        assertEquals("yyyyMM", DateUtils.DATE_SHORT_FORMAT2);
    }
}
