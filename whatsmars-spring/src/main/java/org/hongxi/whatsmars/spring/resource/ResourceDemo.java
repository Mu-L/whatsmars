package org.hongxi.whatsmars.spring.resource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Demonstrates Spring's {@link Resource} abstraction:
 * <ul>
 *   <li>{@link ClassPathResource} — load from classpath</li>
 *   <li>{@link ResourcePatternResolver} — wildcard pattern matching
 *       (e.g. {@code classpath*:*.xml})</li>
 *   <li>Reading resource content as String</li>
 * </ul>
 */
public class ResourceDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("===== ClassPathResource =====");
        ClassPathResource resource = new ClassPathResource("application.yml");
        System.out.println("  exists: " + resource.exists());
        System.out.println("  filename: " + resource.getFilename());
        System.out.println("  content:");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining("\n    ", "    ", ""));
            System.out.println(content);
        }

        System.out.println("\n===== ResourcePatternResolver (wildcard) =====");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] xmlFiles = resolver.getResources("classpath*:*.xml");
        System.out.println("  Found " + xmlFiles.length + " XML file(s):");
        for (Resource r : xmlFiles) {
            System.out.println("    " + r.getFilename() + " (" + r.contentLength() + " bytes)");
        }

        System.out.println("\n===== ResourcePatternResolver (pattern) =====");
        Resource[] propFiles = resolver.getResources("classpath*:application*.properties");
        System.out.println("  Found " + propFiles.length + " properties file(s):");
        for (Resource r : propFiles) {
            System.out.println("    " + r.getFilename());
        }
    }
}
