package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionCapability;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Defines capability support and version-specific appliers for an Ignition profile.
 */
public final class CapabilityCatalog {

    /** Profile that owns this catalog. */
    private final String profileName;

    /** Capability definitions owned by the profile. */
    private final Map<IgnitionCapability, Entry> entries;

    /**
     * Creates an immutable capability catalog.
     */
    private CapabilityCatalog(String profileName, Map<IgnitionCapability, Entry> entries) {
        this.profileName = profileName;

        EnumMap<IgnitionCapability, Entry> copy = new EnumMap<>(IgnitionCapability.class);

        copy.putAll(entries);

        this.entries = Collections.unmodifiableMap(copy);
    }

    /**
     * Creates a capability catalog builder.
     *
     * @param profileName profile name
     * @return new builder
     */
    public static Builder profile(String profileName) {
        return new Builder(profileName);
    }

    /**
     * Returns the profile name.
     *
     * @return profile name
     */
    public String profileName() {
        return profileName;
    }

    /**
     * Returns the declared capabilities.
     *
     * @return immutable capability set
     */
    public Set<IgnitionCapability> capabilities() {
        return Collections.unmodifiableSet(EnumSet.copyOf(entries.keySet()));
    }

    /**
     * Returns a capability definition.
     *
     * @param capability capability to inspect
     * @return capability definition
     */
    public Entry entry(IgnitionCapability capability) {
        Objects.requireNonNull(capability, "capability");

        Entry entry = entries.get(capability);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Capability %s is not declared by profile '%s'".formatted(capability, profileName));
        }

        return entry;
    }

    /**
     * Returns whether a capability is supported.
     *
     * @param capability capability to inspect
     * @return {@code true} when supported
     */
    public boolean supports(IgnitionCapability capability) {
        return entry(capability) instanceof SupportedEntry;
    }

    /**
     * Resolves a capability for an Ignition version.
     *
     * @param capability requested capability
     * @param version selected Ignition version
     * @return capability resolution
     */
    public Resolution resolve(IgnitionCapability capability, IgnitionVersion version) {
        Objects.requireNonNull(version, "version");

        return entry(capability).resolve(version);
    }

    /**
     * Defines support for one capability.
     */
    public sealed interface Entry permits SupportedEntry, UnsupportedEntry {

        /**
         * Resolves the definition for an Ignition version.
         *
         * @param version selected Ignition version
         * @return capability resolution
         */
        Resolution resolve(IgnitionVersion version);
    }

    /**
     * Defines a supported capability and its versioned appliers.
     *
     * @param appliers appliers keyed by their first supported version
     */
    public record SupportedEntry(NavigableMap<IgnitionVersion, CapabilityApplier> appliers) implements Entry {

        /**
         * Validates and copies the supported entry.
         *
         * @param appliers appliers keyed by their first supported version
         */
        public SupportedEntry {
            Objects.requireNonNull(appliers, "appliers");

            if (appliers.isEmpty()) {
                throw new IllegalArgumentException("A supported capability must have at least one applier");
            }

            TreeMap<IgnitionVersion, CapabilityApplier> copy = new TreeMap<>();

            appliers.forEach((version, applier) -> copy.put(
                    Objects.requireNonNull(version, "applier version"), Objects.requireNonNull(applier, "applier")));

            appliers = Collections.unmodifiableNavigableMap(copy);
        }

        /**
         * Returns the first version supporting the capability.
         *
         * @return introduction version
         */
        public IgnitionVersion introducedIn() {
            return appliers.firstKey();
        }

        /**
         * Resolves the applicable applier.
         *
         * <p>Versions before an introduction use the earliest known applier.
         */
        @Override
        public SupportedResolution resolve(IgnitionVersion version) {
            Objects.requireNonNull(version, "version");

            Map.Entry<IgnitionVersion, CapabilityApplier> selected = appliers.floorEntry(version);

            if (selected == null) {
                selected = appliers.firstEntry();
            }

            IgnitionVersion introducedIn = introducedIn();

            return new SupportedResolution(
                    introducedIn, selected.getKey(), selected.getValue(), version.isAtLeast(introducedIn));
        }
    }

    /**
     * Defines an unsupported capability.
     *
     * @param reason unsupported reason
     */
    public record UnsupportedEntry(String reason) implements Entry {

        /**
         * Validates the unsupported entry.
         *
         * @param reason unsupported reason
         */
        public UnsupportedEntry {
            reason = requireNonBlank(reason, "reason");
        }

        /**
         * Returns an unsupported resolution.
         */
        @Override
        public UnsupportedResolution resolve(IgnitionVersion version) {
            Objects.requireNonNull(version, "version");

            return new UnsupportedResolution(reason);
        }
    }

    /**
     * Represents a resolved capability.
     */
    public sealed interface Resolution permits SupportedResolution, UnsupportedResolution {}

    /**
     * Represents a resolved supported capability.
     *
     * @param introducedIn first supported version
     * @param implementationSince version associated with the selected applier
     * @param applier selected capability applier
     * @param available whether the capability is available in the requested version
     */
    public record SupportedResolution(
            IgnitionVersion introducedIn,
            IgnitionVersion implementationSince,
            CapabilityApplier applier,
            boolean available)
            implements Resolution {

        /**
         * Validates the supported resolution.
         *
         * @param introducedIn first supported version
         * @param implementationSince version associated with the selected applier
         * @param applier selected capability applier
         * @param available whether the capability is available in the requested version
         */
        public SupportedResolution {
            Objects.requireNonNull(introducedIn, "introducedIn");

            Objects.requireNonNull(implementationSince, "implementationSince");

            Objects.requireNonNull(applier, "applier");
        }
    }

    /**
     * Represents an unsupported capability.
     *
     * @param reason unsupported reason
     */
    public record UnsupportedResolution(String reason) implements Resolution {

        /**
         * Validates the unsupported resolution.
         *
         * @param reason unsupported reason
         */
        public UnsupportedResolution {
            reason = requireNonBlank(reason, "reason");
        }
    }

    /**
     * Builds a capability catalog.
     */
    public static final class Builder {

        /** Profile that owns the catalog. */
        private final String profileName;

        /** Capability definitions collected by the builder. */
        private final EnumMap<IgnitionCapability, Entry> entries = new EnumMap<>(IgnitionCapability.class);

        /**
         * Creates an empty builder.
         */
        private Builder(String profileName) {
            this.profileName = requireNonBlank(profileName, "profileName");
        }

        /**
         * Adds a supported capability.
         *
         * @param capability supported capability
         * @param since first supported version
         * @param applier capability applier
         * @return this builder
         */
        public Builder supported(IgnitionCapability capability, String since, CapabilityApplier applier) {
            return supported(capability, IgnitionVersion.parse(since), applier);
        }

        /**
         * Adds a supported capability.
         *
         * @param capability supported capability
         * @param since first supported version
         * @param applier capability applier
         * @return this builder
         */
        public Builder supported(IgnitionCapability capability, IgnitionVersion since, CapabilityApplier applier) {
            Objects.requireNonNull(since, "since");
            Objects.requireNonNull(applier, "applier");

            TreeMap<IgnitionVersion, CapabilityApplier> appliers = new TreeMap<>();

            appliers.put(since, applier);

            add(capability, new SupportedEntry(appliers));

            return this;
        }

        /**
         * Begins a versioned capability declaration.
         *
         * @param capability supported capability
         * @return versioned capability builder
         */
        public VersionedBuilder versioned(IgnitionCapability capability) {
            Objects.requireNonNull(capability, "capability");

            requireUndeclared(capability);

            return new VersionedBuilder(this, capability);
        }

        /**
         * Adds an unsupported capability.
         *
         * @param capability unsupported capability
         * @param reason unsupported reason
         * @return this builder
         */
        public Builder unsupported(IgnitionCapability capability, String reason) {
            add(capability, new UnsupportedEntry(reason));

            return this;
        }

        /**
         * Creates the completed catalog.
         *
         * @return capability catalog
         */
        public CapabilityCatalog complete() {
            EnumSet<IgnitionCapability> missing = EnumSet.allOf(IgnitionCapability.class);

            missing.removeAll(entries.keySet());

            if (!missing.isEmpty()) {
                throw new IllegalStateException(
                        "Profile '%s' does not declare capabilities: %s".formatted(profileName, missing));
            }

            return new CapabilityCatalog(profileName, entries);
        }

        /**
         * Adds one capability definition.
         */
        private void add(IgnitionCapability capability, Entry entry) {
            Objects.requireNonNull(capability, "capability");

            Objects.requireNonNull(entry, "entry");

            requireUndeclared(capability);

            entries.put(capability, entry);
        }

        /**
         * Requires a capability to be undeclared.
         */
        private void requireUndeclared(IgnitionCapability capability) {
            if (entries.containsKey(capability)) {
                throw new IllegalStateException(
                        "Profile '%s' declares capability %s more than once".formatted(profileName, capability));
            }
        }
    }

    /**
     * Builds a versioned capability definition.
     */
    public static final class VersionedBuilder {

        /** Parent catalog builder. */
        private final Builder parent;

        /** Capability being defined. */
        private final IgnitionCapability capability;

        /** Versioned capability appliers. */
        private final NavigableMap<IgnitionVersion, CapabilityApplier> appliers = new TreeMap<>();

        /** Whether this declaration has been completed. */
        private boolean completed;

        /**
         * Creates a versioned capability builder.
         */
        private VersionedBuilder(Builder parent, IgnitionCapability capability) {
            this.parent = parent;
            this.capability = capability;
        }

        /**
         * Adds an applier starting at a version.
         *
         * @param since first version using the applier
         * @param applier capability applier
         * @return this builder
         */
        public VersionedBuilder since(String since, CapabilityApplier applier) {
            return since(IgnitionVersion.parse(since), applier);
        }

        /**
         * Adds an applier starting at a version.
         *
         * @param since first version using the applier
         * @param applier capability applier
         * @return this builder
         */
        public VersionedBuilder since(IgnitionVersion since, CapabilityApplier applier) {
            requireOpen();

            Objects.requireNonNull(since, "since");
            Objects.requireNonNull(applier, "applier");

            CapabilityApplier previous = appliers.putIfAbsent(since, applier);

            if (previous != null) {
                throw new IllegalStateException(
                        "Profile '%s' declares multiple appliers for capability %s at version %s"
                                .formatted(parent.profileName, capability, since));
            }

            return this;
        }

        /**
         * Completes the versioned capability declaration.
         *
         * @return parent catalog builder
         */
        public Builder complete() {
            requireOpen();

            if (appliers.isEmpty()) {
                throw new IllegalStateException("Profile '%s' declares capability %s without an applier"
                        .formatted(parent.profileName, capability));
            }

            parent.add(capability, new SupportedEntry(appliers));

            completed = true;

            return parent;
        }

        /**
         * Requires this declaration to remain open.
         */
        private void requireOpen() {
            if (completed) {
                throw new IllegalStateException("Capability %s has already been completed".formatted(capability));
            }
        }
    }

    /**
     * Validates and trims a required string.
     */
    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }

        if (trimmed.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(name + " cannot contain a null character");
        }

        return trimmed;
    }
}
