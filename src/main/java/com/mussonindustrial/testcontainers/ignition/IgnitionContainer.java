package com.mussonindustrial.testcontainers.ignition;

import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.*;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.mussonindustrial.testcontainers.ignition.compatibility.*;
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
import java.util.*;
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
@SuppressWarnings("UnusedReturnValue")
public class IgnitionContainer extends GenericContainer<IgnitionContainer> {

    /** Official Ignition Docker image name. */
    public static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("inductiveautomation/ignition");

    /** Compatibility profile selected for the image. */
    private final IgnitionProfile profile;

    /** Parsed Ignition image version. */
    private final IgnitionVersion ignitionVersion;

    /** Normalized user configuration. */
    private final IgnitionContainerSpec specification = new IgnitionContainerSpec();

    /**
     * Creates an Ignition container.
     *
     * @param dockerImageName concrete Ignition image name
     */
    @SuppressWarnings("unused")
    public IgnitionContainer(String dockerImageName) {
        this(DockerImageName.parse(Objects.requireNonNull(dockerImageName, "dockerImageName")));
    }

    /**
     * Creates an Ignition container.
     *
     * @param dockerImageName concrete Ignition image name
     */
    public IgnitionContainer(DockerImageName dockerImageName) {
        this(
                dockerImageName,
                IgnitionProfiles.resolve(IgnitionVersion.from(dockerImageName)),
                IgnitionVersion.from(dockerImageName));
    }

    /**
     * Creates an Ignition container.
     *
     * @param dockerImageName concrete Ignition image name
     * @param profile compatibility profile
     * @param ignitionVersion Ignition version
     */
    public IgnitionContainer(
            DockerImageName dockerImageName, IgnitionProfile profile, IgnitionVersion ignitionVersion) {
        super(dockerImageName);

        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        this.ignitionVersion = Objects.requireNonNull(ignitionVersion, "profile");
        this.profile = Objects.requireNonNull(profile, "profile");

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
     * Enables Gateway JVM debugging.
     *
     * @return this container
     */
    public IgnitionContainer withDebugMode() {
        return use(DEBUG_MODE, spec -> spec.debugMode(true));
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
     * Enables unsigned third-party modules.
     *
     * @return this container
     */
    public IgnitionContainer withAllowUnsignedModules() {
        return use(UNSIGNED_MODULES, spec -> spec.allowUnsignedModules(true));
    }

    /**
     * Enables or disables unsigned third-party modules.
     *
     * @param allowUnsignedModules unsigned third-party modules state
     * @return this container
     */
    public IgnitionContainer withAllowUnsignedModules(boolean allowUnsignedModules) {
        return use(UNSIGNED_MODULES, spec -> spec.allowUnsignedModules(allowUnsignedModules));
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
     * Sets the Ignition process group ID and user ID.
     *
     * @param gid process group ID
     * @param uid process user ID
     * @return this container
     */
    public IgnitionContainer withProcessIdentity(int gid, int uid) {
        return use(PROCESS_IDENTITY, spec -> {
            spec.gid(gid);
            spec.uid(uid);
        });
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
     * Adds a third-party module.
     *
     * <p>The module descriptor is read immediately.
     *
     * @param path module archive
     * @return this container
     * @throws FileNotFoundException if the module cannot be read
     */
    public IgnitionContainer withThirdPartyModule(Path path) throws FileNotFoundException {
        Objects.requireNonNull(path, "path");

        ThirdPartyModule module = ThirdPartyModule.read(path);

        return use(
                Set.of(THIRD_PARTY_MODULE_INSTALLATION, BUILT_IN_MODULE_SELECTION),
                spec -> spec.addThirdPartyModule(module));
    }

    /**
     * Adds a third-party module.
     *
     * @param path module archive
     * @return this container
     * @throws FileNotFoundException if the module cannot be read
     */
    public IgnitionContainer withThirdPartyModule(String path) throws FileNotFoundException {
        Objects.requireNonNull(path, "path");

        return withThirdPartyModule(Path.of(path));
    }

    /**
     * Replaces the configured third-party modules.
     *
     * <p>Module descriptors are read immediately. The existing selection is
     * unchanged if any archive cannot be read.
     *
     * @param paths module archives
     * @return this container
     * @throws FileNotFoundException if a module archive does not exist
     * @throws IllegalArgumentException if a module archive is invalid
     */
    public IgnitionContainer withThirdPartyModules(Path... paths) throws FileNotFoundException {
        Objects.requireNonNull(paths, "paths");

        List<ThirdPartyModule> modules = new ArrayList<>(paths.length);

        for (Path path : paths) {
            modules.add(ThirdPartyModule.read(Objects.requireNonNull(path, "paths cannot contain null")));
        }

        return use(
                Set.of(THIRD_PARTY_MODULE_INSTALLATION, BUILT_IN_MODULE_SELECTION),
                spec -> spec.thirdPartyModules(modules));
    }

    /**
     * Replaces the configured third-party modules.
     *
     * @param paths module archive paths
     * @return this container
     * @throws FileNotFoundException if a module cannot be read
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

        ContainerPlan.Builder planBuilder = ContainerPlan.builder();

        profile.applyDefaults(ignitionVersion, specification, planBuilder);
        applyRequestedCapabilities(planBuilder);

        ContainerPlan plan = planBuilder.build();
        applyPlan(plan);
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

        return use(Set.of(capability), configuration);
    }

    /**
     * Records capabilities and their requested configuration.
     */
    private IgnitionContainer use(Set<IgnitionCapability> capabilities, Consumer<IgnitionContainerSpec> configuration) {
        Objects.requireNonNull(capabilities, "capabilities");
        Objects.requireNonNull(configuration, "configuration");

        if (capabilities.isEmpty()) {
            throw new IllegalArgumentException("At least one capability is required");
        }

        Set<IgnitionCapability> requestedCapabilities = Set.copyOf(capabilities);

        return configureSpecification(spec -> {
            configuration.accept(spec);

            for (IgnitionCapability capability : requestedCapabilities) {
                spec.request(Objects.requireNonNull(capability, "capabilities cannot contain null"));
            }
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
     * Applies all capabilities requested by the container specification.
     *
     * <p>Capabilities reached through delegation are applied at most once, even
     * when they are also requested directly. Circular capability delegation is
     * rejected.
     *
     * @param plan container plan being configured
     */
    private void applyRequestedCapabilities(ContainerPlan.Builder plan) {
        Set<IgnitionCapability> applied = EnumSet.noneOf(IgnitionCapability.class);

        Set<IgnitionCapability> resolving = EnumSet.noneOf(IgnitionCapability.class);

        for (IgnitionCapability capability : specification.requestedCapabilities()) {
            applyRequestedCapability(capability, plan, applied, resolving);
        }
    }

    /**
     * Applies one capability using the active profile's compatibility policy.
     *
     * <p>A supported capability may be implemented directly by an applier or
     * delegated to another capability. Unsupported capabilities are ignored
     * after a warning is logged.
     *
     * @param capability capability to apply
     * @param plan container plan being configured
     * @param applied capabilities already applied to the plan
     * @param resolving capabilities in the current delegation chain
     * @throws IllegalStateException if capability delegation contains a cycle
     */
    private void applyRequestedCapability(
            IgnitionCapability capability,
            ContainerPlan.Builder plan,
            Set<IgnitionCapability> applied,
            Set<IgnitionCapability> resolving) {
        if (applied.contains(capability)) {
            return;
        }

        if (!resolving.add(capability)) {
            throw new IllegalStateException("Circular capability implementation detected while applying %s: %s"
                    .formatted(capability, resolving));
        }

        try {
            CapabilityCatalog.Resolution resolution = profile.capabilities().resolve(capability, ignitionVersion);

            if (resolution instanceof CapabilityCatalog.UnsupportedResolution unsupported) {
                logger().warn(
                                "Ignition {} profile '{}' does not support capability {}: {}. "
                                        + "The requested configuration will be ignored.",
                                ignitionVersion,
                                profile.name(),
                                capability,
                                unsupported.reason());

                applied.add(capability);
                return;
            }

            if (resolution instanceof CapabilityCatalog.SupportedResolution supported) {
                warnIfUnavailable(capability, supported);

                applyImplementation(supported.implementation(), plan, applied, resolving);

                applied.add(capability);
                return;
            }

            throw new IllegalStateException("Unknown capability resolution type: "
                    + resolution.getClass().getName());
        } finally {
            resolving.remove(capability);
        }
    }

    /**
     * Applies a resolved capability implementation.
     *
     * <p>Direct implementations invoke their capability applier. Delegated
     * implementations recursively apply the referenced capability using the same
     * deduplication and cycle-detection state.
     *
     * @param implementation resolved implementation
     * @param plan container plan being configured
     * @param applied capabilities already applied to the plan
     * @param resolving capabilities in the current delegation chain
     */
    private void applyImplementation(
            CapabilityCatalog.Implementation implementation,
            ContainerPlan.Builder plan,
            Set<IgnitionCapability> applied,
            Set<IgnitionCapability> resolving) {
        if (implementation instanceof CapabilityCatalog.DirectImplementation direct) {
            direct.applier().apply(ignitionVersion, specification, plan);

            return;
        }

        if (implementation instanceof CapabilityCatalog.DelegatedImplementation delegated) {
            applyRequestedCapability(delegated.capability(), plan, applied, resolving);

            return;
        }

        throw new IllegalStateException("Unknown capability implementation type: "
                + implementation.getClass().getName());
    }

    /**
     * Logs a warning when a capability predates its documented introduction.
     *
     * <p>The earliest known profile implementation is still applied so older
     * image versions can be used on a best-effort basis.
     *
     * @param capability capability being applied
     * @param resolution resolved supported capability
     */
    private void warnIfUnavailable(IgnitionCapability capability, CapabilityCatalog.SupportedResolution resolution) {
        if (resolution.available()) {
            return;
        }

        logger().warn(
                        "Capability {} is documented for Ignition {} or newer, "
                                + "but image {} was requested. The '{}' profile "
                                + "translation will still be applied.",
                        capability,
                        resolution.introducedIn(),
                        ignitionVersion,
                        profile.name());
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
        if (plan.arguments().isEmpty()) {
            return;
        }

        String[] existingCommand = getCommandParts();

        if (existingCommand != null && existingCommand.length > 0) {
            logger().debug("Retaining raw container command override; profile-generated command was not applied");

            return;
        }

        setCommand(plan.arguments().toArray(String[]::new));
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
