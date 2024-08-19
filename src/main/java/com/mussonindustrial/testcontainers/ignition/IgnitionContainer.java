package com.mussonindustrial.testcontainers.ignition;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Testcontainers implementation for Ignition.
 * <p>
 * Supported image: {@code inductiveautomation/ignition}
 * <p>
 * Exposed ports:
 * <ul>
 *     <li>Gateway (HTTP): 8080</li>
 *     <li>Gateway (HTTPS): 8043</li>
 *     <li>GAN: 8060</li>
 * </ul>
 */
public class IgnitionContainer extends GenericContainer<IgnitionContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("inductiveautomation/ignition");
    private static final String DEFAULT_TAG = "8.1.33";

    private final EnvVariables env = new EnvVariables();
    private final RuntimeArguments runtime = new RuntimeArguments();

    /**
     * Creates a new Ignition container with the default image and version.
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
        env.setIgnitionActivationToken(token);
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
        env.setGatewayAdminUsername(username);
        env.setGatewayAdminPassword(username);
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
        runtime.setDebugMode(debugMode);
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
        env.setIgnitionEdition(edition);
        return self();
    }

    /**
     * Set a gateway backup file (*.gwbk) to restore from.
     *
     * @param path the path to the gateway backup file.
     * @param restoreDisabled true to restore to a disabled state.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withGatewayBackup(String path, boolean restoreDisabled) {
        checkNotRunning();
        runtime.setRestorePath("/restore.gwbk");
        this.withCopyFileToContainer(MountableFile.forHostPath(path), "/restore.gwbk");
        env.setGatewayRestoreDisabled(restoreDisabled);
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
        runtime.setName(name);
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
        env.setIgnitionGid(gid);
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
        env.setIgnitionLicenseKey(key);
        return self();
    }

    /**
     * Set the maximum memory usage of the gateway.
     *
     * @param memoryMax the maximum memory to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withMaxMemory(int memoryMax) {
        checkNotRunning();
        runtime.setMemoryMax(memoryMax);
        return self();
    }

    /**
     * Include a module when initializing the gateway.
     *
     * @param module the module to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withModule(Module module) {
        checkNotRunning();
        env.addGatewayModule(module);
        return self();
    }

    /**
     * Include a list of modules when initializing the gateway.
     *
     * @param modules the modules to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withModules(Module[] modules) {
        checkNotRunning();
        Arrays.stream(modules).forEach(env::addGatewayModule);
        return self();
    }

    /**
     * Include a third party module when initializing the gateway.
     *
     * @param path the path to the module file to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withThirdPartyModule(String path) {
        checkNotRunning();
        File f = new File(path);
        String containerPath = "/usr/local/bin/ignition/user-lib/modules/" + f.getName();
        this.withCopyFileToContainer(MountableFile.forHostPath(path), containerPath);
        env.addThirdPartyModule(path);
        return self();
    }

    /**
     * Include a list of third party modules when initializing the gateway.
     *
     * @param paths the paths to the modules files to add.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withThirdPartyModules(String[] paths) {
        checkNotRunning();
        Arrays.stream(paths).forEach((path) -> {
            File f = new File(path);
            String containerPath = "/usr/local/bin/ignition/user-lib/modules/" + f.getName();
            this.withCopyFileToContainer(MountableFile.forHostPath(path), containerPath);
            env.addThirdPartyModule(path);
        });
        return self();
    }

    /**
     * Set quick start mode.
     *
     * @param quickStart the debug mode setting to use.
     * @return this {@link IgnitionContainer} for chaining purposes.
     */
    @SuppressWarnings("unused")
    public IgnitionContainer withQuickStart(boolean quickStart) {
        checkNotRunning();
        env.setDisableQuickStart(!quickStart);
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
        env.setTimezone(timezone);
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
        env.setIgnitionUid(uid);
        return self();
    }

    /**
     * Get the gateway admin username.
     *
     * @return the gateway admin username.
     */
    @SuppressWarnings("unused")
    public String getUsername() {
        return env.gatewayAdminUsername;
    }

    /**
     * Get the gateway admin password.
     *
     * @return the gateway admin password.
     */
    @SuppressWarnings("unused")
    public String getPassword() {
        return env.gatewayAdminPassword;
    }

    /**
     * Get the mapped gateway HTTP port.
     *
     * @return the mapped gateway HTTP port.
     */
    @SuppressWarnings("unused")
    public int getGatewayHttpPort() {
        return getMappedPort(env.gatewayHttpPort);
    }

    /**
     * Get the mapped gateway HTTPS port.
     *
     * @return the mapped gateway HTTPS port.
     */
    @SuppressWarnings("unused")
    public int getGatewayHttpsPort() {
        return getMappedPort(env.gatewayHttpsPort);
    }

    /**
     * Get the mapped gateway GAN (gateway area network) port.
     *
     * @return the mapped gateway GAN port.
     */
    @SuppressWarnings("unused")
    public int getGatewayGanPort() {
        return getMappedPort(env.gatewayGanPort);
    }

    /**
     * Get the mapped remote JVM debugging port.
     *
     * @return the mapped remote JVM debugging port.
     */
    @SuppressWarnings("unused")
    public int getDebugPort() {
        return getMappedPort(env.debugPort);
    }

    /**
     * Get the URL of the gateway web interface (using HTTP).
     *
     * @return the URL of the gateway web interface.
     */
    @SuppressWarnings("unused")
    public String getGatewayUrl() {
        return String.format("http://%s:%d", getHost(), getGatewayHttpPort());
    }

    /**
     * Get the URL of the gateway web interface.
     * @param useHttps use HTTPS when `true`
     *
     * @return the URL of the gateway web interface.
     */
    @SuppressWarnings("unused")
    public String getGatewayUrl(boolean useHttps) {
        String mode = useHttps ? "https" : "http";
        return String.format("%s://%s:%d", mode, getHost(), getGatewayHttpPort());
    }

    /**
     * Checks if already running and if so raises an exception to prevent too-late setters.
     */
    private void checkNotRunning() {
        if (isRunning()) {
            throw new IllegalStateException("Setter can only be called before the container is running");
        }
    }

    @Override
    public void start() {
        this.waitingFor(Wait.forHealthcheck());
        this.withEnv(env.toMap());

        if (env.getGatewayHttpPort() != null) {
            this.addExposedPort(env.getGatewayHttpPort());
        }
        if (env.getGatewayHttpsPort() != null) {
            this.addExposedPort(env.getGatewayHttpsPort());
        }
        if (env.getGatewayGanPort() != null) {
            this.addExposedPort(env.getGatewayGanPort());
        }

        if (runtime.getName() != null) {
            this.withCommand("-n", runtime.getName());
        }
        if (runtime.getMemoryMax() != null) {
            this.withCommand("-m", runtime.getMemoryMax().toString());
        }
        if (runtime.getRestorePath() != null) {
            this.withCommand("-r", runtime.getRestorePath());
        }
        if (runtime.getDebugMode() != null && runtime.getDebugMode()) {
            this.addExposedPort(env.getDebugPort());
            this.withCommand("-d");
        }

        super.start();
    }

    @Override
    protected void containerIsStarting(final InspectContainerResponse containerInfo) {
        logger().debug("Ignition container is starting, performing configuration.");
    }

    @Override
    protected void containerIsStarted(final InspectContainerResponse containerInfo) {
        logger().info("Ignition container is ready! Gateway Web UI is available at {}", getGatewayUrl());
    }

    private static class EnvVariables {

        private String timezone;
        private Boolean gatewayRestoreDisabled;
        private String gatewayAdminUsername = "admin";
        private String gatewayAdminPassword = "password";
        private final Integer gatewayHttpPort = 8088;
        private final Integer gatewayHttpsPort = 8043;
        private final Integer gatewayGanPort = 8060;

        private final Integer debugPort = 8000;
        private GatewayEdition ignitionEdition = GatewayEdition.STANDARD;
        private String ignitionLicenseKey;
        private String ignitionActivationToken;
        private Module[] gatewayModules;
        private String[] thirdPartyModules;
        private Integer ignitionUid;
        private Integer ignitionGid;
        private Boolean disableQuickStart = true;

        public void setTimezone(String timezone) { this.timezone = timezone; }

        public void setGatewayRestoreDisabled(Boolean gatewayRestoreDisabled) { this.gatewayRestoreDisabled = gatewayRestoreDisabled; }

        public void setGatewayAdminUsername(String gatewayAdminUsername) { this.gatewayAdminUsername = gatewayAdminUsername; }

        public void setGatewayAdminPassword(String gatewayAdminPassword) { this.gatewayAdminPassword = gatewayAdminPassword; }

        public Integer getGatewayHttpPort() { return gatewayHttpPort; }

        public Integer getGatewayHttpsPort() { return gatewayHttpsPort; }

        public Integer getGatewayGanPort() { return gatewayGanPort; }

        public Integer getDebugPort() { return debugPort; }

        public void setIgnitionEdition(GatewayEdition ignitionEdition) { this.ignitionEdition = ignitionEdition; }

        public void setIgnitionLicenseKey(String ignitionLicenseKey) { this.ignitionLicenseKey = ignitionLicenseKey; }

        public void setIgnitionActivationToken(String ignitionActivationToken) { this.ignitionActivationToken = ignitionActivationToken; }

        public void addGatewayModule(Module moduleIdentifier) { this.gatewayModules =  ArrayUtils.add(gatewayModules, moduleIdentifier ); }

        public void addThirdPartyModule(String thirdPartyModule) { this.thirdPartyModules =  ArrayUtils.add(thirdPartyModules, thirdPartyModule ); }


        public void setIgnitionUid(Integer ignitionUid) { this.ignitionUid = ignitionUid; }

        public void setIgnitionGid(Integer ignitionGid) { this.ignitionGid = ignitionGid; }

        public void setDisableQuickStart(Boolean disableQuickStart) { this.disableQuickStart = disableQuickStart; }

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();

            String acceptIgnitionEula = "Y";
            map.put("ACCEPT_IGNITION_EULA", acceptIgnitionEula);
            if (disableQuickStart != null) map.put("DISABLE_QUICKSTART", disableQuickStart.toString());
            if (gatewayAdminPassword != null) map.put("GATEWAY_ADMIN_PASSWORD", gatewayAdminPassword);
            if (gatewayAdminUsername != null) map.put("GATEWAY_ADMIN_USERNAME", gatewayAdminUsername);
            if (gatewayRestoreDisabled != null) map.put("GATEWAY_RESTORE_DISABLED", gatewayRestoreDisabled.toString());
            map.put("GATEWAY_GAN_PORT", gatewayGanPort.toString());
            map.put("GATEWAY_HTTP_PORT", gatewayHttpPort.toString());
            map.put("GATEWAY_HTTPS_PORT", gatewayHttpsPort.toString());
            if (ignitionEdition != null) map.put("IGNITION_EDITION", ignitionEdition.toString());
            if (ignitionActivationToken != null) map.put("IGNITION_ACTIVATION_TOKEN", ignitionActivationToken);
            if (ignitionGid != null) map.put("IGNITION_GID", ignitionGid.toString());
            if (ignitionLicenseKey != null) map.put("IGNITION_LICENSE_KEY", ignitionLicenseKey);
            if (ignitionUid != null) map.put("IGNITION_UID", ignitionUid.toString());
            if (gatewayModules != null) map.put("GATEWAY_MODULES_ENABLED", arrayToString(gatewayModules));
            if (timezone != null) map.put("TZ", timezone);

            return map;
        }

        private String arrayToString(Object[] array) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i].toString());
                if (i < array.length - 1) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
    }


    private static class RuntimeArguments {
        private Boolean debugMode;
        private String name;
        private String restorePath;
        private Integer memoryMax;

        public Boolean getDebugMode() { return debugMode; }
        public void setDebugMode(Boolean debugMode) { this.debugMode = debugMode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRestorePath() { return restorePath; }
        public void setRestorePath(String restorePath) { this.restorePath = restorePath; }
        public Integer getMemoryMax() { return memoryMax; }
        public void setMemoryMax(Integer memoryMax) { this.memoryMax = memoryMax; }
    }
}
