package org.hongxi.whatsmars.boot.sample.beans;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by shenhongxi on 2020/7/17.
 */
@ConfigurationProperties(prefix = "test")
public class TestProperties {

    private Map<String, String> map;
    private Properties properties;

    private String name;

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestProperties that = (TestProperties) o;
        return Objects.equals(map, that.map)
                && Objects.equals(properties, that.properties)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, properties, name);
    }

    @Override
    public String toString() {
        return "TestProperties(map=" + map + ", properties=" + properties + ", name=" + name + ")";
    }
}
