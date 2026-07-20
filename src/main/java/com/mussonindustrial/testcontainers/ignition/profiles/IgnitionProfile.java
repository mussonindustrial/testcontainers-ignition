package com.mussonindustrial.testcontainers.ignition.profiles;

import com.mussonindustrial.testcontainers.ignition.IgnitionCapability;
import com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import com.mussonindustrial.testcontainers.ignition.compatibility.*;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerPlan;
import com.mussonindustrial.testcontainers.ignition.internal.IgnitionContainerSpec;
import java.time.Duration;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

/**
 * Describes the Docker API contract for an Ignition release line.
 *
 * <p>A profile declares the capabilities, modules, endpoints, readiness
 * behavior, and container configuration rules used by a compatible Ignition
 * image.
 *
 * <p>Profiles normally represent an Ignition image generation rather than a
 * single patch release. For example, one profile may support every
 * {@code 8.1.x} image while using capability and module version metadata to
 * describe differences between patch releases.
 */
public interface IgnitionProfile {

    /**
     * Returns the human-readable name of this profile.
     *
     * @return a name such as {@code "Ignition 8.1"}
     */
    String name();

    /**
     * Determines whether this profile applies to an Ignition image version.
     *
     * @param version parsed Ignition image version
     * @return {@code true} when this profile supports the release line
     */
    boolean matches(IgnitionVersion version);

    /**
     * Returns the capabilities available from this Ignition image generation.
     *
     * <p>The matrix declares whether each public capability is supported and
     * the first version in which supported capabilities became available.
     *
     * @return this profile's capability matrix
     */
    CapabilityMatrix capabilities();

    /**
     * Returns the built-in modules available from this Ignition image
     * generation.
     *
     * <p>The catalog maps stable logical modules to their profile-specific
     * identifiers, dependencies, endpoints, and version requirements.
     *
     * @return this profile's module catalog
     */
    ModuleCatalog modules();

    /**
     * Returns the network endpoints defined by this Ignition image generation.
     *
     * <p>The catalog declares container ports independently of the modules
     * and capabilities that cause those ports to be exposed.
     *
     * @return this profile's endpoint catalog
     */
    EndpointCatalog endpoints();

    /**
     * Returns the function that translates a capability into container
     * configuration.
     *
     * <p>A missing applier indicates that the profile cannot translate the
     * requested capability. The caller should warn and ignore the capability
     * rather than fail container configuration.
     *
     * @param capability requested semantic capability
     * @return the capability applier, or {@code null} when unavailable
     */
    CapabilityApplier applier(IgnitionCapability capability);

    /**
     * Creates the strategy used to determine when the Gateway is ready.
     *
     * <p>A new strategy instance is returned for each invocation so that
     * container instances do not share a mutable wait-strategy state.
     *
     * @param version selected Ignition image version
     * @return readiness strategy for the selected image
     */
    default WaitStrategy createWaitStrategy(IgnitionVersion version) {
        return Wait.forHttp("/StatusPing")
                .forPort(endpoints().port(IgnitionEndpoint.GATEWAY_HTTP))
                .forResponsePredicate("{\"state\":\"RUNNING\"}"::equals)
                .withStartupTimeout(Duration.ofMinutes(5));
    }

    /**
     * Applies configuration that is independent of explicitly requested
     * capabilities.
     *
     * <p>Defaults should be limited to settings that apply to the profile as
     * a whole, such as the timezone and standard Gateway endpoints.
     * Version-dependent optional behavior should be applied through capability
     * appliers instead.
     *
     * @param version selected Ignition image version
     * @param specification normalized container specification
     * @param plan mutable container plan
     */
    default void applyDefaults(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        // No defaults.
    }

    /**
     * Returns the support declaration for a capability.
     *
     * @param capability capability to inspect
     * @return support declaration for the capability
     * @throws IllegalStateException if the capability matrix does not contain
     *                               the requested capability
     */
    default CapabilitySupport supportFor(IgnitionCapability capability) {
        CapabilitySupport support = capabilities().get(capability);

        if (support == null) {
            throw new IllegalStateException(
                    "Profile '%s' does not declare capability %s".formatted(name(), capability));
        }

        return support;
    }

    /**
     * Applies one requested capability to a container plan.
     *
     * <p>This method performs only the profile-specific translation.
     * Compatibility warnings for unavailable or version-gated capabilities
     * are handled by the container before this method is invoked.
     *
     * @param capability requested capability
     * @param version selected Ignition image version
     * @param specification normalized container specification
     * @param plan mutable container plan
     * @return {@code true} when an applier was available
     */
    default boolean apply(
            IgnitionCapability capability,
            IgnitionVersion version,
            IgnitionContainerSpec specification,
            ContainerPlan.Builder plan) {
        CapabilityApplier capabilityApplier = applier(capability);

        if (capabilityApplier == null) {
            return false;
        }

        capabilityApplier.apply(version, specification, plan);

        return true;
    }
}
