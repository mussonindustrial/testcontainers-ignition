package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

/**
 * Declares the network endpoints available from an Ignition profile.
 */
public final class EndpointCatalog {

    /**
     * Describes a container endpoint.
     *
     * @param containerPort container port number
     * @param description endpoint description
     */
    public record Entry(int containerPort, String description) {

        /**
         * Validates the endpoint definition.
         *
         * @param containerPort container port number
         * @param description endpoint description
         */
        public Entry {
            if (containerPort < 1 || containerPort > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535: " + containerPort);
            }

            description = Objects.requireNonNull(description, "description");
        }
    }

    /** Name of the owning profile. */
    private final String profileName;

    /** Endpoint definitions keyed by logical endpoint. */
    private final Map<IgnitionEndpoint, Entry> entries;

    /**
     * Creates an immutable endpoint catalog.
     */
    private EndpointCatalog(String profileName, Map<IgnitionEndpoint, Entry> entries) {
        this.profileName = profileName;
        this.entries = Collections.unmodifiableMap(new EnumMap<>(entries));
    }

    /**
     * Creates a catalog builder for an Ignition profile.
     *
     * @param profileName profile name
     * @return a new catalog builder
     */
    public static Builder profile(String profileName) {
        return new Builder(profileName);
    }

    /**
     * Returns the definition of an endpoint.
     *
     * @param endpoint endpoint to inspect
     * @return endpoint definition
     */
    public Entry get(IgnitionEndpoint endpoint) {
        Entry entry = entries.get(Objects.requireNonNull(endpoint, "endpoint"));

        if (entry == null) {
            throw new IllegalStateException(
                    "Profile '%s' does not declare endpoint %s".formatted(profileName, endpoint));
        }

        return entry;
    }

    /**
     * Returns the container port for an endpoint.
     *
     * @param endpoint endpoint to inspect
     * @return container port
     */
    public int port(IgnitionEndpoint endpoint) {
        return get(endpoint).containerPort();
    }

    /**
     * Returns all endpoint definitions.
     *
     * @return immutable endpoint entries
     */
    public Map<IgnitionEndpoint, Entry> entries() {
        return entries;
    }

    /**
     * Builds an endpoint catalog.
     */
    public static final class Builder {

        /** Name of the owning profile. */
        private final String profileName;

        /** Endpoint definitions collected by the builder. */
        private final EnumMap<IgnitionEndpoint, Entry> entries = new EnumMap<>(IgnitionEndpoint.class);

        /**
         * Creates a builder for a profile.
         */
        private Builder(String profileName) {
            this.profileName = Objects.requireNonNull(profileName, "profileName");
        }

        /**
         * Declares an endpoint.
         *
         * @param endpoint logical endpoint
         * @param containerPort container port number
         * @param description endpoint description
         * @return this builder
         */
        public Builder endpoint(IgnitionEndpoint endpoint, int containerPort, String description) {
            Objects.requireNonNull(endpoint, "endpoint");

            Entry previous = entries.putIfAbsent(endpoint, new Entry(containerPort, description));

            if (previous != null) {
                throw new IllegalStateException(
                        "Endpoint %s is declared more than once in profile '%s'".formatted(endpoint, profileName));
            }

            return this;
        }

        /**
         * Validates and creates the endpoint catalog.
         *
         * @return completed endpoint catalog
         * @throws IllegalStateException if any endpoint is undeclared
         */
        public EndpointCatalog complete() {
            EnumSet<IgnitionEndpoint> missing = EnumSet.allOf(IgnitionEndpoint.class);

            missing.removeAll(entries.keySet());

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Endpoint catalog '%s' is missing: %s".formatted(profileName, missing));
            }

            return new EndpointCatalog(profileName, entries);
        }
    }
}
