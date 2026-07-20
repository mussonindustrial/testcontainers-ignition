package com.mussonindustrial.testcontainers.ignition.compatibility;

import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;

/**
 * Describes whether a capability is supported by an Ignition profile.
 *
 * @param status support status
 * @param since first supported version
 * @param reason reason the capability is unsupported
 */
public record CapabilitySupport(CapabilityStatus status, IgnitionVersion since, String reason) {

    /**
     * Creates a supported capability declaration.
     *
     * @param version first supported version
     * @return supported capability declaration
     */
    public static CapabilitySupport supportedSince(String version) {
        return new CapabilitySupport(CapabilityStatus.SUPPORTED, IgnitionVersion.parse(version), null);
    }

    /**
     * Creates an unsupported capability declaration.
     *
     * @param reason reason the capability is unsupported
     * @return unsupported capability declaration
     */
    public static CapabilitySupport unsupported(String reason) {
        return new CapabilitySupport(CapabilityStatus.UNSUPPORTED, null, reason);
    }

    /**
     * Returns whether the capability is available in a version.
     *
     * @param version Ignition version to inspect
     * @return {@code true} when the capability is available
     */
    public boolean isAvailableIn(IgnitionVersion version) {
        return status == CapabilityStatus.SUPPORTED && version.compareTo(since) >= 0;
    }
}
