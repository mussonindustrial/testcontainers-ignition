package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionCapability;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Declares capability support for an Ignition profile.
 */
public final class CapabilityMatrix {

    /** Name of the owning profile. */
    private final String profileName;

    /** Support declarations keyed by capability. */
    private final Map<IgnitionCapability, CapabilitySupport> entries;

    /**
     * Creates an immutable capability matrix.
     */
    private CapabilityMatrix(String profileName, Map<IgnitionCapability, CapabilitySupport> entries) {
        this.profileName = profileName;
        this.entries = Map.copyOf(entries);
    }

    /**
     * Returns the support declaration for a capability.
     *
     * @param capability capability to inspect
     * @return support declaration, or {@code null} when absent
     */
    public CapabilitySupport get(IgnitionCapability capability) {
        return entries.get(capability);
    }

    /**
     * Returns all capability support declarations.
     *
     * @return immutable capability entries
     */
    public Set<Map.Entry<IgnitionCapability, CapabilitySupport>> entries() {
        return entries.entrySet();
    }

    /**
     * Creates a matrix builder for an Ignition profile.
     *
     * @param profileName profile name
     * @return a new matrix builder
     */
    public static Builder profile(String profileName) {
        return new Builder(profileName);
    }

    /**
     * Builds a capability matrix.
     */
    public static final class Builder {

        /** Name of the owning profile. */
        private final String profileName;

        /** Capability declarations collected by the builder. */
        private final EnumMap<IgnitionCapability, CapabilitySupport> entries = new EnumMap<>(IgnitionCapability.class);

        /**
         * Creates a builder for a profile.
         */
        private Builder(String profileName) {
            this.profileName = profileName;
        }

        /**
         * Declares a supported capability.
         *
         * @param capability supported capability
         * @param since first supported Ignition version
         * @return this builder
         */
        public Builder supported(IgnitionCapability capability, String since) {
            entries.put(capability, CapabilitySupport.supportedSince(since));

            return this;
        }

        /**
         * Declares an unsupported capability.
         *
         * @param capability unsupported capability
         * @param reason reason the capability is unsupported
         * @return this builder
         */
        public Builder unsupported(IgnitionCapability capability, String reason) {
            entries.put(capability, CapabilitySupport.unsupported(reason));

            return this;
        }

        /**
         * Validates and creates the capability matrix.
         *
         * @return completed capability matrix
         * @throws IllegalStateException if any capability is undeclared
         */
        public CapabilityMatrix complete() {
            EnumSet<IgnitionCapability> missing = EnumSet.allOf(IgnitionCapability.class);

            missing.removeAll(entries.keySet());

            if (!missing.isEmpty()) {
                throw new IllegalStateException(
                        "Capability profile '%s' is missing: %s".formatted(profileName, missing));
            }

            return new CapabilityMatrix(profileName, entries);
        }
    }
}
