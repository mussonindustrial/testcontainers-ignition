package com.mussonindustrial.testcontainers.ignition;

/**
 * Identifies an Ignition Gateway edition.
 */
public enum IgnitionGatewayEdition {

    /** Standard Gateway edition. */
    STANDARD("standard"),

    /** Edge Gateway edition. */
    EDGE("edge"),

    /** Maker Gateway edition. */
    MAKER("maker");

    /** Docker image value for the edition. */
    private final String value;

    /**
     * Creates a Gateway edition.
     *
     * @param value Docker image value
     */
    IgnitionGatewayEdition(String value) {
        this.value = value;
    }

    /**
     * Returns the Docker image value.
     *
     * @return edition value
     */
    @Override
    public String toString() {
        return value;
    }
}
