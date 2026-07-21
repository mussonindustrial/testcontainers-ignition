package com.mussonindustrial.testcontainers.ignition.profiles;

import com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilityCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.EndpointCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.ModuleCatalog;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerPlan;
import com.mussonindustrial.testcontainers.ignition.internal.IgnitionContainerSpec;
import java.time.Duration;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

/**
 * Describes the Docker API contract for an Ignition release line.
 *
 * <p>A profile defines the capabilities, modules, endpoints, readiness
 * behavior, and container configuration used by compatible Ignition images.
 */
public interface IgnitionProfile {

    /**
     * Returns the profile name.
     *
     * @return profile name
     */
    String name();

    /**
     * Returns whether this profile supports a version.
     *
     * @param version Ignition image version
     * @return {@code true} when the profile matches
     */
    boolean matches(IgnitionVersion version);

    /**
     * Returns the profile's capability catalog.
     *
     * <p>The catalog defines capability availability and version-specific
     * container configuration appliers.
     *
     * @return capability catalog
     */
    CapabilityCatalog capabilities();

    /**
     * Returns the profile's built-in module catalog.
     *
     * @return module catalog
     */
    ModuleCatalog modules();

    /**
     * Returns the profile's network endpoint catalog.
     *
     * @return endpoint catalog
     */
    EndpointCatalog endpoints();

    /**
     * Creates the Gateway readiness strategy.
     *
     * @param version Ignition image version
     * @return readiness strategy
     */
    default WaitStrategy createWaitStrategy(IgnitionVersion version) {
        return Wait.forHttp("/StatusPing")
                .forPort(endpoints().port(IgnitionEndpoint.GATEWAY_HTTP))
                .forResponsePredicate("{\"state\":\"RUNNING\"}"::equals)
                .withStartupTimeout(Duration.ofMinutes(5));
    }

    /**
     * Applies profile-wide container configuration.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    default void applyDefaults(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        // No defaults.
    }
}
