package com.mussonindustrial.testcontainers.ignition;

import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.BUILT_IN_MODULE_SELECTION;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.DEBUG_MODE;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.GATEWAY_EDITION;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.GATEWAY_NAME;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.GATEWAY_RESTORE;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.INITIAL_ADMIN_CONFIGURATION;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.LEASED_LICENSE_ACTIVATION;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.LICENSE_ACCEPTANCE;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.MAX_MEMORY;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.PROCESS_IDENTITY;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.QUICK_START_CONTROL;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.SUPPLEMENTAL_ARGUMENTS;
import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.THIRD_PARTY_MODULE_INSTALLATION;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilityStatus;
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilitySupport;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerFileCopy;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerPlan;
import com.mussonindustrial.testcontainers.ignition.internal.GatewayCredentials;
import com.mussonindustrial.testcontainers.ignition.internal.GeneratedContainerFile;
import com.mussonindustrial.testcontainers.ignition.internal.IgnitionContainerSpec;
import com.mussonindustrial.testcontainers.ignition.profiles.IgnitionProfile;
import com.mussonindustrial.testcontainers.ignition.profiles.IgnitionProfiles;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Testcontainers implementation for an Ignition Gateway.
 *
 * <p>User configuration is recorded in an {@link IgnitionContainerSpec} and
 * translated by the {@link IgnitionProfile} selected for the image version.
 *
 * <p>Unsupported capabilities are ignored with warnings. Capabilities used
 * before their documented version are applied with warnings.
 */
public class IgnitionContainer extends GenericContainer<IgnitionContainer> {

    /** Official Ignition Docker image name. */
    public static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("inductiveautomation/ignition");

    /** Parsed Ignition image version. */
    private final IgnitionVersion ignitionVersion;

    /** Compatibility profile selected for the image. */
    private final IgnitionProfile profile;

    /** Normalized user configuration. */
    private final IgnitionContainerSpec specification = new IgnitionContainerSpec();

    /**
     * Creates an Ignition container.
     *
     * @param dockerImageName concrete Ignition image name
     */
    public IgnitionContainer(String dockerImageName) {
        this(DockerImageName.parse(Objects.requireNonNull(dockerImageName, "dockerImageName")));
    }

    /**
     * Creates an Ignition container.
     *
     * @param dockerImageName concrete Ignition image name
     */
    public IgnitionContainer(DockerImageName dockerImageName) {
        super(dockerImageName);

        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        ignitionVersion = IgnitionVersion.from(dockerImageName);

        profile = IgnitionProfiles.resolve(ignitionVersion);

        setWaitStrategy(profile.createWaitStrategy(ignitionVersion));
    }

    /**
     * Accepts the Ignition license agreement.
     *
     * @return this container
     */
    public IgnitionContainer acceptLicense() {
        return use(LICENSE_ACCEPTANCE, IgnitionContainerSpec::acceptLicense);
    }

    /**
     * Sets the leased-license activation token.
     *
     * @param activationToken activation token
     * @return this container
     */
    public IgnitionContainer withActivationToken(String activationToken) {
        Objects.requireNonNull(activationToken, "activationToken");

        return use(LEASED_LICENSE_ACTIVATION, spec -> spec.activationToken(activationToken));
    }

    /**
     * Sets the initial Gateway administrator credentials.
     *
     * @param username administrator username
     * @param password administrator password
     * @return this container
     */
    public IgnitionContainer withCredentials(String username, String password) {
        Objects.requireNonNull(username, "username");

        Objects.requireNonNull(password, "password");

        return use(INITIAL_ADMIN_CONFIGURATION, spec -> spec.credentials(username, password));
    }

    /**
     * Enables or disables Gateway JVM debugging.
     *
     * @param debugMode debug state
     * @return this container
     */
    public IgnitionContainer withDebugMode(boolean debugMode) {
        return use(DEBUG_MODE, spec -> spec.debugMode(debugMode));
    }

    /**
     * Sets the Gateway edition.
     *
     * @param edition Gateway edition
     * @return this container
     */
    public IgnitionContainer withEdition(IgnitionGatewayEdition edition) {
        Objects.requireNonNull(edition, "edition");

        return use(GATEWAY_EDITION, spec -> spec.edition(edition));
    }

    /**
     * Exposes an additional container port.
     *
     * @param port container port
     * @return this container
     */
    public IgnitionContainer withAdditionalExposedPort(int port) {
        checkNotRunning();
        addExposedPort(port);

        return self();
    }

    /**
     * Restores a Gateway backup with disabled resources enabled.
     *
     * @param path Gateway backup path
     * @return this container
     * @throws FileNotFoundException if the backup does not exist
     */
    public IgnitionContainer withGatewayBackup(String path) throws FileNotFoundException {
        Objects.requireNonNull(path, "path");

        return withGatewayBackup(Path.of(path), false);
    }

    /**
     * Restores a Gateway backup.
     *
     * @param path Gateway backup path
     * @param restoreDisabled whether disabled resources remain disabled
     * @return this container
     * @throws FileNotFoundException if the backup does not exist
     */
    public IgnitionContainer withGatewayBackup(String path, boolean restoreDisabled) throws FileNotFoundException {
        Objects.requireNonNull(path, "path");

        return withGatewayBackup(Path.of(path), restoreDisabled);
    }

    /**
     * Restores a Gateway backup with disabled resources enabled.
     *
     * @param path Gateway backup path
     * @return this container
     * @throws FileNotFoundException if the backup does not exist
     */
    public IgnitionContainer withGatewayBackup(Path path) throws FileNotFoundException {
        return withGatewayBackup(path, false);
    }

    /**
     * Restores a Gateway backup.
     *
     * @param path Gateway backup path
     * @param restoreDisabled whether disabled resources remain disabled
     * @return this container
     * @throws FileNotFoundException if the backup does not exist
     */
    public IgnitionContainer withGatewayBackup(Path path, boolean restoreDisabled) throws FileNotFoundException {
        Objects.requireNonNull(path, "path");

        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException("gateway backup '%s' does not exist".formatted(path));
        }

        return use(GATEWAY_RESTORE, spec -> spec.gatewayRestore(path, restoreDisabled));
    }

    /**
     * Sets the Gateway name.
     *
     * @param name Gateway name
     * @return this container
     */
    public IgnitionContainer withGatewayName(String name) {
        Objects.requireNonNull(name, "name");

        return use(GATEWAY_NAME, spec -> spec.gatewayName(name));
    }

    /**
     * Sets the Ignition process group ID.
     *
     * @param gid process group ID
     * @return this container
     */
    public IgnitionContainer withGid(int gid) {
        return use(PROCESS_IDENTITY, spec -> spec.gid(gid));
    }

    /**
     * Sets the leased-license key.
     *
     * @param licenseKey license key
     * @return this container
     */
    public IgnitionContainer withLicenseKey(String licenseKey) {
        Objects.requireNonNull(licenseKey, "licenseKey");

        return use(LEASED_LICENSE_ACTIVATION, spec -> spec.licenseKey(licenseKey));
    }

    /**
     * Sets the maximum Gateway JVM memory.
     *
     * @param maxMemory maximum memory value
     * @return this container
     */
    public IgnitionContainer withMaxMemory(String maxMemory) {
        Objects.requireNonNull(maxMemory, "maxMemory");

        return use(MAX_MEMORY, spec -> spec.maxMemory(maxMemory));
    }

    /**
     * Selects the built-in modules to install.
     *
     * <p>An empty selection explicitly requests no built-in modules.
     *
     * @param modules selected modules
     * @return this container
     */
    public IgnitionContainer withModules(IgnitionModule... modules) {
        Objects.requireNonNull(modules, "modules");

        Set<IgnitionModule> requestedModules = new LinkedHashSet<>();

        for (IgnitionModule module : modules) {
            requestedModules.add(Objects.requireNonNull(module, "modules cannot contain null"));
        }

        Set<IgnitionModule> immutableModules = Collections.unmodifiableSet(requestedModules);

        return use(BUILT_IN_MODULE_SELECTION, spec -> spec.modules(immutableModules));
    }

    /**
     * Adds third-party module files.
     *
     * @param paths module file paths
     * @return this container
     * @throws FileNotFoundException if a module does not exist
     */
    public IgnitionContainer withThirdPartyModules(Path... paths) throws FileNotFoundException {
        Objects.requireNonNull(paths, "paths");

        Set<Path> modules = new LinkedHashSet<>();

        for (Path path : paths) {
            Objects.requireNonNull(path, "Module path cannot be null");

            if (!Files.isRegularFile(path)) {
                throw new FileNotFoundException("module '%s' does not exist".formatted(path));
            }

            modules.add(path);
        }

        Set<Path> immutableModules = Collections.unmodifiableSet(modules);

        return use(THIRD_PARTY_MODULE_INSTALLATION, spec -> spec.thirdPartyModules(immutableModules));
    }

    /**
     * Adds third-party module files.
     *
     * @param paths module file paths
     * @return this container
     * @throws FileNotFoundException if a module does not exist
     */
    public IgnitionContainer withThirdPartyModules(String... paths) throws FileNotFoundException {
        Objects.requireNonNull(paths, "paths");

        Path[] convertedPaths = Arrays.stream(paths)
                .map(path -> Path.of(Objects.requireNonNull(path, "Module path cannot be null")))
                .toArray(Path[]::new);

        return withThirdPartyModules(convertedPaths);
    }

    /**
     * Enables or disables Gateway Quick Start.
     *
     * @param quickStartEnabled Quick Start state
     * @return this container
     */
    public IgnitionContainer withQuickStart(boolean quickStartEnabled) {
        return use(QUICK_START_CONTROL, spec -> spec.quickStartEnabled(quickStartEnabled));
    }

    /**
     * Sets the container timezone.
     *
     * @param timezone timezone identifier
     * @return this container
     */
    public IgnitionContainer withTimezone(String timezone) {
        Objects.requireNonNull(timezone, "timezone");

        return configureSpecification(spec -> spec.timezone(timezone));
    }

    /**
     * Sets the Ignition process user ID.
     *
     * @param uid process user ID
     * @return this container
     */
    public IgnitionContainer withUid(int uid) {
        return use(PROCESS_IDENTITY, spec -> spec.uid(uid));
    }

    /**
     * Sets supplemental startup arguments.
     *
     * @param arguments startup arguments
     * @return this container
     */
    public IgnitionContainer withAdditionalArgs(String... arguments) {
        Objects.requireNonNull(arguments, "arguments");

        List<String> immutableArguments = List.copyOf(Arrays.asList(arguments));

        return use(SUPPLEMENTAL_ARGUMENTS, spec -> spec.additionalArguments(immutableArguments));
    }

    /**
     * Returns the parsed Ignition image version.
     *
     * @return image version
     */
    public IgnitionVersion getIgnitionVersion() {
        return ignitionVersion;
    }

    /**
     * Returns the selected compatibility profile.
     *
     * @return compatibility profile
     */
    public IgnitionProfile getProfile() {
        return profile;
    }

    /**
     * Returns the configured administrator username.
     *
     * @return username, or {@code null} when unset
     */
    public String getUsername() {
        GatewayCredentials credentials = specification.credentials();

        return credentials == null ? null : credentials.username();
    }

    /**
     * Returns the configured administrator password.
     *
     * @return password, or {@code null} when unset
     */
    public String getPassword() {
        GatewayCredentials credentials = specification.credentials();

        return credentials == null ? null : credentials.password();
    }

    /**
     * Returns the mapped Gateway HTTP port.
     *
     * @return mapped port
     */
    public int getMappedGatewayPort() {
        return getMappedEndpointPort(IgnitionEndpoint.GATEWAY_HTTP);
    }

    /**
     * Returns the mapped Gateway HTTPS port.
     *
     * @return mapped port
     */
    public int getMappedGatewaySslPort() {
        return getMappedEndpointPort(IgnitionEndpoint.GATEWAY_HTTPS);
    }

    /**
     * Returns the mapped Gateway Network port.
     *
     * @return mapped port
     */
    public int getMappedGatewayGanPort() {
        return getMappedEndpointPort(IgnitionEndpoint.GATEWAY_NETWORK);
    }

    /**
     * Returns the mapped debugger port.
     *
     * @return mapped port
     */
    public int getMappedDebugPort() {
        return getMappedEndpointPort(IgnitionEndpoint.DEBUG);
    }

    /**
     * Returns the mapped OPC UA port.
     *
     * @return mapped port
     */
    public int getMappedOpcUaPort() {
        return getMappedEndpointPort(IgnitionEndpoint.OPC_UA);
    }

    /**
     * Returns the Gateway HTTP URL.
     *
     * @return Gateway URL
     */
    public String getGatewayUrl() {
        return getGatewayUrl(false);
    }

    /**
     * Returns the Gateway URL.
     *
     * @param ssl whether to use HTTPS
     * @return Gateway URL
     */
    public String getGatewayUrl(boolean ssl) {
        String scheme = ssl ? "https" : "http";

        int port = ssl ? getMappedGatewaySslPort() : getMappedGatewayPort();

        return "%s://%s:%d".formatted(scheme, getHost(), port);
    }

    /**
     * Returns the OPC UA server URL.
     *
     * @return OPC UA URL
     */
    public String getOpcUaUrl() {
        return "opc.tcp://%s:%d".formatted(getHost(), getMappedOpcUaPort());
    }

    /**
     * Returns the OPC UA discovery URL.
     *
     * @return discovery URL
     */
    public String getOpcUaDiscoveryUrl() {
        return getOpcUaUrl() + "/discovery";
    }

    /**
     * Returns the mapped port for a logical endpoint.
     *
     * @param endpoint logical endpoint
     * @return mapped port
     * @throws IllegalStateException if the endpoint is not exposed
     */
    public int getMappedEndpointPort(IgnitionEndpoint endpoint) {
        Objects.requireNonNull(endpoint, "endpoint");

        int containerPort = profile.endpoints().port(endpoint);

        if (!getExposedPorts().contains(containerPort)) {
            throw new IllegalStateException(
                    "Ignition endpoint %s is not exposed for the current container configuration".formatted(endpoint));
        }

        return getMappedPort(containerPort);
    }

    /**
     * Returns the normalized container specification.
     *
     * @return container specification
     */
    protected IgnitionContainerSpec specification() {
        return specification;
    }

    /** Builds and applies the profile-generated container configuration. */
    @Override
    protected void configure() {
        super.configure();

        specification.validate();

        ContainerPlan.Builder plan = ContainerPlan.builder();

        profile.applyDefaults(ignitionVersion, specification, plan);

        for (IgnitionCapability capability : specification.requestedCapabilities()) {
            applyRequestedCapability(capability, plan);
        }

        applyPlan(plan.build());
    }

    /** Logs that the Ignition container is starting. */
    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        logger().debug("Ignition {} container is starting using profile '{}'", ignitionVersion, profile.name());
    }

    /** Logs that the Ignition container is ready. */
    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        logger().info("Ignition {} container is ready. Gateway Web UI: {}", ignitionVersion, getGatewayUrl());
    }

    /**
     * Records a capability and its requested configuration.
     */
    private IgnitionContainer use(IgnitionCapability capability, Consumer<IgnitionContainerSpec> configuration) {
        Objects.requireNonNull(capability, "capability");

        Objects.requireNonNull(configuration, "configuration");

        return configureSpecification(spec -> {
            configuration.accept(spec);
            spec.request(capability);
        });
    }

    /**
     * Records profile-independent configuration.
     */
    private IgnitionContainer configureSpecification(Consumer<IgnitionContainerSpec> configuration) {
        checkNotRunning();

        Objects.requireNonNull(configuration, "configuration").accept(specification);

        return self();
    }

    /**
     * Applies a requested capability using the compatibility policy.
     */
    private void applyRequestedCapability(IgnitionCapability capability, ContainerPlan.Builder plan) {
        CapabilitySupport support = profile.supportFor(capability);

        if (support.status() == CapabilityStatus.UNSUPPORTED) {
            logger().warn(
                            "Ignition {} profile '{}' does not support capability {}: {}. The requested configuration will be ignored.",
                            ignitionVersion,
                            profile.name(),
                            capability,
                            support.reason());

            return;
        }

        if (!support.isAvailableIn(ignitionVersion)) {
            logger().warn(
                            "Capability {} is documented for Ignition {} or newer, but image {} was requested. The '{}' profile translation will still be applied.",
                            capability,
                            support.since(),
                            ignitionVersion,
                            profile.name());
        }

        boolean applied = profile.apply(capability, ignitionVersion, specification, plan);

        if (!applied) {
            logger().warn(
                            "Ignition profile '{}' declares capability {} but has no applier. The requested configuration will be ignored.",
                            profile.name(),
                            capability);
        }
    }

    /**
     * Applies a normalized plan to Testcontainers.
     *
     * <p>Raw environment and command overrides take precedence.
     */
    private void applyPlan(ContainerPlan plan) {
        plan.warnings().forEach(warning -> logger().warn(warning));

        applyEnvironment(plan);
        applyCommand(plan);
        applyExposedPorts(plan);
        applyFileCopies(plan);
        applyGeneratedFiles(plan);
    }

    /**
     * Applies profile-generated environment variables.
     */
    private void applyEnvironment(ContainerPlan plan) {
        Set<String> existingEnvironment = Set.copyOf(getEnvMap().keySet());

        for (Map.Entry<String, String> entry : plan.environment().entrySet()) {
            if (existingEnvironment.contains(entry.getKey())) {
                logger().debug("Retaining raw environment override for {}", entry.getKey());

                continue;
            }

            addEnv(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Applies the profile-generated container command.
     */
    private void applyCommand(ContainerPlan plan) {
        if (plan.command().isEmpty()) {
            return;
        }

        String[] existingCommand = getCommandParts();

        if (existingCommand != null && existingCommand.length > 0) {
            logger().debug("Retaining raw container command override; profile-generated command was not applied");

            return;
        }

        setCommand(plan.command().toArray(String[]::new));
    }

    /**
     * Applies profile-generated exposed ports.
     */
    private void applyExposedPorts(ContainerPlan plan) {
        plan.exposedPorts().forEach(this::addExposedPort);
    }

    /**
     * Applies profile-generated host file copies.
     */
    private void applyFileCopies(ContainerPlan plan) {
        for (ContainerFileCopy copy : plan.fileCopies()) {
            MountableFile source = MountableFile.forHostPath(copy.source(), copy.mode());

            withCopyToContainer(source, copy.destination());
        }
    }

    /**
     * Applies profile-generated in-memory files.
     */
    private void applyGeneratedFiles(ContainerPlan plan) {
        for (GeneratedContainerFile file : plan.generatedFiles()) {
            Transferable contents = Transferable.of(file.contents(), file.mode());

            withCopyToContainer(contents, file.destination());
        }
    }

    /**
     * Ensures that the container has not started.
     */
    private void checkNotRunning() {
        if (isRunning()) {
            throw new IllegalStateException("Configuration methods can only be called before the container is running");
        }
    }
}
