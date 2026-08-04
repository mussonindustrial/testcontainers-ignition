package com.mussonindustrial.testcontainers.ignition.profiles;

import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry of the Ignition Docker API profiles supported by this library.
 */
public final class IgnitionProfiles {

    private static final List<IgnitionProfile> PROFILES = List.of(new Ignition81Profile(), new Ignition83Profile());

    private IgnitionProfiles() {}

    /**
     * Resolves the profile responsible for an Ignition version.
     *
     * @param version concrete Ignition image version
     * @return the single matching profile
     * @throws UnsupportedOperationException if no profile supports the
     *                                       release line
     * @throws IllegalStateException if multiple profiles match the version
     */
    public static IgnitionProfile resolve(IgnitionVersion version) {
        Objects.requireNonNull(version, "version");

        List<IgnitionProfile> matches =
                PROFILES.stream().filter(profile -> profile.matches(version)).toList();

        if (matches.isEmpty()) {
            throw unsupportedVersion(version);
        }

        if (matches.size() > 1) {
            throw ambiguousVersion(version, matches);
        }

        return matches.get(0);
    }

    /**
     * Finds the profile for a version without throwing when the release line
     * is not supported.
     *
     * @param version concrete Ignition image version
     * @return the matching profile, or an empty optional
     * @throws IllegalStateException if multiple profiles match the version
     */
    public static Optional<IgnitionProfile> find(IgnitionVersion version) {
        Objects.requireNonNull(version, "version");

        List<IgnitionProfile> matches =
                PROFILES.stream().filter(profile -> profile.matches(version)).toList();

        if (matches.size() > 1) {
            throw ambiguousVersion(version, matches);
        }

        return matches.stream().findFirst();
    }

    /**
     * Determines whether this library has a profile for the supplied Ignition version.
     *
     * @param version concrete Ignition image version
     * @return true if this library has a profile for the supplied Ignition version
     */
    public static boolean supports(IgnitionVersion version) {
        return find(version).isPresent();
    }

    /**
     * Returns the complete immutable profile registry.
     *
     * @return the complete immutable profile registry
     */
    public static List<IgnitionProfile> all() {
        return PROFILES;
    }

    /**
     * Validates the static registry.
     */
    public static void validate() {
        for (IgnitionProfile profile : PROFILES) {
            Objects.requireNonNull(profile, "The profile registry cannot contain null");

            if (profile.name() == null || profile.name().isBlank()) {
                throw new IllegalStateException("Every Ignition profile must have a name");
            }

            Objects.requireNonNull(
                    profile.capabilities(), () -> "Profile '%s' has no capability matrix".formatted(profile.name()));
        }
    }

    private static UnsupportedOperationException unsupportedVersion(IgnitionVersion version) {
        String supportedProfiles = PROFILES.stream().map(IgnitionProfile::name).collect(Collectors.joining(", "));

        if (supportedProfiles.isBlank()) {
            supportedProfiles = "<none>";
        }

        return new UnsupportedOperationException(
                "No Ignition Docker profile supports version %s. Available profiles: %s"
                        .formatted(version, supportedProfiles));
    }

    private static IllegalStateException ambiguousVersion(IgnitionVersion version, List<IgnitionProfile> matches) {
        String matchingProfiles = matches.stream().map(IgnitionProfile::name).collect(Collectors.joining(", "));

        return new IllegalStateException(
                "Multiple Ignition profiles match version %s: %s".formatted(version, matchingProfiles));
    }
}
