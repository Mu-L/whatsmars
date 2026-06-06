package org.hongxi.whatsmars.spring.converter;

import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Set;

/**
 * Demonstrates Spring's {@link ConversionService} and custom {@link org.springframework.core.convert.converter.Converter}:
 * <ul>
 *   <li>{@link DefaultConversionService} — built-in type conversions</li>
 *   <li>Registering a custom {@link StringToMarsConverter}</li>
 *   <li>{@link ConversionServiceFactoryBean} — for use in Spring context</li>
 * </ul>
 */
public class ConversionDemo {

    public static void main(String[] args) {
        // 1. Create a DefaultConversionService (includes built-in converters)
        DefaultConversionService cs = new DefaultConversionService();

        // 2. Register our custom converter
        cs.addConverter(new StringToMarsConverter());

        System.out.println("===== Built-in conversions =====");
        System.out.println("  String -> Integer: " + cs.convert("42", Integer.class));
        System.out.println("  String -> Boolean: " + cs.convert("true", Boolean.class));
        System.out.println("  Integer -> String: " + cs.convert(100, String.class));
        System.out.println("  canConvert(String, Long): " + cs.canConvert(String.class, Long.class));

        System.out.println("\n===== Custom converter: String -> Mars =====");
        Mars mars = cs.convert("火星:999", Mars.class);
        System.out.println("  result: cnName=" + mars.getCnName() + ", age=" + mars.getAge());

        System.out.println("\n===== canConvert checks =====");
        System.out.println("  canConvert(String, Mars): " + cs.canConvert(String.class, Mars.class));
        System.out.println("  canConvert(Mars, String): " + cs.canConvert(Mars.class, String.class));
    }
}
