package com.mussonindustrial.testcontainers.ignition.internal;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Describes a file copied into a container.
 *
 * @param source host source path
 * @param destination container destination path
 * @param mode Unix file mode
 */
public record ContainerFileCopy(Path source, String destination, int mode) {

    /** Default file mode. */
    public static final int DEFAULT_MODE = 0644;

    /** Read-only file mode. */
    public static final int READ_ONLY_MODE = 0444;

    /**
     * Validates the file copy.
     *
     * @param source host source path
     * @param destination container destination path
     * @param mode Unix file mode
     */
    public ContainerFileCopy {
        Objects.requireNonNull(source, "source");

        destination = requireAbsoluteContainerPath(destination);

        validateMode(mode);
    }

    /**
     * Creates a file copy using the default mode.
     *
     * @param source host source path
     * @param destination container destination path
     */
    public ContainerFileCopy(Path source, String destination) {
        this(source, destination, DEFAULT_MODE);
    }

    /**
     * Creates a file copy using the default mode.
     *
     * @param source host source path
     * @param destination container destination path
     * @return file copy
     */
    public static ContainerFileCopy copy(Path source, String destination) {
        return new ContainerFileCopy(source, destination, DEFAULT_MODE);
    }

    /**
     * Creates a read-only file copy.
     *
     * @param source host source path
     * @param destination container destination path
     * @return read-only file copy
     */
    public static ContainerFileCopy readOnly(Path source, String destination) {
        return new ContainerFileCopy(source, destination, READ_ONLY_MODE);
    }

    /**
     * Validates a container destination path.
     */
    private static String requireAbsoluteContainerPath(String destination) {
        Objects.requireNonNull(destination, "destination");

        String trimmed = destination.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Container destination cannot be blank");
        }

        if (!trimmed.startsWith("/")) {
            throw new IllegalArgumentException("Container destination must be absolute: " + destination);
        }

        if (trimmed.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Container destination cannot contain " + "a null character");
        }

        return trimmed;
    }

    /**
     * Validates a Unix file mode.
     */
    private static void validateMode(int mode) {
        if (mode < 0 || mode > 0777) {
            throw new IllegalArgumentException("Invalid Unix file mode: " + Integer.toOctalString(mode));
        }
    }
}
