package com.mussonindustrial.testcontainers.ignition.internal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Describes the Docker configuration produced by an Ignition profile.
 */
public final class ContainerPlan {

    /** Environment variables applied to the container. */
    private final Map<String, String> environment;

    /** Command arguments passed to the container. */
    private final List<String> command;

    /** Container ports exposed by the plan. */
    private final Set<Integer> exposedPorts;

    /** Host files copied into the container. */
    private final List<ContainerFileCopy> fileCopies;

    /** In-memory files created inside the container. */
    private final List<GeneratedContainerFile> generatedFiles;

    /** Compatibility warnings produced while building the plan. */
    private final List<String> warnings;

    /**
     * Creates an immutable container plan.
     */
    private ContainerPlan(Builder builder) {
        environment = Collections.unmodifiableMap(new LinkedHashMap<>(builder.environment));

        command = List.copyOf(builder.command);

        exposedPorts = Collections.unmodifiableSet(new LinkedHashSet<>(builder.exposedPorts));

        fileCopies = List.copyOf(builder.fileCopies);
        generatedFiles = List.copyOf(builder.generatedFiles);
        warnings = List.copyOf(builder.warnings);
    }

    /**
     * Creates a container plan builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the environment variables.
     *
     * @return immutable environment map
     */
    public Map<String, String> environment() {
        return environment;
    }

    /**
     * Returns the container command arguments.
     *
     * @return immutable command list
     */
    public List<String> command() {
        return command;
    }

    /**
     * Returns the exposed container ports.
     *
     * @return immutable port set
     */
    public Set<Integer> exposedPorts() {
        return exposedPorts;
    }

    /**
     * Returns the host files copied into the container.
     *
     * @return immutable file-copy list
     */
    public List<ContainerFileCopy> fileCopies() {
        return fileCopies;
    }

    /**
     * Returns the files generated inside the container.
     *
     * @return immutable generated-file list
     */
    public List<GeneratedContainerFile> generatedFiles() {
        return generatedFiles;
    }

    /**
     * Returns the compatibility warnings.
     *
     * @return immutable warning list
     */
    public List<String> warnings() {
        return warnings;
    }

    /**
     * Returns whether the plan contains no configuration or warnings.
     *
     * @return {@code true} when the plan is empty
     */
    public boolean isEmpty() {
        return environment.isEmpty()
                && command.isEmpty()
                && exposedPorts.isEmpty()
                && fileCopies.isEmpty()
                && generatedFiles.isEmpty()
                && warnings.isEmpty();
    }

    /**
     * Builds a container plan.
     */
    public static final class Builder {

        /** Environment variables collected by the builder. */
        private final Map<String, String> environment = new LinkedHashMap<>();

        /** Command arguments collected by the builder. */
        private final List<String> command = new ArrayList<>();

        /** Exposed ports collected by the builder. */
        private final Set<Integer> exposedPorts = new LinkedHashSet<>();

        /** Host file copies collected by the builder. */
        private final List<ContainerFileCopy> fileCopies = new ArrayList<>();

        /** Generated files collected by the builder. */
        private final List<GeneratedContainerFile> generatedFiles = new ArrayList<>();

        /** Container paths already reserved by files. */
        private final Set<String> containerDestinations = new LinkedHashSet<>();

        /** Compatibility warnings collected by the builder. */
        private final List<String> warnings = new ArrayList<>();

        /** Creates an empty builder. */
        private Builder() {}

        /**
         * Adds an environment variable.
         *
         * @param name variable name
         * @param value variable value
         * @return this builder
         */
        public Builder environment(String name, String value) {
            environment.put(requireEnvironmentName(name), requireEnvironmentValue(value));

            return this;
        }

        /**
         * Adds environment variables.
         *
         * @param values variables to add
         * @return this builder
         */
        public Builder environment(Map<String, String> values) {
            Objects.requireNonNull(values, "values");

            values.forEach(this::environment);
            return this;
        }

        /**
         * Adds a command argument.
         *
         * @param value argument value
         * @return this builder
         */
        public Builder argument(String value) {
            command.add(requireCommandArgument(value));

            return this;
        }

        /**
         * Adds command arguments.
         *
         * @param values argument values
         * @return this builder
         */
        public Builder arguments(String... values) {
            Objects.requireNonNull(values, "values");

            Arrays.stream(values).forEach(this::argument);

            return this;
        }

        /**
         * Adds command arguments.
         *
         * @param values argument values
         * @return this builder
         */
        public Builder arguments(Collection<String> values) {
            Objects.requireNonNull(values, "values");

            values.forEach(this::argument);
            return this;
        }

        /**
         * Exposes a container port.
         *
         * @param port container port
         * @return this builder
         */
        public Builder expose(int port) {
            validatePort(port);
            exposedPorts.add(port);
            return this;
        }

        /**
         * Exposes container ports.
         *
         * @param ports container ports
         * @return this builder
         */
        public Builder expose(int... ports) {
            Objects.requireNonNull(ports, "ports");

            Arrays.stream(ports).forEach(this::expose);

            return this;
        }

        /**
         * Adds a host file copy.
         *
         * @param copy file-copy definition
         * @return this builder
         */
        public Builder copy(ContainerFileCopy copy) {
            Objects.requireNonNull(copy, "copy");

            reserveDestination(copy.destination());
            fileCopies.add(copy);

            return this;
        }

        /**
         * Adds a host file copy using the default mode.
         *
         * @param source host source path
         * @param destination container destination path
         * @return this builder
         */
        public Builder copy(Path source, String destination) {
            return copy(new ContainerFileCopy(source, destination));
        }

        /**
         * Adds a host file copy.
         *
         * @param source host source path
         * @param destination container destination path
         * @param mode Unix file mode
         * @return this builder
         */
        public Builder copy(Path source, String destination, int mode) {
            return copy(new ContainerFileCopy(source, destination, mode));
        }

        /**
         * Adds an in-memory container file.
         *
         * @param file generated-file definition
         * @return this builder
         */
        public Builder generatedFile(GeneratedContainerFile file) {
            Objects.requireNonNull(file, "file");

            reserveDestination(file.destination());
            generatedFiles.add(file);

            return this;
        }

        /**
         * Adds a binary container file using the default mode.
         *
         * @param destination container destination path
         * @param contents file contents
         * @return this builder
         */
        public Builder generatedFile(String destination, byte[] contents) {
            return generatedFile(GeneratedContainerFile.bytes(destination, contents));
        }

        /**
         * Adds a UTF-8 container file using the default mode.
         *
         * @param destination container destination path
         * @param contents file contents
         * @return this builder
         */
        public Builder generatedFile(String destination, String contents) {
            return generatedFile(GeneratedContainerFile.text(destination, contents));
        }

        /**
         * Adds a UTF-8 container file.
         *
         * @param destination container destination path
         * @param contents file contents
         * @param mode Unix file mode
         * @return this builder
         */
        public Builder generatedFile(String destination, String contents, int mode) {
            Objects.requireNonNull(contents, "contents");

            return generatedFile(
                    new GeneratedContainerFile(destination, contents.getBytes(StandardCharsets.UTF_8), mode));
        }

        /**
         * Adds a compatibility warning.
         *
         * @param warning warning message
         * @return this builder
         */
        public Builder warning(String warning) {
            Objects.requireNonNull(warning, "warning");

            String trimmed = warning.trim();

            if (!trimmed.isEmpty()) {
                warnings.add(trimmed);
            }

            return this;
        }

        /**
         * Merges another plan into this builder.
         *
         * <p>Environment values from the other plan replace existing values.
         *
         * @param other plan to merge
         * @return this builder
         */
        public Builder merge(ContainerPlan other) {
            Objects.requireNonNull(other, "other");

            environment(other.environment());
            arguments(other.command());

            other.exposedPorts().forEach(this::expose);

            other.fileCopies().forEach(this::copy);

            other.generatedFiles().forEach(this::generatedFile);

            other.warnings().forEach(this::warning);

            return this;
        }

        /**
         * Creates the immutable container plan.
         *
         * @return completed plan
         */
        public ContainerPlan build() {
            return new ContainerPlan(this);
        }

        /**
         * Reserves a destination path for one container file.
         */
        private void reserveDestination(String destination) {
            if (!containerDestinations.add(destination)) {
                throw new IllegalStateException("Multiple container files target the " + "same path: " + destination);
            }
        }

        /**
         * Validates an environment variable name.
         */
        private static String requireEnvironmentName(String value) {
            Objects.requireNonNull(value, "name");

            String trimmed = value.trim();

            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Environment variable name cannot be blank");
            }

            if (trimmed.indexOf('=') >= 0) {
                throw new IllegalArgumentException("Environment variable name cannot contain '=': " + value);
            }

            if (trimmed.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Environment variable name cannot contain " + "a null character");
            }

            return trimmed;
        }

        /**
         * Validates an environment variable value.
         */
        private static String requireEnvironmentValue(String value) {
            Objects.requireNonNull(value, "value");

            if (value.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Environment variable value cannot contain " + "a null character");
            }

            return value;
        }

        /**
         * Validates a command argument.
         */
        private static String requireCommandArgument(String value) {
            Objects.requireNonNull(value, "command argument");

            if (value.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Command argument cannot contain " + "a null character");
            }

            return value;
        }

        /**
         * Validates a container port.
         */
        private static void validatePort(int port) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535: " + port);
            }
        }
    }
}
