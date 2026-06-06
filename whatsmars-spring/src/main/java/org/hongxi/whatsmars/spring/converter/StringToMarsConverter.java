package org.hongxi.whatsmars.spring.converter;

import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.core.convert.converter.Converter;

/**
 * Demonstrates a custom {@link Converter}: converts a String like
 * {@code "火星:500"} into a {@link Mars} object.
 *
 * <p>Format: {@code cnName:age}</p>
 */
public class StringToMarsConverter implements Converter<String, Mars> {

    @Override
    public Mars convert(String source) {
        String[] parts = source.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid format: '" + source + "', expected 'cnName:age'");
        }
        String cnName = parts[0].trim();
        long age = Long.parseLong(parts[1].trim());
        System.out.printf("[StringToMarsConverter] '%s' -> Mars(cnName=%s, age=%d)%n",
                source, cnName, age);
        return new Mars(age, cnName);
    }
}
