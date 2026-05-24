package org.hongxi.whatsmars.boot.sample.web.converter;

import org.hongxi.whatsmars.common.util.DateUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by zhaozhong on 2016/9/6.
 */
@Component
public class StringToDateConverter implements Converter<String, Date> {

    private Pattern dateTimePattern = Pattern.compile(DateUtils.DATETIME_FORMAT_REGEX);
    private Pattern shortDatePattern = Pattern.compile(DateUtils.SHORT_DATE_FORMAT_REGEX);
    private Pattern shortDate2Pattern = Pattern.compile(DateUtils.SHORT_DATE_FORMAT2_REGEX);
    private Pattern datePattern = Pattern.compile(DateUtils.DATE_FORMAT_REGEX);
    private Pattern date2Pattern = Pattern.compile(DateUtils.DATE_FORMAT2_REGEX);

    @Override
    public Date convert(String source) {
        if (StringUtils.hasLength(source)) {
            if(shortDatePattern.matcher(source).matches()) {
                return DateUtils.getDateFormat(source, DateUtils.DATE_SHORT_FORMAT);
            }else if(datePattern.matcher(source).matches()) {
                return DateUtils.getDateFormat(source, DateUtils.DATE_FORMAT);
            }else if(dateTimePattern.matcher(source).matches()) {
                return DateUtils.getDateFormat(source, DateUtils.DATETIME_FORMAT);
            }else if(shortDate2Pattern.matcher(source).matches()) {
                return DateUtils.getDateFormat(source, DateUtils.DATE_SHORT_FORMAT2);
            }else if(date2Pattern.matcher(source).matches()) {
                return DateUtils.getDateFormat(source, DateUtils.DATE_FORMAT2);
            }
        }
        return null;
    }
}