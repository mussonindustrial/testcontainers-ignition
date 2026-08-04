package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import java.util.*;

/**
 * Declarative catalog of the built-in modules available in an Ignition
 * Docker image generation.
 *
 * <p>Profiles only need to declare supported modules. Any module omitted
 * from the builder is automatically declared unsupported when the catalog
 * is completed.
 */
public final class ModuleCatalog {

    /**
     * Indicates whether a logical module is supported by a profile.
     */
    public enum Status {

        /** The module can be selected by the profile. */
        SUPPORTED,

        /** The module is not available in the profile. */
        UNSUPPORTED
    }

    /**
     * Default reason assigned to modules omitted from a profile.
     */
    private static final String DEFAULT_UNSUPPORTED_REASON = "The module is not supported by this Ignition profile";

    /**
     * Profile-specific definition of one logical Ignition module.
     *
     * @param status whether the module can be selected
     * @param since first known version supporting the module
     * @param identifier image-specific module identifier
     * @param dependencies other logical modules required by this module
     * @param endpoints network endpoints contributed by this module
     * @param description compatibility notes or the unsupported reason
     */
    public record Entry(
            Status status,
            IgnitionVersion since,
            String identifier,
            Set<IgnitionModule> dependencies,
            Set<IgnitionEndpoint> endpoints,
            String description) {

        /**
         * Validates and defensively copies a module entry.
         *
         * @param status whether the module can be selected
         * @param since first known version supporting the module
         * @param identifier image-specific module identifier
         * @param dependencies other logical modules required by this module
         * @param endpoints network endpoints contributed by this module
         * @param description compatibility notes or the unsupported reason
         */
        public Entry {
            Objects.requireNonNull(status, "status");

            identifier = Objects.requireNonNull(identifier, "identifier");

            dependencies = Set.copyOf(Objects.requireNonNull(dependencies, "dependencies"));

            endpoints = Set.copyOf(Objects.requireNonNull(endpoints, "endpoints"));

            description = requireNonBlank(description, "description");

            if (status == Status.SUPPORTED) {
                Objects.requireNonNull(since, "since");

                if (identifier.isEmpty()) {
                    throw new IllegalArgumentException("A supported module must have an identifier");
                }
            } else {
                if (since != null) {
                    throw new IllegalArgumentException("An unsupported module cannot have a minimum version");
                }

                if (!dependencies.isEmpty()) {
                    throw new IllegalArgumentException("An unsupported module cannot have dependencies");
                }

                if (!endpoints.isEmpty()) {
                    throw new IllegalArgumentException("An unsupported module cannot expose endpoints");
                }
            }
        }

        /**
         * Returns whether this module is available in the supplied version.
         *
         * @param version selected Ignition image version
         * @return {@code true} when the module is supported and old enough
         */
        public boolean isAvailableIn(IgnitionVersion version) {
            Objects.requireNonNull(version, "version");

            return status == Status.SUPPORTED && version.isAtLeast(since);
        }
    }

    /**
     * Result of resolving a logical module selection.
     *
     * @param identifiers ordered, de-duplicated module identifiers
     * @param resolvedModules modules included after resolving dependencies
     * @param endpoints endpoints contributed by the resolved modules
     * @param warnings compatibility warnings generated during resolution
     */
    public record Resolution(
            List<String> identifiers,
            Set<IgnitionModule> resolvedModules,
            Set<IgnitionEndpoint> endpoints,
            List<String> warnings) {

        /**
         * Defensively copies all resolved collections.
         *
         * @param identifiers ordered, de-duplicated module identifiers
         * @param resolvedModules modules included after resolving dependencies
         * @param endpoints endpoints contributed by the resolved modules
         * @param warnings compatibility warnings generated during resolution
         */
        public Resolution {
            identifiers = List.copyOf(Objects.requireNonNull(identifiers, "identifiers"));

            resolvedModules = Collections.unmodifiableSet(
                    new LinkedHashSet<>(Objects.requireNonNull(resolvedModules, "resolvedModules")));

            endpoints =
                    Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(endpoints, "endpoints")));

            warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
        }
    }

    /** Human-readable name of the owning Ignition profile. */
    private final String profileName;

    /** Complete module definition table for the profile. */
    private final Map<IgnitionModule, Entry> entries;

    /**
     * Creates an immutable module catalog.
     */
    private ModuleCatalog(String profileName, Map<IgnitionModule, Entry> entries) {
        this.profileName = requireNonBlank(profileName, "profileName");

        this.entries = Collections.unmodifiableMap(new EnumMap<>(Objects.requireNonNull(entries, "entries")));
    }

    /**
     * Creates a module catalog builder for an Ignition profile.
     *
     * @param profileName human-readable profile name
     * @return a new catalog builder
     */
    public static Builder profile(String profileName) {
        return new Builder(profileName);
    }

    /**
     * Returns the human-readable name of the owning profile.
     *
     * @return profile name
     */
    public String profileName() {
        return profileName;
    }

    /**
     * Returns the definition of a logical module.
     *
     * <p>Completed catalogs contain an entry for every
     * {@link IgnitionModule}, including unsupported modules.
     *
     * @param module logical module
     * @return profile-specific module definition
     */
    public Entry get(IgnitionModule module) {
        Entry entry = entries.get(Objects.requireNonNull(module, "module"));

        if (entry == null) {
            throw new IllegalStateException("Profile '%s' does not declare module %s".formatted(profileName, module));
        }

        return entry;
    }

    /**
     * Look up a module identifier.
     **
     * @param identifier image specific identifier
     * @return logical Ignition module
     */
    public Optional<IgnitionModule> find(String identifier) {
        Objects.requireNonNull(identifier, "identifier");

        return entries.entrySet().stream()
                .filter(e -> e.getValue().identifier().equals(identifier))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Returns the complete immutable module definition table.
     *
     * @return module definitions keyed by logical module
     */
    public Map<IgnitionModule, Entry> entries() {
        return entries;
    }

    /**
     * Resolves logical modules into image identifiers, dependencies,
     * endpoints, and compatibility warnings.
     *
     * @param version selected Ignition image version
     * @param requestedModules requested logical modules
     * @return resolved module configuration
     */
    public Resolution resolve(IgnitionVersion version, Set<IgnitionModule> requestedModules) {
        Objects.requireNonNull(version, "version");

        Objects.requireNonNull(requestedModules, "requestedModules");

        LinkedHashSet<String> identifiers = new LinkedHashSet<>();

        LinkedHashSet<IgnitionModule> resolvedModules = new LinkedHashSet<>();

        LinkedHashSet<IgnitionEndpoint> resolvedEndpoints = new LinkedHashSet<>();

        List<String> warnings = new ArrayList<>();

        EnumSet<IgnitionModule> visiting = EnumSet.noneOf(IgnitionModule.class);

        for (IgnitionModule requestedModule : requestedModules) {
            resolve(
                    version,
                    Objects.requireNonNull(requestedModule, "requestedModules cannot contain null"),
                    null,
                    identifiers,
                    resolvedModules,
                    resolvedEndpoints,
                    visiting,
                    warnings);
        }

        return new Resolution(List.copyOf(identifiers), resolvedModules, resolvedEndpoints, List.copyOf(warnings));
    }

    /**
     * Recursively resolves one module and its dependencies.
     */
    private void resolve(
            IgnitionVersion version,
            IgnitionModule module,
            IgnitionModule requiredBy,
            LinkedHashSet<String> identifiers,
            LinkedHashSet<IgnitionModule> resolvedModules,
            LinkedHashSet<IgnitionEndpoint> resolvedEndpoints,
            EnumSet<IgnitionModule> visiting,
            List<String> warnings) {
        if (resolvedModules.contains(module)) {
            return;
        }

        if (!visiting.add(module)) {
            throw new IllegalStateException(
                    "Circular module dependency in profile '%s': %s".formatted(profileName, module));
        }

        Entry entry = get(module);

        if (entry.status() == Status.UNSUPPORTED) {
            String dependencyContext =
                    requiredBy == null ? "" : " It is required by '%s'.".formatted(requiredBy.displayName());

            warnings.add("Module '%s' is not available in profile '%s': %s.%s"
                    .formatted(module.displayName(), profileName, entry.description(), dependencyContext));

            visiting.remove(module);
            return;
        }

        if (!entry.isAvailableIn(version)) {
            warnings.add(
                    "Module '%s' is documented for Ignition %s or newer, but image %s was requested. Identifier %s will still be applied."
                            .formatted(module.displayName(), entry.since(), version, entry.identifier()));
        }

        for (IgnitionModule dependency : entry.dependencies()) {
            resolve(version, dependency, module, identifiers, resolvedModules, resolvedEndpoints, visiting, warnings);
        }

        identifiers.add(entry.identifier());

        resolvedEndpoints.addAll(entry.endpoints());

        resolvedModules.add(module);
        visiting.remove(module);
    }

    /**
     * Builds a module catalog for one Ignition profile.
     *
     * <p>Only supported modules need to be declared. Any logical module
     * omitted from the builder is marked unsupported by
     * {@link #complete()}.
     */
    public static final class Builder {

        /** Human-readable name of the owning profile. */
        private final String profileName;

        /** Explicit module declarations made by the profile. */
        private final EnumMap<IgnitionModule, Entry> entries = new EnumMap<>(IgnitionModule.class);

        /**
         * Creates a builder for the supplied profile.
         */
        private Builder(String profileName) {
            this.profileName = requireNonBlank(profileName, "profileName");
        }

        /**
         * Declares a supported module.
         *
         * @param module logical module
         * @param since first version supporting the module
         * @param definition profile-specific module definition
         * @return this builder
         */
        public Builder supported(IgnitionModule module, String since, ModuleDefinition definition) {
            Objects.requireNonNull(definition, "definition");

            put(
                    module,
                    new Entry(
                            Status.SUPPORTED,
                            IgnitionVersion.parse(since),
                            definition.identifier(),
                            definition.dependencies(),
                            definition.endpoints(),
                            definition.description()));

            return this;
        }

        /**
         * Explicitly declares an unsupported module with a custom reason.
         *
         * <p>This is optional. Unspecified modules are automatically marked
         * unsupported when the catalog is completed.
         *
         * @param module logical module
         * @param reason reason the module is unavailable
         * @return this builder
         */
        public Builder unsupported(IgnitionModule module, String reason) {
            put(module, unsupportedEntry(reason));

            return this;
        }

        /**
         * Adds default unsupported entries and creates the immutable catalog.
         *
         * @return completed module catalog
         */
        public ModuleCatalog complete() {
            for (IgnitionModule module : IgnitionModule.values()) {
                entries.putIfAbsent(module, unsupportedEntry(DEFAULT_UNSUPPORTED_REASON));
            }

            return new ModuleCatalog(profileName, entries);
        }

        /**
         * Adds one explicit module declaration.
         */
        private void put(IgnitionModule module, Entry entry) {
            Objects.requireNonNull(module, "module");

            Objects.requireNonNull(entry, "entry");

            if (entries.putIfAbsent(module, entry) != null) {
                throw new IllegalStateException(
                        "Module %s is declared more than once in profile '%s'".formatted(module, profileName));
            }
        }
    }

    /**
     * Creates an unsupported module entry.
     */
    private static Entry unsupportedEntry(String reason) {
        return new Entry(Status.UNSUPPORTED, null, "", Set.of(), Set.of(), requireNonBlank(reason, "reason"));
    }

    /**
     * Validates and normalizes a required string.
     */
    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }

        return trimmed;
    }
}
