package com.mussonindustrial.testcontainers.ignition;

/**
 * Identifies a logical Ignition network endpoint.
 *
 * <p>Each Ignition profile defines the corresponding container port.
 */
public enum IgnitionEndpoint {

    /** Gateway HTTP endpoint. */
    GATEWAY_HTTP,

    /** Gateway HTTPS endpoint. */
    GATEWAY_HTTPS,

    /** Gateway Network endpoint. */
    GATEWAY_NETWORK,

    /** OPC UA server endpoint. */
    OPC_UA,

    /** Gateway JVM debugger endpoint. */
    DEBUG
}
