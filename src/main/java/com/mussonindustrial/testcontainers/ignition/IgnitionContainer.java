package com.mussonindustrial.testcontainers.ignition;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Testcontainers implementation for Ignition.
 *
 * <p>
 * Supported image: {@code inductiveautomation/ignition}
 * <p>
 * Exposed ports:
 * <ul>
 *      <li>Gateway: 8088</li>
 *      <li>Gateway (SSL): 8043</li>
 *      <li>Gateway Area Network: 8060</li>
 *      <li>OPC UA Server (if OPC UA module is enabled): 62541</li>
 *      <li>JVM Debugger (if debug mode is enabled): 8000</li>
 * </ul>
 */
public class IgnitionContainer extends GenericContainer<IgnitionContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("inductiveautomation/ignition");

    private static final String DEFAULT_TAG = "8.3.8";

    private static final Integer GATEWAY_PORT = 8088;

    private static final Integer GATEWAY_SSL_PORT = 8043;

    private static final Integer GAN_PORT = 8060;

    private static final Integer OPCUA_PORT = 62541;

    private static final Integer DEBUG_PORT = 8000;

    private static final String INSTALL_DIR = "/usr/local/bin/ignition";

    private String username;

    private String password;

    private Integer uid;

    private Integer gid;

    private String name;

    private GatewayEdition edition = GatewayEdition.STANDARD;

    private String timezone = "Etc/UTC";

    private String maxMemory;

    private final Set<IgnitionModule> modules = new LinkedHashSet<>(Set.of(GatewayModule.OPC_UA));

    private final Map<Path, ThirdPartyModuleDescriptor> thirdPartyModules = new LinkedHashMap<>();

    private boolean licenseAccepted = false;

    private boolean quickStartEnabled = false;

    private Boolean debugMode = false;

    private boolean developerMode = false;

    private Path gatewayBackup;

    private boolean restoreDisabled = false;

    private String activationToken;

    private String licenseKey;

    private List<String> additionalArgs;

    /**
     * Creates a new Ignition container with the default image and version.
     */
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

        this.waitStrategy = Wait.forHttp("/StatusPing")
                .forPort(GATEWAY_PORT)
                .forResponsePredicate("{\"state\":\"RUNNING\"}"::equals)
                .withStartupTimeout(Duration.ofMinutes(5));
    }

    /**
     * Automatically accept the Ignition EULA.
     * @see <a href="https://inductiveautomation.com/ignition/license">https://inductiveautomation.com/ignition/license</a>
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer acceptLicense() {
        checkNotRunning();
        this.licenseAccepted = true;
        return self();
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
     * Enable debug mode.
     *
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withDebugMode() {
        checkNotRunning();
        this.debugMode = true;
        return self();
    }

    /**
     * Enable or disable debug mode.
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
     * Enable developer mode so the gateway can load unsigned modules.
     *
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withDeveloperMode() {
        return withDeveloperMode(true);
    }

    /**
     * Enable or disable developer mode.
     *
     * @param developerMode whether unsigned modules should be allowed.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withDeveloperMode(boolean developerMode) {
        checkNotRunning();
        this.developerMode = developerMode;
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
     * Add an exposed port to the container.
     *
     * <p>After the container is started, the mapped port can be retrieved using the
     * {@link #getMappedPort(int)} method.
     *
     * @param port the port to expose.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withAdditionalExposedPort(int port) {
        checkNotRunning();
        this.addExposedPort(port);
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
    public IgnitionContainer withGatewayBackup(String path, boolean restoreDisabled) throws FileNotFoundException {
        return this.withGatewayBackup(Path.of(path), restoreDisabled);
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
     *
     * @param path the path to the gateway backup file.
     * @param restoreDisabled true to restore to a disabled state.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if the gateway backup does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withGatewayBackup(Path path, boolean restoreDisabled) throws FileNotFoundException {
        checkNotRunning();

        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException(String.format("gateway backup '%s' does not exist", path));
        }

        this.gatewayBackup = path.toAbsolutePath().normalize();
        this.restoreDisabled = restoreDisabled;
        return self();
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
    public IgnitionContainer withModules(IgnitionModule... modules) {
        checkNotRunning();
        this.modules.clear();
        this.modules.addAll(List.of(modules));
        return self();
    }

    /**
     * Include a third party module when initializing the gateway. The module identifier and
     * gateway-scoped dependencies are read from its {@code module.xml} descriptor.
     *
     * @param path the path to the module file to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if the module path does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withThirdPartyModule(Path path) throws FileNotFoundException {
        checkNotRunning();
        addThirdPartyModule(path);
        return self();
    }

    /**
     * Include a third party module when initializing the gateway.
     *
     * @param path the path to the module file to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if the module path does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withThirdPartyModule(String path) throws FileNotFoundException {
        return withThirdPartyModule(Path.of(path));
    }

    /**
     * Include third party modules when initializing the gateway.
     *
     * @param paths the paths to the module files to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     * @throws FileNotFoundException if a module path does not exist.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withThirdPartyModules(Path... paths) throws FileNotFoundException {
        checkNotRunning();
        this.thirdPartyModules.clear();

        for (Path path : paths) {
            addThirdPartyModule(path);
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
    public IgnitionContainer withThirdPartyModules(String... paths) throws FileNotFoundException {
        return this.withThirdPartyModules(Arrays.stream(paths).map(Path::of).toArray(Path[]::new));
    }

    /**
     * Enable or disable quick start mode.
     *
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withQuickStart() {
        checkNotRunning();
        this.quickStartEnabled = true;
        return self();
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
     * Set supplemental JVM/Wrapper/Gateway arguments.
     * @param additionalArgs one or more additional arguments.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    public IgnitionContainer withAdditionalArgs(String... additionalArgs) {
        checkNotRunning();
        this.additionalArgs = List.of(additionalArgs);
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
    public int getMappedGatewaySslPort() {
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
     * Get the gateway's OPC UA URL.
     * Only valid if {@link GatewayModule#OPC_UA} is enabled.
     *
     * @return the gateway's OPC UA URL.
     */
    @SuppressWarnings("unused")
    public String getOpcUaUrl() {
        return String.format("opc.tcp://%s:%d", getHost(), getMappedOpcUaPort());
    }

    /**
     * Get the gateway's OPC UA Discovery URL.
     * Only valid if {@link GatewayModule#OPC_UA} is enabled.
     *
     * @return the gateway's OPC UA Discovery URL.
     */
    @SuppressWarnings("unused")
    public String getOpcUaDiscoveryUrl() {
        return String.format("%s/discovery", getOpcUaUrl());
    }

    /**
     * Get the URL of the gateway web interface.
     *
     * @param ssl use HTTPS when `true`
     * @return the URL of the gateway web interface.
     */
    @SuppressWarnings("unused")
    public String getGatewayUrl(boolean ssl) {
        String mode = ssl ? "https" : "http";
        Integer port = ssl ? getMappedGatewaySslPort() : getMappedGatewayPort();
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

        applyCommands();
        applyEnvironmentVariables();

        exposePorts();

        mapGatewayBackup();
        mapThirdPartyModules();
    }

    private void exposePorts() {
        addExposedPorts(GATEWAY_PORT, GATEWAY_SSL_PORT);

        if (getEnabledModuleIdentifiers().contains(GatewayModule.OPC_UA.getIdentifier())) {
            addExposedPorts(OPCUA_PORT);
        }

        if (debugMode) {
            addExposedPorts(DEBUG_PORT);
        }
    }

    private void mapGatewayBackup() {
        if (gatewayBackup != null) {
            this.withCopyFileToContainer(MountableFile.forHostPath(gatewayBackup), "/restore.gwbk");
        }
    }

    private void mapThirdPartyModules() {
        for (Path path : thirdPartyModules.keySet()) {
            MountableFile file = MountableFile.forHostPath(path);
            String containerPath = Path.of(
                            INSTALL_DIR, "user-lib", "modules", path.toFile().getName())
                    .toString();
            this.withCopyFileToContainer(file, containerPath);
        }
    }

    private void applyCommands() {
        List<String> commands = new ArrayList<>();
        if (debugMode) commands.add("-d");
        if (maxMemory != null) commands.addAll(List.of("-m", maxMemory));
        if (name != null) commands.addAll(List.of("-n", name));
        if (gatewayBackup != null) commands.addAll(List.of("-r", "/restore.gwbk"));

        if (developerMode || additionalArgs != null) {
            commands.add("--");
            if (developerMode) commands.add("-Dignition.allowunsignedmodules=true");
            if (additionalArgs != null) commands.addAll(additionalArgs);
        }

        if (!commands.isEmpty()) {
            this.withCommand(commands.toArray(String[]::new));
        }
    }

    private void applyEnvironmentVariables() {
        if (licenseAccepted) addEnv("ACCEPT_IGNITION_EULA", "Y");
        addEnv("DISABLE_QUICKSTART", String.valueOf(!quickStartEnabled));
        if (username != null) addEnv("GATEWAY_ADMIN_USERNAME", username);
        if (password != null) addEnv("GATEWAY_ADMIN_PASSWORD", password);

        addEnv("GATEWAY_GAN_PORT", String.valueOf(GAN_PORT));
        addEnv("GATEWAY_HTTP_PORT", String.valueOf(GATEWAY_PORT));
        addEnv("GATEWAY_HTTPS_PORT", String.valueOf(GATEWAY_SSL_PORT));

        if (gatewayBackup != null) addEnv("GATEWAY_RESTORE_DISABLED", String.valueOf(restoreDisabled));
        addEnv("GATEWAY_MODULES_ENABLED", getEnabledModulesString());
        String acceptedModuleCertificates = getAcceptedModuleCertificatesString();
        if (!acceptedModuleCertificates.isEmpty()) addEnv("ACCEPT_MODULE_CERTS", acceptedModuleCertificates);

        addEnv("IGNITION_EDITION", edition.toString());
        if (gid != null) addEnv("IGNITION_GID", gid.toString());
        if (uid != null) addEnv("IGNITION_UID", uid.toString());

        if (activationToken != null) addEnv("IGNITION_ACTIVATION_TOKEN", activationToken);
        if (licenseKey != null) addEnv("IGNITION_LICENSE_KEY", licenseKey);

        addEnv("TZ", timezone);
    }

    private String getEnabledModulesString() {
        return String.join(",", getEnabledModuleIdentifiers());
    }

    private Set<String> getEnabledModuleIdentifiers() {
        Set<String> identifiers = new LinkedHashSet<>();
        modules.stream().map(IgnitionModule::getIdentifier).forEach(identifiers::add);
        for (ThirdPartyModuleDescriptor descriptor : thirdPartyModules.values()) {
            identifiers.addAll(descriptor.gatewayDependencies());
            identifiers.add(descriptor.identifier());
        }
        if (identifiers.isEmpty()) identifiers.add(GatewayModule.OPC_UA.getIdentifier());
        return identifiers;
    }

    private String getAcceptedModuleCertificatesString() {
        Set<String> identifiers = new LinkedHashSet<>();
        thirdPartyModules.values().stream()
                .map(ThirdPartyModuleDescriptor::identifier)
                .forEach(identifiers::add);
        return String.join(",", identifiers);
    }

    private void addThirdPartyModule(Path path) throws FileNotFoundException {
        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException(String.format("module '%s' does not exist", path));
        }

        Path modulePath = path.toAbsolutePath().normalize();
        thirdPartyModules.put(modulePath, readThirdPartyModuleDescriptor(modulePath));
    }

    private static ThirdPartyModuleDescriptor readThirdPartyModuleDescriptor(Path modulePath) {
        try (ZipFile moduleFile = new ZipFile(modulePath.toFile())) {
            ZipEntry descriptorEntry = moduleFile.getEntry("module.xml");
            if (descriptorEntry == null) {
                throw new IllegalArgumentException("Module archive does not contain module.xml: " + modulePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            try (InputStream input = moduleFile.getInputStream(descriptorEntry)) {
                Element module = (Element) factory.newDocumentBuilder()
                        .parse(input)
                        .getElementsByTagName("module")
                        .item(0);
                if (module == null) {
                    throw new IllegalArgumentException(
                            "module.xml does not contain a module descriptor: " + modulePath);
                }

                String identifier = requiredElementText(module, "id", modulePath);
                Set<String> gatewayDependencies = new LinkedHashSet<>();
                NodeList dependencies = module.getElementsByTagName("depends");
                for (int index = 0; index < dependencies.getLength(); index++) {
                    Element dependency = (Element) dependencies.item(index);
                    String dependencyIdentifier = dependency.getTextContent().trim();
                    if ("G".equals(dependency.getAttribute("scope")) && !dependencyIdentifier.isEmpty()) {
                        gatewayDependencies.add(dependencyIdentifier);
                    }
                }

                return new ThirdPartyModuleDescriptor(identifier, Set.copyOf(gatewayDependencies));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to read module descriptor: " + modulePath, e);
        }
    }

    private static String requiredElementText(Element module, String elementName, Path modulePath) {
        NodeList elements = module.getElementsByTagName(elementName);
        if (elements.getLength() == 0 || elements.item(0).getTextContent().isBlank()) {
            throw new IllegalArgumentException("module.xml does not contain a " + elementName + ": " + modulePath);
        }
        return elements.item(0).getTextContent().trim();
    }

    @Override
    protected void containerIsStarting(final InspectContainerResponse containerInfo) {
        logger().debug("Ignition container is starting, performing configuration.");
    }

    @Override
    protected void containerIsStarted(final InspectContainerResponse containerInfo) {
        logger().info("Ignition container is ready! Gateway Web UI is available at: {}", getGatewayUrl());
    }

    private record ThirdPartyModuleDescriptor(String identifier, Set<String> gatewayDependencies) {}
}
