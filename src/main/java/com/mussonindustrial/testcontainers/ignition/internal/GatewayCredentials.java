package com.mussonindustrial.testcontainers.ignition.internal;

import java.util.Objects;

/**
 * Stores initial Gateway administrator credentials.
 *
 * @param username administrator username
 * @param password administrator password
 */
public record GatewayCredentials(String username, String password) {

    /**
     * Validates the credentials.
     *
     * @param username administrator username
     * @param password administrator password
     */
    public GatewayCredentials {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
    }
}
