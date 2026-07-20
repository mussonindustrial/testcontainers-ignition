package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Describes a profile-specific Ignition module.
 *
 * @param identifiers image-specific module identifiers
 * @param dependencies required logical modules
 * @param endpoints network endpoints exposed by the module
 * @param description module description
 */
public record ModuleDefinition(
        List<String> identifiers,
        Set<IgnitionModule> dependencies,
        Set<IgnitionEndpoint> endpoints,
        String description) {

    /**
     * Validates and copies the module definition.
     *
     * @param identifiers image-specific module identifiers
     * @param dependencies required logical modules
     * @param endpoints network endpoints exposed by the module
     * @param description module description
     */
    public ModuleDefinition {
        identifiers = List.copyOf(Objects.requireNonNull(identifiers, "identifiers"));
        dependencies = Set.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        endpoints = Set.copyOf(Objects.requireNonNull(endpoints, "endpoints"));
        description = Objects.requireNonNull(description, "description");

        if (identifiers.isEmpty()) {
            throw new IllegalArgumentException("A module must have at least one identifier");
        }

        if (identifiers.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Module identifiers cannot be blank");
        }
    }

    /**
     * Creates a built-in module definition.
     *
     * @param identifiers image-specific module identifiers
     * @return module definition
     */
    public static ModuleDefinition module(String... identifiers) {
        Objects.requireNonNull(identifiers, "identifiers");

        return new ModuleDefinition(List.of(identifiers), Set.of(), Set.of(), "Built-in module");
    }

    /**
     * Adds required logical modules.
     *
     * @param modules required modules
     * @return updated module definition
     */
    public ModuleDefinition requires(IgnitionModule... modules) {
        Objects.requireNonNull(modules, "modules");

        LinkedHashSet<IgnitionModule> combined = new LinkedHashSet<>(dependencies);

        Arrays.stream(modules)
                .map(module -> Objects.requireNonNull(module, "modules cannot contain null"))
                .forEach(combined::add);

        return new ModuleDefinition(identifiers, combined, endpoints, description);
    }

    /**
     * Adds network endpoints exposed by the module.
     *
     * @param endpoints exposed endpoints
     * @return updated module definition
     */
    public ModuleDefinition exposes(IgnitionEndpoint... endpoints) {
        Objects.requireNonNull(endpoints, "endpoints");

        LinkedHashSet<IgnitionEndpoint> combined = new LinkedHashSet<>(this.endpoints);

        Arrays.stream(endpoints)
                .map(endpoint -> Objects.requireNonNull(endpoint, "endpoints cannot contain null"))
                .forEach(combined::add);

        return new ModuleDefinition(identifiers, dependencies, combined, description);
    }

    /**
     * Replaces the module description.
     *
     * @param description module description
     * @return updated module definition
     */
    public ModuleDefinition describedAs(String description) {
        return new ModuleDefinition(identifiers, dependencies, endpoints, description);
    }
}
