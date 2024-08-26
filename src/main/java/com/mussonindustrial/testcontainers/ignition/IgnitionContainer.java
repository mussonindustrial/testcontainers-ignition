package com.mussonindustrial.testcontainers.ignition;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Testcontainers implementation for Ignition.
 *
 * <p> Supported image: {@code inductiveautomation/ignition}
 * <p> Exposed ports:
 *
 * <ul>
 * <li>Gateway: 8080
 * <li>Gateway (SSL): 8043
 * <li>GAN: 8060
 * <li>JVM Debugger: 8000
 * </ul>
 */
public class IgnitionContainer extends GenericContainer<IgnitionContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("inductiveautomation/ignition");
    private static final String DEFAULT_TAG = "8.1.33";
    private static final Integer GATEWAY_PORT = 8088;
    private static final Integer GATEWAY_SSL_PORT = 8043;
    private static final Integer GAN_PORT = 8060;
    private static final Integer OPCUA_PORT = 8000;
    private static final Integer DEBUG_PORT = 8000;
    private static final String INSTALL_DIR = "/usr/local/bin/ignition";

    private String username = "admin";

    private String password = "password";

    private Integer uid;

    private Integer gid;

    private String name = "testcontainers-ignition";

    private GatewayEdition edition = GatewayEdition.STANDARD;

    private String timezone = "Etc/UTC";

    private String maxMemory;

    private final Set<Module> modules = new HashSet<>();

    private final Set<Path> thirdPartyModules = new HashSet<>();

    private boolean quickStartEnabled = false;

    private Boolean debugMode = false;

    private Path gatewayBackup;

    private boolean restoreDisabled = false;

    private String activationToken;

    private String licenseKey;

    /**
     * Creates a new Ignition container with the default image and version.
     *
     * @deprecated use {@link #IgnitionContainer(DockerImageName)} instead
     */
    @Deprecated
    public IgnitionContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    /**
     * Creates a new Ignition container with the specified image name.
     *
     * @param dockerImageName the image name that should be used.
     */
    public IgnitionContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * Create a new Ignition container with the specified image name.
     *
     * @param dockerImageName the image name that should be used.
     */
    public IgnitionContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        this.waitStrategy = Wait.forHealthcheck();
    }

    /**
     * Set an activation token for the gateway.
     *
     * @param token the activation token to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withActivationToken(String token) {
        checkNotRunning();
        this.activationToken = token;
        return self();
    }

    /**
     * Set custom username and password for the admin user.
     *
     * @param username the admin username to use.
     * @param password the password for the admin user.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withCredentials(final String username, final String password) {
        checkNotRunning();
        this.username = username;
        this.password = password;
        return self();
    }

    /**
     * Set debug mode.
     *
     * @param debugMode the debug mode setting to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withDebugMode(boolean debugMode) {
        checkNotRunning();
        this.debugMode = debugMode;
        return self();
    }

    /**
     * Set the gateway edition.
     *
     * @param edition the Ignition version to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withEdition(GatewayEdition edition) {
        checkNotRunning();
        this.edition = edition;
        return self();
    }

    /**
     * Set a gateway backup file (*.gwbk) to restore from.
     * Restores into the enabled state.
     *
     * @param path the path to the gateway backup file.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if the gateway backup does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withGatewayBackup(Path path) throws FileNotFoundException {
        return this.withGatewayBackup(path, false);
    }

    /**
     * Set a gateway backup file (*.gwbk) to restore from.
     * Restores into the enabled state.
     *
     * @param path the path to the gateway backup file.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if the gateway backup does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withGatewayBackup(String path) throws FileNotFoundException {
        return this.withGatewayBackup(path, false);
    }

    /**
     * Set a gateway backup file (*.gwbk) to restore from.
     *
     * @param path the path to the gateway backup file.
     * @param restoreDisabled true to restore to a disabled state.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if the gateway backup does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withGatewayBackup(Path path, boolean restoreDisabled) throws FileNotFoundException {
        checkNotRunning();

        if (!path.toFile().exists()) {
            throw new FileNotFoundException(String.format("gateway backup '%s' does not exist", path));
        }

        this.gatewayBackup = path;
        this.restoreDisabled = restoreDisabled;
        return self();
    }

    /**
     * Set a gateway backup file (*.gwbk) to restore from.
     *
     * @param path the path to the gateway backup file.
     * @param restoreDisabled true to restore to a disabled state.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if the gateway backup does not exist.
     */
    public IgnitionContainer withGatewayBackup(String path, boolean restoreDisabled) throws FileNotFoundException {
        return this.withGatewayBackup(Path.of(path), restoreDisabled);
    }

    /**
     * Set the gateway name.
     *
     * @param name the gateway name to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withGatewayName(String name) {
        checkNotRunning();
        this.name = name;
        return self();
    }

    /**
     * Set the GID of the process running the Ignition gateway.
     *
     * @param gid the GID to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withGid(int gid) {
        checkNotRunning();
        this.gid = gid;
        return self();
    }

    /**
     * Set a license key for the gateway.
     *
     * @param key the license key to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withLicenseKey(String key) {
        checkNotRunning();
        this.licenseKey = key;
        return self();
    }

    /**
     * Set the maximum memory usage of the gateway.
     *
     * @param maxMemory the maximum memory to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withMaxMemory(String maxMemory) {
        checkNotRunning();
        this.maxMemory = maxMemory;
        return self();
    }

    /**
     * Include modules when initializing the gateway.
     *
     * @param modules the modules to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withModule(Module... modules) {
        checkNotRunning();
        this.modules.addAll(List.of(modules));
        return self();
    }

    /**
     * Include third party modules when initializing the gateway.
     *
     * @param paths the paths to the module files to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if a module path does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withThirdPartyModule(Path... paths) throws FileNotFoundException {
        checkNotRunning();

        for (Path path : paths) {
            if (!path.toFile().exists()) {
                throw new FileNotFoundException(String.format("module '%s' does not exist", path));
            }
            this.thirdPartyModules.add(path);
        }

        return self();
    }

    /**
     * Include third party modules when initializing the gateway.
     *
     * @param paths the paths to the module files to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if a module path does not exist.
     */
    public IgnitionContainer withThirdPartyModule(String... paths) throws FileNotFoundException {
        return this.withThirdPartyModule(Arrays.stream(paths).map(Path::of).toArray(Path[]::new));
    }

    /**
     * Set quick start mode.
     *
     * @param quickStartEnabled the quickstart mode setting to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withQuickStart(boolean quickStartEnabled) {
        checkNotRunning();
        this.quickStartEnabled = quickStartEnabled;
        return self();
    }

    /**
     * Set gateway timezone.
     *
     * @param timezone the gateway timezone to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withTimezone(String timezone) {
        checkNotRunning();
        this.timezone = timezone;
        return self();
    }

    /**
     * Set the UID of the process running the Ignition gateway.
     *
     * @param uid the UID to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withUid(int uid) {
        checkNotRunning();
        this.uid = uid;
        return self();
    }

    /**
     * Get the gateway admin username.
     *
     * @return the gateway admin username.
     */
    @SuppressWarnings("unused")
    public String getUsername() {
        return username;
    }

    /**
     * Get the gateway admin password.
     *
     * @return the gateway admin password.
     */
    @SuppressWarnings("unused")
    public String getPassword() {
        return password;
    }

    /**
     * Get the mapped gateway HTTP port.
     *
     * @return the mapped gateway HTTP port.
     */
    @SuppressWarnings("unused")
    public int getMappedGatewayPort() {
        return getMappedPort(GATEWAY_PORT);
    }

    /**
     * Get the mapped gateway HTTPS port.
     *
     * @return the mapped gateway HTTPS port.
     */
    @SuppressWarnings("unused")
    public int getMappedGatewaySSLPort() {
        return getMappedPort(GATEWAY_SSL_PORT);
    }

    /**
     * Get the mapped gateway GAN (gateway area network) port.
     *
     * @return the mapped gateway GAN port.
     */
    @SuppressWarnings("unused")
    public int getMappedGatewayGanPort() {
        return getMappedPort(GAN_PORT);
    }

    /**
     * Get the mapped remote JVM debugging port.
     *
     * @return the mapped remote JVM debugging port.
     */
    @SuppressWarnings("unused")
    public int getMappedDebugPort() {
        return getMappedPort(DEBUG_PORT);
    }

    /**
     * Get the mapped OPC-UA server port.
     *
     * @return the mapped OPC-USA server port.
     */
    @SuppressWarnings("unused")
    public int getMappedOpcUaPort() {
        return getMappedPort(OPCUA_PORT);
    }

    /**
     * Get the URL of the gateway web interface (using HTTP).
     *
     * @return the URL of the gateway web interface.
     */
    @SuppressWarnings("unused")
    public String getGatewayUrl() {
        return String.format("http://%s:%d", getHost(), getMappedGatewayPort());
    }

    /**
     * Get the URL of the gateway web interface.
     *
     * @param useHttps use HTTPS when `true`
     * @return the URL of the gateway web interface.
     */
    @SuppressWarnings("unused")
    public String getGatewayUrl(boolean useHttps) {
        String mode = useHttps ? "https" : "http";
        Integer port = useHttps ? getMappedGatewaySSLPort() : getMappedGatewayPort();
        return String.format("%s://%s:%d", mode, getHost(), port);
    }

    /**
     * Checks if already running and if so raises an exception to prevent too-late
     * setters.
     */
    private void checkNotRunning() {
        if (isRunning()) {
            throw new IllegalStateException("Setter can only be called before the container is running");
        }
    }

    @Override
    protected void configure() {
        super.configure();

        this.withEnv(getEnvironmentVariables());

        exposePorts();

        mapGatewayBackup();
        mapThirdPartyModules();

        if (debugMode) this.withCommand("-d");
        if (maxMemory != null) this.withCommand("-m", maxMemory);
        if (name != null) this.withCommand("-n", name);
        if (gatewayBackup != null) this.withCommand("-r", "/restore.gwbk");
    }

    private void exposePorts() {
        addExposedPorts(GATEWAY_PORT, GATEWAY_SSL_PORT);

        if (modules.contains(Module.OPC_UA)) {
            addExposedPorts(OPCUA_PORT);
        }

        if (debugMode) {
            addExposedPorts(DEBUG_PORT);
        }
    }

    private void mapGatewayBackup() {
        if (gatewayBackup != null) {
            this.withCopyFileToContainer(MountableFile.forHostPath(gatewayBackup.toString()), "/restore.gwbk");
        }
    }

    private void mapThirdPartyModules() {
        for (Path path : thirdPartyModules) {
            MountableFile file = MountableFile.forHostPath(path);
            String containerPath = Path.of(
                            INSTALL_DIR, "user-lib", "modules", path.toFile().getName())
                    .toString();
            this.withCopyFileToContainer(file, containerPath);
        }
    }

    private Map<String, String> getEnvironmentVariables() {
        Map<String, String> map = new HashMap<>();
        map.put("ACCEPT_IGNITION_EULA", "Y");
        map.put("DISABLE_QUICKSTART", String.valueOf(!quickStartEnabled));

        map.put("GATEWAY_ADMIN_USERNAME", username);
        map.put("GATEWAY_ADMIN_PASSWORD", password);

        map.put("GATEWAY_GAN_PORT", String.valueOf(GAN_PORT));
        map.put("GATEWAY_HTTP_PORT", String.valueOf(GATEWAY_PORT));
        map.put("GATEWAY_HTTPS_PORT", String.valueOf(GATEWAY_SSL_PORT));

        if (gatewayBackup != null) map.put("GATEWAY_RESTORE_DISABLED", String.valueOf(restoreDisabled));
        map.put(
                "GATEWAY_MODULES_ENABLED",
                modules.stream().map(Objects::toString).collect(Collectors.joining(",")));

        map.put("IGNITION_EDITION", edition.toString());
        if (gid != null) map.put("IGNITION_GID", gid.toString());
        if (uid != null) map.put("IGNITION_UID", uid.toString());

        if (activationToken != null) map.put("IGNITION_ACTIVATION_TOKEN", activationToken);
        if (licenseKey != null) map.put("IGNITION_LICENSE_KEY", licenseKey);

        map.put("TZ", timezone);

        return map;
    }

    @Override
    protected void containerIsStarting(final InspectContainerResponse containerInfo) {
        logger().debug("Ignition container is starting, performing configuration.");
    }

    @Override
    protected void containerIsStarted(final InspectContainerResponse containerInfo) {
        logger().info("Ignition container is ready! Gateway Web UI is available at: {}", getGatewayUrl());
    }
}
