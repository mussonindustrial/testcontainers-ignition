package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerPlan;
import com.mussonindustrial.testcontainers.ignition.internal.IgnitionContainerSpec;

/**
 * Applies one Ignition capability to a container plan.
 */
@FunctionalInterface
public interface CapabilityApplier {

    /**
     * Translates a requested capability into container configuration.
     *
     * @param version selected Ignition version
     * @param specification requested container configuration
     * @param plan container plan being constructed
     */
    void apply(IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan);
}
