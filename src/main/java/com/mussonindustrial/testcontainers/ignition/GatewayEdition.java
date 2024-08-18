package com.mussonindustrial.testcontainers.ignition;

public enum GatewayEdition {
    STANDARD("standard"),
    EDGE("edge"),
    MAKER("maker");

    private final String value;
    private GatewayEdition(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
