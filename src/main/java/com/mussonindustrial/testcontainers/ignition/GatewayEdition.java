package com.mussonindustrial.testcontainers.ignition;

/**
 * Gateway Editions
 */
public enum GatewayEdition {
    /**
     * Standard Edition
    */
    STANDARD("standard"),

    /**
     * Edge Edition
     */
    EDGE("edge"),

    /**
     * Maker Edition
     */
    MAKER("maker");

    private final String value;
    GatewayEdition(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
