package com.mussonindustrial.testcontainers.ignition.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Describes an in-memory file created inside a container.
 *
 * @param destination container destination path
 * @param contents file contents
 * @param mode Unix file mode
 */
public record GeneratedContainerFile(String destination, byte[] contents, int mode) {

    /** Default file mode. */
    public static final int DEFAULT_MODE = 0644;

    /** File mode for sensitive contents. */
    public static final int SECRET_MODE = 0600;

    /**
     * Validates and copies the generated file.
     *
     * @param destination container destination path
     * @param contents file contents
     * @param mode Unix file mode
     */
    public GeneratedContainerFile {
        destination = requireAbsoluteContainerPath(destination);

        contents = Arrays.copyOf(Objects.requireNonNull(contents, "contents"), contents.length);

        validateMode(mode);
    }

    /**
     * Creates a generated file using the default mode.
     *
     * @param destination container destination path
     * @param contents file contents
     */
    public GeneratedContainerFile(String destination, byte[] contents) {
        this(destination, contents, DEFAULT_MODE);
    }

    /**
     * Returns a copy of the file contents.
     *
     * @return file contents
     */
    @Override
    public byte[] contents() {
        return Arrays.copyOf(contents, contents.length);
    }

    /**
     * Creates a binary file using the default mode.
     *
     * @param destination container destination path
     * @param contents file contents
     * @return generated file
     */
    public static GeneratedContainerFile bytes(String destination, byte[] contents) {
        return new GeneratedContainerFile(destination, contents, DEFAULT_MODE);
    }

    /**
     * Creates a UTF-8 text file.
     *
     * @param destination container destination path
     * @param contents file contents
     * @return generated file
     */
    public static GeneratedContainerFile text(String destination, String contents) {
        return text(destination, contents, StandardCharsets.UTF_8);
    }

    /**
     * Creates a text file using a charset.
     *
     * @param destination container destination path
     * @param contents file contents
     * @param charset content charset
     * @return generated file
     */
    public static GeneratedContainerFile text(String destination, String contents, Charset charset) {
        Objects.requireNonNull(contents, "contents");
        Objects.requireNonNull(charset, "charset");

        return new GeneratedContainerFile(destination, contents.getBytes(charset), DEFAULT_MODE);
    }

    /**
     * Creates a UTF-8 file using the secret mode.
     *
     * @param destination container destination path
     * @param contents file contents
     * @return generated secret file
     */
    public static GeneratedContainerFile secret(String destination, String contents) {
        Objects.requireNonNull(contents, "contents");

        return new GeneratedContainerFile(destination, contents.getBytes(StandardCharsets.UTF_8), SECRET_MODE);
    }

    /**
     * Returns the contents as UTF-8 text.
     *
     * @return file contents
     */
    public String contentsAsString() {
        return contentsAsString(StandardCharsets.UTF_8);
    }

    /**
     * Returns the contents as text using a charset.
     *
     * @param charset content charset
     * @return file contents
     */
    public String contentsAsString(Charset charset) {
        return new String(contents, Objects.requireNonNull(charset, "charset"));
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
