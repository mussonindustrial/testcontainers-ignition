package com.mussonindustrial.testcontainers.ignition;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class IgnitionContainer extends GenericContainer<IgnitionContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("inductiveautomation/ignition");
    private static final String DEFAULT_TAG = "8.1.33";
    private final EnvVariables env = new EnvVariables();
    private final RuntimeArguments runtime = new RuntimeArguments();
    private final CustomArguments custom = new CustomArguments();

    public IgnitionContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    public IgnitionContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public IgnitionContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        this.waitStrategy = Wait.forHealthcheck();
    }

    public IgnitionContainer withActivationToken(String token) {
        env.setIgnitionActivationToken(token);
        return self();
    }

    public IgnitionContainer withAdminUsername(String username) {
        env.setGatewayAdminUsername(username);
        return self();
    }

    public IgnitionContainer withAdminPassword(String password) {
        env.setGatewayAdminPassword(password);
        return self();
    }

    public IgnitionContainer withDebugMode(boolean debugMode) {
        runtime.setDebugMode(debugMode);
        return self();
    }

    public IgnitionContainer withEdition(GatewayEdition edition) {
        env.setIgnitionEdition(edition);
        return self();
    }

    public IgnitionContainer withGanPort(int port) {
        env.setGatewayGanPort(port);
        return self();
    }

    public IgnitionContainer withGatewayBackup(String path, boolean restoreDisabled) {
        runtime.setRestorePath("/restore.gwbk");
        this.withCopyFileToContainer(MountableFile.forHostPath(path), "/restore.gwbk");
        env.setGatewayRestoreDisabled(restoreDisabled);
        return self();
    }

    public IgnitionContainer withGatewayName(String name) {
        runtime.setName(name);
        return self();
    }

    public IgnitionContainer withGid(int gid) {
        env.setIgnitionGid(gid);
        return self();
    }

    public IgnitionContainer withHttpPort(int port) {
        env.setGatewayHttpPort(port);
        return self();
    }

    public IgnitionContainer withHttpsPort(int port) {
        env.setGatewayHttpsPort(port);
        return self();
    }

    public IgnitionContainer withInstallPath(String installPath) {
        custom.setInstallPath(installPath);
        return self();
    }

    public IgnitionContainer withLicenseKey(String key) {
        env.setIgnitionLicenseKey(key);
        return self();
    }

    public IgnitionContainer withModules(ModuleIdentifier[] modules) {
        env.setGatewayModulesEnabled(modules);
        return self();
    }

    public IgnitionContainer withMaxMemory(int memoryMax) {
        runtime.setMemoryMax(memoryMax);
        return self();
    }

    public IgnitionContainer withQuickStart(boolean quickStart) {
        env.setDisableQuickStart(!quickStart);
        return self();
    }

    public IgnitionContainer withTimezone(String timezone) {
        env.setTimezone(timezone);
        return self();
    }

    public IgnitionContainer withUid(int uid) {
        env.setIgnitionUid(uid);
        return self();
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

            this.withCommand("-d");
        }

        super.start();
    }

    public static class EnvVariables {

        private String timezone;
        private String acceptIgnitionEula = "Y";
        private Boolean gatewayRestoreDisabled;
        private String gatewayAdminUsername = "admin";
        private String gatewayAdminPassword = "password";
        private Integer gatewayHttpPort = 8088;
        private Integer gatewayHttpsPort = 8043;
        private Integer gatewayGanPort;
        private GatewayEdition ignitionEdition = GatewayEdition.STANDARD;
        private String ignitionLicenseKey;
        private String ignitionActivationToken;
        private GatewayNetworkConfiguration[] gatewayNetwork;
        private EAMType eamSetupInstallSelection;
        private String eamAgentControllerServerName;
        private Integer eamAgentSendStatsInterval;
        private String eamControllerArchivePath;
        private String eamControllerDatasource;
        private String eamControllerArchiveLocation;
        private Integer eamControllerLowDiskThresholdMb;
        private ModuleIdentifier[] gatewayModulesEnabled;
        private Integer ignitionUid;
        private Integer ignitionGid;
        private Boolean disableQuickStart = true;
        private Boolean gatewayNetworkEnabled;
        private Boolean gatewayNetworkRequiresSsl;
        private Boolean gatewayNetworkRequireTwoWayAuth;
        private Integer gatewayNetworkSendThreads;
        private Integer gatewayNetworkReceiveThreads;
        private Integer gatewayNetworkReceiveMax;
        private Boolean gatewayNetworkAllowIncoming;
        private SecurityPolicy gatewayNetworkSecurityPolicy;
        private String[] gatewayNetworkWhitelist;
        private Integer gatewayNetworkAllowedProxyHops;
        private Integer gatewayNetworkWebSocketSessionIdleTimeout;

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public String getAcceptIgnitionEula() { return acceptIgnitionEula; }
        public void setAcceptIgnitionEula(String acceptIgnitionEula) { this.acceptIgnitionEula = acceptIgnitionEula; }

        public Boolean getGatewayRestoreDisabled() { return gatewayRestoreDisabled; }
        public void setGatewayRestoreDisabled(Boolean gatewayRestoreDisabled) { this.gatewayRestoreDisabled = gatewayRestoreDisabled; }

        public String getGatewayAdminUsername() { return gatewayAdminUsername; }
        public void setGatewayAdminUsername(String gatewayAdminUsername) { this.gatewayAdminUsername = gatewayAdminUsername; }

        public String getGatewayAdminPassword() { return gatewayAdminPassword; }
        public void setGatewayAdminPassword(String gatewayAdminPassword) { this.gatewayAdminPassword = gatewayAdminPassword; }

        public Integer getGatewayHttpPort() { return gatewayHttpPort; }
        public void setGatewayHttpPort(Integer gatewayHttpPort) { this.gatewayHttpPort = gatewayHttpPort; }

        public Integer getGatewayHttpsPort() { return gatewayHttpsPort; }
        public void setGatewayHttpsPort(Integer gatewayHttpsPort) { this.gatewayHttpsPort = gatewayHttpsPort; }

        public Integer getGatewayGanPort() { return gatewayGanPort; }
        public void setGatewayGanPort(Integer gatewayGanPort) { this.gatewayGanPort = gatewayGanPort; }

        public GatewayEdition getIgnitionEdition() { return ignitionEdition; }
        public void setIgnitionEdition(GatewayEdition ignitionEdition) { this.ignitionEdition = ignitionEdition; }

        public String getIgnitionLicenseKey() { return ignitionLicenseKey; }
        public void setIgnitionLicenseKey(String ignitionLicenseKey) { this.ignitionLicenseKey = ignitionLicenseKey; }

        public String getIgnitionActivationToken() { return ignitionActivationToken; }
        public void setIgnitionActivationToken(String ignitionActivationToken) { this.ignitionActivationToken = ignitionActivationToken; }

        public GatewayNetworkConfiguration[] getGatewayNetwork() { return gatewayNetwork; }
        public void setGatewayNetwork(GatewayNetworkConfiguration[] gatewayNetwork) { this.gatewayNetwork = gatewayNetwork; }

        public EAMType getEamSetupInstallSelection() { return eamSetupInstallSelection; }
        public void setEamSetupInstallSelection(EAMType eamSetupInstallSelection) { this.eamSetupInstallSelection = eamSetupInstallSelection; }

        public String getEamAgentControllerServerName() { return eamAgentControllerServerName; }
        public void setEamAgentControllerServerName(String eamAgentControllerServerName) { this.eamAgentControllerServerName = eamAgentControllerServerName; }

        public Integer getEamAgentSendStatsInterval() { return eamAgentSendStatsInterval; }
        public void setEamAgentSendStatsInterval(Integer eamAgentSendStatsInterval) { this.eamAgentSendStatsInterval = eamAgentSendStatsInterval; }

        public String getEamControllerArchivePath() { return eamControllerArchivePath; }
        public void setEamControllerArchivePath(String eamControllerArchivePath) { this.eamControllerArchivePath = eamControllerArchivePath; }

        public String getEamControllerDatasource() { return eamControllerDatasource; }
        public void setEamControllerDatasource(String eamControllerDatasource) { this.eamControllerDatasource = eamControllerDatasource; }

        public String getEamControllerArchiveLocation() { return eamControllerArchiveLocation; }
        public void setEamControllerArchiveLocation(String eamControllerArchiveLocation) { this.eamControllerArchiveLocation = eamControllerArchiveLocation; }

        public Integer getEamControllerLowDiskThresholdMb() { return eamControllerLowDiskThresholdMb; }
        public void setEamControllerLowDiskThresholdMb(Integer eamControllerLowDiskThresholdMb) { this.eamControllerLowDiskThresholdMb = eamControllerLowDiskThresholdMb; }

        public ModuleIdentifier[] getGatewayModulesEnabled() { return gatewayModulesEnabled; }
        public void setGatewayModulesEnabled(ModuleIdentifier[] gatewayModulesEnabled) { this.gatewayModulesEnabled = gatewayModulesEnabled; }

        public Integer getIgnitionUid() { return ignitionUid; }
        public void setIgnitionUid(Integer ignitionUid) { this.ignitionUid = ignitionUid; }

        public Integer getIgnitionGid() { return ignitionGid; }
        public void setIgnitionGid(Integer ignitionGid) { this.ignitionGid = ignitionGid; }

        public Boolean getDisableQuickStart() { return disableQuickStart; }
        public void setDisableQuickStart(Boolean disableQuickStart) { this.disableQuickStart = disableQuickStart; }

        public Boolean getGatewayNetworkEnabled() { return gatewayNetworkEnabled; }
        public void setGatewayNetworkEnabled(Boolean gatewayNetworkEnabled) { this.gatewayNetworkEnabled = gatewayNetworkEnabled; }

        public Boolean getGatewayNetworkRequiresSsl() { return gatewayNetworkRequiresSsl; }
        public void setGatewayNetworkRequiresSsl(Boolean gatewayNetworkRequiresSsl) { this.gatewayNetworkRequiresSsl = gatewayNetworkRequiresSsl; }

        public Boolean getGatewayNetworkRequireTwoWayAuth() { return gatewayNetworkRequireTwoWayAuth; }
        public void setGatewayNetworkRequireTwoWayAuth(Boolean gatewayNetworkRequireTwoWayAuth) { this.gatewayNetworkRequireTwoWayAuth = gatewayNetworkRequireTwoWayAuth; }

        public Integer getGatewayNetworkSendThreads() { return gatewayNetworkSendThreads; }
        public void setGatewayNetworkSendThreads(Integer gatewayNetworkSendThreads) { this.gatewayNetworkSendThreads = gatewayNetworkSendThreads; }

        public Integer getGatewayNetworkReceiveThreads() { return gatewayNetworkReceiveThreads; }
        public void setGatewayNetworkReceiveThreads(Integer gatewayNetworkReceiveThreads) { this.gatewayNetworkReceiveThreads = gatewayNetworkReceiveThreads; }

        public Integer getGatewayNetworkReceiveMax() { return gatewayNetworkReceiveMax; }
        public void setGatewayNetworkReceiveMax(Integer gatewayNetworkReceiveMax) { this.gatewayNetworkReceiveMax = gatewayNetworkReceiveMax; }

        public Boolean getGatewayNetworkAllowIncoming() { return gatewayNetworkAllowIncoming; }
        public void setGatewayNetworkAllowIncoming(Boolean gatewayNetworkAllowIncoming) { this.gatewayNetworkAllowIncoming = gatewayNetworkAllowIncoming; }

        public SecurityPolicy getGatewayNetworkSecurityPolicy() { return gatewayNetworkSecurityPolicy; }
        public void setGatewayNetworkSecurityPolicy(SecurityPolicy gatewayNetworkSecurityPolicy) { this.gatewayNetworkSecurityPolicy = gatewayNetworkSecurityPolicy; }

        public String[] getGatewayNetworkWhitelist() { return gatewayNetworkWhitelist; }
        public void setGatewayNetworkWhitelist(String[] gatewayNetworkWhitelist) { this.gatewayNetworkWhitelist = gatewayNetworkWhitelist; }

        public Integer getGatewayNetworkAllowedProxyHops() { return gatewayNetworkAllowedProxyHops; }
        public void setGatewayNetworkAllowedProxyHops(Integer gatewayNetworkAllowedProxyHops) { this.gatewayNetworkAllowedProxyHops = gatewayNetworkAllowedProxyHops; }

        public Integer getGatewayNetworkWebSocketSessionIdleTimeout() { return gatewayNetworkWebSocketSessionIdleTimeout; }
        public void setGatewayNetworkWebSocketSessionIdleTimeout(Integer gatewayNetworkWebSocketSessionIdleTimeout) { this.gatewayNetworkWebSocketSessionIdleTimeout = gatewayNetworkWebSocketSessionIdleTimeout; }

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();

            if (acceptIgnitionEula != null) map.put("ACCEPT_IGNITION_EULA", acceptIgnitionEula);
            if (disableQuickStart != null) map.put("DISABLE_QUICKSTART", disableQuickStart.toString());
            if (eamAgentControllerServerName != null) map.put("EAM_AGENT_CONTROLLERSERVERNAME", eamAgentControllerServerName);
            if (eamAgentSendStatsInterval != null) map.put("EAM_AGENT_SENDSTATSINTERVAL", eamAgentSendStatsInterval.toString());
            if (eamControllerArchiveLocation != null) map.put("EAM_CONTROLLER_ARCHIVELOCATION", eamControllerArchiveLocation);
            if (eamControllerArchivePath != null) map.put("EAM_CONTROLLER_ARCHIVEPATH", eamControllerArchivePath);
            if (eamControllerLowDiskThresholdMb != null) map.put("EAM_CONTROLLER_LOWDISKTHRESHOLDMB", eamControllerLowDiskThresholdMb.toString());
            if (eamControllerDatasource != null) map.put("EAM_CONTROLLER_DATASOURCE", eamControllerDatasource);
            if (eamSetupInstallSelection != null) map.put("EAM_SETUP_INSTALLSELECTION", eamSetupInstallSelection.toString());
            if (gatewayAdminPassword != null) map.put("GATEWAY_ADMIN_PASSWORD", gatewayAdminPassword);
            if (gatewayAdminUsername != null) map.put("GATEWAY_ADMIN_USERNAME", gatewayAdminUsername);
            if (gatewayNetworkAllowIncoming != null) map.put("GATEWAY_NETWORK_ALLOWINCOMING", gatewayNetworkAllowIncoming.toString());
            if (gatewayNetworkAllowedProxyHops != null) map.put("GATEWAY_NETWORK_ALLOWEDPROXYHOPS", gatewayNetworkAllowedProxyHops.toString());
            if (gatewayNetworkEnabled != null) map.put("GATEWAY_NETWORK_ENABLED", gatewayNetworkEnabled.toString());
            if (gatewayNetworkRequireTwoWayAuth != null) map.put("GATEWAY_NETWORK_REQUIRETWOWAYAUTH", gatewayNetworkRequireTwoWayAuth.toString());
            if (gatewayNetworkRequiresSsl != null) map.put("GATEWAY_NETWORK_REQUIRESSL", gatewayNetworkRequiresSsl.toString());
            if (gatewayNetworkReceiveMax != null) map.put("GATEWAY_NETWORK_RECEIVEMAX", gatewayNetworkReceiveMax.toString());
            if (gatewayNetworkReceiveThreads != null) map.put("GATEWAY_NETWORK_RECEIVETHREADS", gatewayNetworkReceiveThreads.toString());
            if (gatewayNetworkSecurityPolicy != null) map.put("GATEWAY_NETWORK_SECURITYPOLICY", gatewayNetworkSecurityPolicy.toString());
            if (gatewayNetwork != null) map.put("GATEWAY_NETWORK", arrayToString(gatewayNetwork));
            if (gatewayNetworkSendThreads != null) map.put("GATEWAY_NETWORK_SENDTHREADS", gatewayNetworkSendThreads.toString());
            if (gatewayNetworkWhitelist != null) map.put("GATEWAY_NETWORK_WHITELIST", arrayToString(gatewayNetworkWhitelist));
            if (gatewayNetworkWebSocketSessionIdleTimeout != null) map.put("GATEWAY_NETWORK_WEBSOCKETSESSIONIDLETIMEOUT", gatewayNetworkWebSocketSessionIdleTimeout.toString());
            if (gatewayRestoreDisabled != null) map.put("GATEWAY_RESTORE_DISABLED", gatewayRestoreDisabled.toString());
            if (gatewayGanPort != null) map.put("GATEWAY_GAN_PORT", gatewayGanPort.toString());
            if (gatewayHttpPort != null) map.put("GATEWAY_HTTP_PORT", gatewayHttpPort.toString());
            if (gatewayHttpsPort != null) map.put("GATEWAY_HTTPS_PORT", gatewayHttpsPort.toString());
            if (ignitionEdition != null) map.put("IGNITION_EDITION", ignitionEdition.toString());
            if (ignitionActivationToken != null) map.put("IGNITION_ACTIVATION_TOKEN", ignitionActivationToken);
            if (ignitionGid != null) map.put("IGNITION_GID", ignitionGid.toString());
            if (ignitionLicenseKey != null) map.put("IGNITION_LICENSE_KEY", ignitionLicenseKey);
            if (ignitionUid != null) map.put("IGNITION_UID", ignitionUid.toString());
            if (gatewayModulesEnabled != null) map.put("GATEWAY_MODULES_ENABLED", arrayToString(gatewayModulesEnabled));
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

    private static class CustomArguments {
        private String installPath = "/usr/local/bin/ignition";

        public String getInstallPath() { return installPath; }
        public void setInstallPath(String installPath) { this.installPath = installPath; }
    }
}
