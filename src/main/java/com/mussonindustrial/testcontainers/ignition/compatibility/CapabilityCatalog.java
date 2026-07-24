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
 * Defines capability support and version-specific implementations for an Ignition profile.
 */
public final class CapabilityCatalog {

    /** Profile that owns this catalog. */
    private final String profileName;

    /** Capability definitions owned by the profile. */
    private final Map<IgnitionCapability, Entry> entries;

    /** Creates an immutable capability catalog. */
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

    /** Defines support for one capability. */
    public sealed interface Entry permits SupportedEntry, UnsupportedEntry {

        /**
         * Resolves the definition for an Ignition version.
         *
         * @param version selected Ignition version
         * @return capability resolution
         */
        Resolution resolve(IgnitionVersion version);
    }

    /** Represents one profile-specific capability implementation. */
    public sealed interface Implementation permits DirectImplementation, DelegatedImplementation {}

    /**
     * Implements a capability with a profile-specific applier.
     *
     * @param applier capability applier
     */
    public record DirectImplementation(CapabilityApplier applier) implements Implementation {

        /**
         * Validates the direct implementation.
         *
         * @param applier capability applier
         */
        public DirectImplementation {
            Objects.requireNonNull(applier, "applier");
        }
    }

    /**
     * Implements a capability through another logical capability.
     *
     * @param capability capability providing the implementation
     */
    public record DelegatedImplementation(IgnitionCapability capability) implements Implementation {

        /**
         * Validates the delegated implementation.
         *
         * @param capability capability providing the implementation
         */
        public DelegatedImplementation {
            Objects.requireNonNull(capability, "capability");
        }
    }

    /**
     * Defines a supported capability and its versioned implementations.
     *
     * @param implementations implementations keyed by their first supported version
     */
    public record SupportedEntry(NavigableMap<IgnitionVersion, Implementation> implementations) implements Entry {

        /**
         * Validates and copies the supported entry.
         *
         * @param implementations implementations keyed by their first supported version
         */
        public SupportedEntry {
            Objects.requireNonNull(implementations, "implementations");

            if (implementations.isEmpty()) {
                throw new IllegalArgumentException("A supported capability must have at least one implementation");
            }

            TreeMap<IgnitionVersion, Implementation> copy = new TreeMap<>();

            implementations.forEach((version, implementation) -> copy.put(
                    Objects.requireNonNull(version, "implementation version"),
                    Objects.requireNonNull(implementation, "implementation")));

            implementations = Collections.unmodifiableNavigableMap(copy);
        }

        /**
         * Returns the first version supporting the capability.
         *
         * @return introduction version
         */
        public IgnitionVersion introducedIn() {
            return implementations.firstKey();
        }

        /**
         * Resolves the applicable implementation.
         *
         * <p>Versions before the capability introduction use the earliest known implementation.
         *
         * @param version selected Ignition version
         * @return supported capability resolution
         */
        @Override
        public SupportedResolution resolve(IgnitionVersion version) {
            Objects.requireNonNull(version, "version");

            Map.Entry<IgnitionVersion, Implementation> selected = implementations.floorEntry(version);

            if (selected == null) {
                selected = implementations.firstEntry();
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
         *
         * @param version selected Ignition version
         * @return unsupported capability resolution
         */
        @Override
        public UnsupportedResolution resolve(IgnitionVersion version) {
            Objects.requireNonNull(version, "version");

            return new UnsupportedResolution(reason);
        }
    }

    /** Represents a resolved capability. */
    public sealed interface Resolution permits SupportedResolution, UnsupportedResolution {}

    /**
     * Represents a resolved supported capability.
     *
     * @param introducedIn first supported version
     * @param implementationSince version associated with the selected implementation
     * @param implementation selected capability implementation
     * @param available whether the capability is available in the requested version
     */
    public record SupportedResolution(
            IgnitionVersion introducedIn,
            IgnitionVersion implementationSince,
            Implementation implementation,
            boolean available)
            implements Resolution {

        /**
         * Validates the supported resolution.
         *
         * @param introducedIn first supported version
         * @param implementationSince version associated with the selected implementation
         * @param implementation selected capability implementation
         * @param available whether the capability is available in the requested version
         */
        public SupportedResolution {
            Objects.requireNonNull(introducedIn, "introducedIn");
            Objects.requireNonNull(implementationSince, "implementationSince");
            Objects.requireNonNull(implementation, "implementation");
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

    /** Builds a capability catalog. */
    public static final class Builder {

        /** Profile that owns the catalog. */
        private final String profileName;

        /** Capability definitions collected by the builder. */
        private final EnumMap<IgnitionCapability, Entry> entries = new EnumMap<>(IgnitionCapability.class);

        /** Creates an empty builder. */
        private Builder(String profileName) {
            this.profileName = requireNonBlank(profileName, "profileName");
        }

        /**
         * Adds a directly implemented capability.
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
         * Adds a directly implemented capability.
         *
         * @param capability supported capability
         * @param since first supported version
         * @param applier capability applier
         * @return this builder
         */
        public Builder supported(IgnitionCapability capability, IgnitionVersion since, CapabilityApplier applier) {
            return implemented(capability, since, new DirectImplementation(applier));
        }

        /**
         * Adds a capability implemented through another capability.
         *
         * @param capability supported capability
         * @param since first supported version
         * @param implementationCapability capability providing the implementation
         * @return this builder
         */
        public Builder implementedBy(
                IgnitionCapability capability, String since, IgnitionCapability implementationCapability) {
            return implementedBy(capability, IgnitionVersion.parse(since), implementationCapability);
        }

        /**
         * Adds a capability implemented through another capability.
         *
         * @param capability supported capability
         * @param since first supported version
         * @param implementationCapability capability providing the implementation
         * @return this builder
         */
        public Builder implementedBy(
                IgnitionCapability capability, IgnitionVersion since, IgnitionCapability implementationCapability) {
            requireDistinctCapabilities(capability, implementationCapability);

            return implemented(capability, since, new DelegatedImplementation(implementationCapability));
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

            validateDelegations();

            return new CapabilityCatalog(profileName, entries);
        }

        /** Adds one supported capability implementation. */
        private Builder implemented(
                IgnitionCapability capability, IgnitionVersion since, Implementation implementation) {
            Objects.requireNonNull(since, "since");
            Objects.requireNonNull(implementation, "implementation");

            TreeMap<IgnitionVersion, Implementation> implementations = new TreeMap<>();
            implementations.put(since, implementation);

            add(capability, new SupportedEntry(implementations));

            return this;
        }

        /** Adds one capability definition. */
        private void add(IgnitionCapability capability, Entry entry) {
            Objects.requireNonNull(capability, "capability");
            Objects.requireNonNull(entry, "entry");
            requireUndeclared(capability);

            entries.put(capability, entry);
        }

        /** Requires a capability to be undeclared. */
        private void requireUndeclared(IgnitionCapability capability) {
            if (entries.containsKey(capability)) {
                throw new IllegalStateException(
                        "Profile '%s' declares capability %s more than once".formatted(profileName, capability));
            }
        }

        /** Validates delegated capability implementations. */
        private void validateDelegations() {
            entries.forEach((capability, entry) -> {
                if (!(entry instanceof SupportedEntry supported)) {
                    return;
                }

                for (Implementation implementation : supported.implementations().values()) {
                    if (!(implementation instanceof DelegatedImplementation delegated)) {
                        continue;
                    }

                    requireDistinctCapabilities(capability, delegated.capability());

                    Entry delegatedEntry = entries.get(delegated.capability());

                    if (!(delegatedEntry instanceof SupportedEntry)) {
                        throw new IllegalStateException(
                                "Profile '%s' implements capability %s through unsupported capability %s"
                                        .formatted(profileName, capability, delegated.capability()));
                    }
                }
            });
        }
    }

    /** Builds a versioned capability definition. */
    public static final class VersionedBuilder {

        /** Parent catalog builder. */
        private final Builder parent;

        /** Capability being defined. */
        private final IgnitionCapability capability;

        /** Versioned capability implementations. */
        private final NavigableMap<IgnitionVersion, Implementation> implementations = new TreeMap<>();

        /** Whether this declaration has been completed. */
        private boolean completed;

        /** Creates a versioned capability builder. */
        private VersionedBuilder(Builder parent, IgnitionCapability capability) {
            this.parent = parent;
            this.capability = capability;
        }

        /**
         * Adds a direct implementation starting at a version.
         *
         * @param since first version using the implementation
         * @param applier capability applier
         * @return this builder
         */
        public VersionedBuilder since(String since, CapabilityApplier applier) {
            return since(IgnitionVersion.parse(since), applier);
        }

        /**
         * Adds a direct implementation starting at a version.
         *
         * @param since first version using the implementation
         * @param applier capability applier
         * @return this builder
         */
        public VersionedBuilder since(IgnitionVersion since, CapabilityApplier applier) {
            return add(since, new DirectImplementation(applier));
        }

        /**
         * Adds a delegated implementation starting at a version.
         *
         * @param since first version using the implementation
         * @param implementationCapability capability providing the implementation
         * @return this builder
         */
        public VersionedBuilder implementedBy(String since, IgnitionCapability implementationCapability) {
            return implementedBy(IgnitionVersion.parse(since), implementationCapability);
        }

        /**
         * Adds a delegated implementation starting at a version.
         *
         * @param since first version using the implementation
         * @param implementationCapability capability providing the implementation
         * @return this builder
         */
        public VersionedBuilder implementedBy(IgnitionVersion since, IgnitionCapability implementationCapability) {
            requireDistinctCapabilities(capability, implementationCapability);

            return add(since, new DelegatedImplementation(implementationCapability));
        }

        /**
         * Completes the versioned capability declaration.
         *
         * @return parent catalog builder
         */
        public Builder complete() {
            requireOpen();

            if (implementations.isEmpty()) {
                throw new IllegalStateException("Profile '%s' declares capability %s without an implementation"
                        .formatted(parent.profileName, capability));
            }

            parent.add(capability, new SupportedEntry(implementations));
            completed = true;

            return parent;
        }

        /** Adds one versioned capability implementation. */
        private VersionedBuilder add(IgnitionVersion since, Implementation implementation) {
            requireOpen();
            Objects.requireNonNull(since, "since");
            Objects.requireNonNull(implementation, "implementation");

            Implementation previous = implementations.putIfAbsent(since, implementation);

            if (previous != null) {
                throw new IllegalStateException(
                        "Profile '%s' declares multiple implementations for capability %s at version %s"
                                .formatted(parent.profileName, capability, since));
            }

            return this;
        }

        /** Requires this declaration to remain open. */
        private void requireOpen() {
            if (completed) {
                throw new IllegalStateException("Capability %s has already been completed".formatted(capability));
            }
        }
    }

    /** Requires a delegated implementation to target a different capability. */
    private static void requireDistinctCapabilities(
            IgnitionCapability capability, IgnitionCapability implementationCapability) {
        Objects.requireNonNull(capability, "capability");
        Objects.requireNonNull(implementationCapability, "implementationCapability");

        if (capability == implementationCapability) {
            throw new IllegalArgumentException("Capability %s cannot be implemented by itself".formatted(capability));
        }
    }

    /** Validates and trims a required string. */
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
