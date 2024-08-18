package com.mussonindustrial.testcontainers.ignition;

public class GatewayNetworkConfiguration {
    private final String host;
    private final Integer port;
    private final Integer pingRate;
    private final Integer pingMaxMissed;
    private final Boolean enabled;
    private final Boolean enableSsl;
    private final Integer websocketTimeout;
    private final String description;

    private GatewayNetworkConfiguration(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.pingRate = builder.pingRate;
        this.pingMaxMissed = builder.pingMaxMissed;
        this.enabled = builder.enabled;
        this.enableSsl = builder.enableSsl;
        this.websocketTimeout = builder.websocketTimeout;
        this.description = builder.description;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getPingRate() {
        return pingRate;
    }

    public Integer getPingMaxMissed() {
        return pingMaxMissed;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getEnableSsl() {
        return enableSsl;
    }

    public Integer getWebsocketTimeout() {
        return websocketTimeout;
    }

    public String getDescription() {
        return description;
    }

    // Builder class
    public static class Builder {
        private String host;
        private Integer port;
        private Integer pingRate;
        private Integer pingMaxMissed;
        private Boolean enabled;
        private Boolean enableSsl;
        private Integer websocketTimeout;
        private String description;

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(Integer port) {
            this.port = port;
            return this;
        }

        public Builder setPingRate(Integer pingRate) {
            this.pingRate = pingRate;
            return this;
        }

        public Builder setPingMaxMissed(Integer pingMaxMissed) {
            this.pingMaxMissed = pingMaxMissed;
            return this;
        }

        public Builder setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setEnableSsl(Boolean enableSsl) {
            this.enableSsl = enableSsl;
            return this;
        }

        public Builder setWebsocketTimeout(Integer websocketTimeout) {
            this.websocketTimeout = websocketTimeout;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public GatewayNetworkConfiguration build() {
            return new GatewayNetworkConfiguration(this);
        }
    }
}

