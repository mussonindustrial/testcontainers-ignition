package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Describes a profile-specific Ignition module.
 *
 * @param identifier image-specific module identifier
 * @param dependencies required logical modules
 * @param endpoints network endpoints exposed by the module
 * @param description module description
 */
public record ModuleDefinition(
        String identifier, Set<IgnitionModule> dependencies, Set<IgnitionEndpoint> endpoints, String description) {

    /**
     * Validates and copies the module definition.
     *
     * @param identifier image-specific module identifier
     * @param dependencies required logical modules
     * @param endpoints network endpoints exposed by the module
     * @param description module description
     */
    public ModuleDefinition {
        identifier = Objects.requireNonNull(identifier, "identifier");
        dependencies = Set.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        endpoints = Set.copyOf(Objects.requireNonNull(endpoints, "endpoints"));
        description = Objects.requireNonNull(description, "description");
    }

    /**
     * Creates a built-in module definition.
     *
     * @param identifier image-specific module identifier
     * @return module definition
     */
    public static ModuleDefinition module(String identifier) {
        Objects.requireNonNull(identifier, "identifier");

        return new ModuleDefinition(identifier, Set.of(), Set.of(), "Built-in module");
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

        return new ModuleDefinition(identifier, combined, endpoints, description);
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

        return new ModuleDefinition(identifier, dependencies, combined, description);
    }

    /**
     * Replaces the module description.
     *
     * @param description module description
     * @return updated module definition
     */
    public ModuleDefinition describedAs(String description) {
        return new ModuleDefinition(identifier, dependencies, endpoints, description);
    }
}
