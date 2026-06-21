package org.hongxi.whatsmars.nacos.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

@ConfigurationProperties(prefix = "cloud.agent")
@Component
public class AgentProperties {

    private String name;

    private String version;

    private int credits;

    private boolean enabled;

    private Provider provider = new Provider();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentProperties that = (AgentProperties) o;
        return credits == that.credits && enabled == that.enabled
                && Objects.equals(name, that.name)
                && Objects.equals(version, that.version)
                && Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, credits, enabled, provider);
    }

    @Override
    public String toString() {
        return "AgentProperties(name=" + name + ", version=" + version + ", credits=" + credits
                + ", enabled=" + enabled + ", provider=" + provider + ")";
    }

    public static class Provider {

        private String name;

        private String model;

        private String apiKey;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Provider that = (Provider) o;
            return Objects.equals(name, that.name)
                    && Objects.equals(model, that.model)
                    && Objects.equals(apiKey, that.apiKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, model, apiKey);
        }

        @Override
        public String toString() {
            return "Provider(name=" + name + ", model=" + model + ", apiKey=" + apiKey + ")";
        }
    }
}
