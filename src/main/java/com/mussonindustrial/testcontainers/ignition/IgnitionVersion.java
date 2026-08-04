package com.mussonindustrial.testcontainers.ignition;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.utility.DockerImageName;

/**
 * Represents a parsed Ignition image version.
 *
 * @param major major version
 * @param minor minor version
 * @param patch patch version
 * @param qualifier optional version qualifier
 */
public record IgnitionVersion(int major, int minor, int patch, String qualifier)
        implements Comparable<IgnitionVersion> {

    /** Pattern used to parse Ignition version strings. */
    private static final Pattern VERSION_PATTERN = Pattern.compile("^[vV]?"
            + "(?<major>\\d+)"
            + "\\.(?<minor>\\d+)"
            + "\\.(?<patch>\\d+)"
            + "(?:[-_.](?<qualifier>"
            + "[A-Za-z0-9][A-Za-z0-9._-]*))?"
            + "$");

    /** Pattern used to split qualifiers into comparable parts. */
    private static final Pattern QUALIFIER_PART_PATTERN = Pattern.compile("\\d+|[A-Za-z]+");

    /**
     * Validates and normalizes the version.
     *
     * @param major major version
     * @param minor minor version
     * @param patch patch version
     * @param qualifier optional version qualifier
     */
    public IgnitionVersion {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version components cannot be negative");
        }

        qualifier = normalizeQualifier(qualifier);
    }

    /**
     * Creates a stable Ignition version.
     *
     * @param major major version
     * @param minor minor version
     * @param patch patch version
     */
    public IgnitionVersion(int major, int minor, int patch) {
        this(major, minor, patch, "");
    }

    /**
     * Parses an Ignition version string.
     *
     * @param value version string
     * @return parsed version
     */
    public static IgnitionVersion parse(String value) {
        Objects.requireNonNull(value, "value");

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Ignition version cannot be blank");
        }

        Matcher matcher = VERSION_PATTERN.matcher(trimmed);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid Ignition version '%s'. Expected a concrete version such as '8.1.48', '8.3.0', or '2027.2.0'."
                            .formatted(value));
        }

        return new IgnitionVersion(
                Integer.parseInt(matcher.group("major")),
                Integer.parseInt(matcher.group("minor")),
                Integer.parseInt(matcher.group("patch")),
                matcher.group("qualifier"));
    }

    /**
     * Parses the version tag from a Docker image name.
     *
     * @param imageName Docker image name
     * @return parsed version
     */
    public static IgnitionVersion from(DockerImageName imageName) {
        Objects.requireNonNull(imageName, "imageName");

        String tag = imageName.getVersionPart();

        if (tag == null || tag.isBlank() || tag.equalsIgnoreCase("latest")) {
            throw new IllegalArgumentException(
                    "A concrete Ignition image tag is required; " + "received '%s'.".formatted(imageName));
        }

        try {
            return parse(tag);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Cannot determine the Ignition version " + "from image '%s'".formatted(imageName), exception);
        }
    }

    /**
     * Returns whether this version belongs to a release line.
     *
     * @param major required major version
     * @param minor required minor version
     * @return {@code true} when the release line matches
     */
    public boolean isReleaseLine(int major, int minor) {
        return this.major == major && this.minor == minor;
    }

    /**
     * Returns whether this version meets a minimum version.
     *
     * @param minimum minimum version
     * @return {@code true} when this version is new enough
     */
    public boolean isAtLeast(IgnitionVersion minimum) {
        return compareTo(minimum) >= 0;
    }

    /**
     * Returns whether this version meets a minimum version.
     *
     * @param minimum minimum version string
     * @return {@code true} when this version is new enough
     */
    public boolean isAtLeast(String minimum) {
        return isAtLeast(parse(minimum));
    }

    /**
     * Returns whether this version precedes another version.
     *
     * @param other version to compare
     * @return {@code true} when this version is older
     */
    public boolean isBefore(IgnitionVersion other) {
        return compareTo(other) < 0;
    }

    /**
     * Returns whether this version precedes another version.
     *
     * @param other version string to compare
     * @return {@code true} when this version is older
     */
    public boolean isBefore(String other) {
        return isBefore(parse(other));
    }

    /**
     * Returns whether this version has a qualifier.
     *
     * @return {@code true} when a qualifier is present
     */
    public boolean hasQualifier() {
        return !qualifier.isBlank();
    }

    /**
     * Returns whether this version is a prerelease.
     *
     * @return {@code true} for prerelease qualifiers
     */
    public boolean isPrerelease() {
        return isPrereleaseQualifier(qualifier);
    }

    /**
     * Compares this version to another version.
     *
     * @param other version to compare
     * @return comparison result
     */
    @Override
    public int compareTo(@NotNull IgnitionVersion other) {
        Objects.requireNonNull(other, "other");

        int comparison = Integer.compare(major, other.major);

        if (comparison != 0) {
            return comparison;
        }

        comparison = Integer.compare(minor, other.minor);

        if (comparison != 0) {
            return comparison;
        }

        comparison = Integer.compare(patch, other.patch);

        if (comparison != 0) {
            return comparison;
        }

        return compareQualifiers(qualifier, other.qualifier);
    }

    /**
     * Returns the normalized version string.
     *
     * @return version string
     */
    @Override
    public @NotNull String toString() {
        String version = "%d.%d.%d".formatted(major, minor, patch);

        return qualifier.isBlank() ? version : version + "-" + qualifier;
    }

    /**
     * Compares two version qualifiers.
     */
    private static int compareQualifiers(String left, String right) {
        if (left.equals(right)) {
            return 0;
        }

        boolean leftBlank = left.isBlank();
        boolean rightBlank = right.isBlank();

        if (leftBlank && rightBlank) {
            return 0;
        }

        boolean leftPrerelease = isPrereleaseQualifier(left);

        boolean rightPrerelease = isPrereleaseQualifier(right);

        if (leftPrerelease != rightPrerelease) {
            return leftPrerelease ? -1 : 1;
        }

        if (leftBlank != rightBlank) {
            return leftBlank ? -1 : 1;
        }

        return compareQualifierParts(left, right);
    }

    /**
     * Compares the parts of two qualifiers.
     */
    private static int compareQualifierParts(String left, String right) {
        Matcher leftMatcher = QUALIFIER_PART_PATTERN.matcher(left);

        Matcher rightMatcher = QUALIFIER_PART_PATTERN.matcher(right);

        while (leftMatcher.find() && rightMatcher.find()) {
            String leftPart = leftMatcher.group();

            String rightPart = rightMatcher.group();

            int comparison = compareQualifierPart(leftPart, rightPart);

            if (comparison != 0) {
                return comparison;
            }
        }

        return left.compareToIgnoreCase(right);
    }

    /**
     * Compares two individual qualifier parts.
     */
    private static int compareQualifierPart(String left, String right) {
        boolean leftNumeric = Character.isDigit(left.charAt(0));

        boolean rightNumeric = Character.isDigit(right.charAt(0));

        if (leftNumeric && rightNumeric) {
            return Integer.compare(Integer.parseInt(left), Integer.parseInt(right));
        }

        if (leftNumeric != rightNumeric) {
            return leftNumeric ? 1 : -1;
        }

        int leftRank = qualifierRank(left);
        int rightRank = qualifierRank(right);

        if (leftRank != rightRank) {
            return Integer.compare(leftRank, rightRank);
        }

        return left.compareToIgnoreCase(right);
    }

    /**
     * Returns the ordering rank of a qualifier.
     */
    private static int qualifierRank(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "snapshot", "dev" -> 0;
            case "alpha", "a" -> 10;
            case "beta", "b" -> 20;
            case "milestone", "m" -> 30;
            case "preview", "pre" -> 40;
            case "ea" -> 50;
            case "rc", "cr" -> 60;
            default -> 100;
        };
    }

    /**
     * Returns whether a qualifier identifies a prerelease.
     */
    private static boolean isPrereleaseQualifier(String qualifier) {
        if (qualifier.isBlank()) {
            return false;
        }

        Matcher matcher = QUALIFIER_PART_PATTERN.matcher(qualifier.toLowerCase(Locale.ROOT));

        while (matcher.find()) {
            String part = matcher.group();

            if (part.matches("alpha|a|beta|b|rc|cr|snapshot|preview|pre|dev|ea|milestone|m")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Normalizes an optional version qualifier.
     */
    private static String normalizeQualifier(String qualifier) {
        if (qualifier == null) {
            return "";
        }

        return qualifier.trim().replaceAll("^[._-]+", "").replaceAll("[._-]+$", "");
    }
}
