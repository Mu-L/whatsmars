package org.hongxi.whatsmars.boot.sample.web.converter;

import org.hongxi.whatsmars.common.util.DateUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Created by hongxi on 2020/8/16.
 * 只对 GET 或 form 表单请求有效
 * 对于json请求，需要在类的属性上加 @JsonFormat
 */
@Component
public class StringToDateConverter implements Converter<String, LocalDateTime> {

    private final Pattern dateTimePattern = Pattern.compile(DateUtils.DATETIME_FORMAT_REGEX);
    private final Pattern shortDatePattern = Pattern.compile(DateUtils.SHORT_DATE_FORMAT_REGEX);
    private final Pattern shortDate2Pattern = Pattern.compile(DateUtils.SHORT_DATE_FORMAT2_REGEX);
    private final Pattern datePattern = Pattern.compile(DateUtils.DATE_FORMAT_REGEX);
    private final Pattern date2Pattern = Pattern.compile(DateUtils.DATE_FORMAT2_REGEX);

    @Override
    public LocalDateTime convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        if (shortDatePattern.matcher(source).matches()) {
            return LocalDateTime.parse(source, DateUtils.DATE_SHORT_FORMATTER);
        }
        if (datePattern.matcher(source).matches()) {
            return LocalDateTime.parse(source, DateUtils.DATE_FORMATTER);
        }
        if (dateTimePattern.matcher(source).matches()) {
            return LocalDateTime.parse(source, DateUtils.DATETIME_FORMATTER);
        }
        if (shortDate2Pattern.matcher(source).matches()) {
            return LocalDateTime.parse(source, DateUtils.DATE_SHORT_FORMATTER2);
        }
        if (date2Pattern.matcher(source).matches()) {
            return LocalDateTime.parse(source, DateUtils.DATE_FORMATTER2);
        }
        throw new IllegalArgumentException("Invalid date value '" + source + "'");
    }
}