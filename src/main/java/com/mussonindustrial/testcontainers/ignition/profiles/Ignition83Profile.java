package com.mussonindustrial.testcontainers.ignition.profiles;

import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.*;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.*;
import static com.mussonindustrial.testcontainers.ignition.compatibility.ModuleDefinition.module;

import com.mussonindustrial.testcontainers.ignition.IgnitionCapability;
import com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint;
import com.mussonindustrial.testcontainers.ignition.IgnitionGatewayEdition;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilityApplier;
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilityMatrix;
import com.mussonindustrial.testcontainers.ignition.compatibility.EndpointCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.ModuleCatalog;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerFileCopy;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerPlan;
import com.mussonindustrial.testcontainers.ignition.internal.GatewayCredentials;
import com.mussonindustrial.testcontainers.ignition.internal.GatewayRestore;
import com.mussonindustrial.testcontainers.ignition.internal.IgnitionContainerSpec;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Docker API profile for the Ignition 8.3 release line.
 *
 * <p>This profile declares the capabilities, modules, endpoints, and
 * container configuration behavior supported by Ignition 8.3 images.
 */
public final class Ignition83Profile implements IgnitionProfile {

    /** Human-readable name for this profile. */
    private static final String PROFILE_NAME = "Ignition 8.3";

    /** Root installation directory in the Ignition 8.3 Docker image. */
    private static final String INSTALLATION_DIRECTORY = "/usr/local/bin/ignition";

    /** Directory where integrated third-party module files are installed. */
    private static final String MODULE_DIRECTORY = INSTALLATION_DIRECTORY + "/user-lib/modules";

    /** Container path used for a staged Gateway backup. */
    private static final String RESTORE_PATH = "/restore.gwbk";

    /**
     * Capabilities provided by the Ignition 8.3 Docker image and the first
     * release in which each capability is available.
     */
    private static final CapabilityMatrix CAPABILITIES = CapabilityMatrix.profile(PROFILE_NAME)
            .supported(LICENSE_ACCEPTANCE, "8.3.0")
            .supported(LEASED_LICENSE_ACTIVATION, "8.3.0")
            .supported(INITIAL_ADMIN_CONFIGURATION, "8.3.0")
            .supported(GATEWAY_NAME, "8.3.0")
            .supported(GATEWAY_EDITION, "8.3.0")
            .supported(GATEWAY_RESTORE, "8.3.0")
            .supported(DEBUG_MODE, "8.3.0")
            .supported(MAX_MEMORY, "8.3.0")
            .supported(SUPPLEMENTAL_ARGUMENTS, "8.3.0")
            .supported(BUILT_IN_MODULE_SELECTION, "8.3.0")
            .supported(THIRD_PARTY_MODULE_INSTALLATION, "8.3.0")
            .supported(PROCESS_IDENTITY, "8.3.0")
            .supported(QUICK_START_CONTROL, "8.3.0")
            .complete();

    /**
     * Network endpoints available from the Ignition 8.3 Docker image.
     *
     * <p>Endpoint declarations do not imply that the port is always exposed.
     * Modules and capabilities determine which optional endpoints are added
     * to the final container plan.
     */
    private static final EndpointCatalog ENDPOINTS = EndpointCatalog.profile(PROFILE_NAME)
            .endpoint(GATEWAY_HTTP, 8088, "Gateway HTTP")
            .endpoint(GATEWAY_HTTPS, 8043, "Gateway HTTPS")
            .endpoint(GATEWAY_NETWORK, 8060, "Gateway Network")
            .endpoint(OPC_UA, 62541, "OPC UA server and discovery")
            .endpoint(DEBUG, 8000, "Gateway JVM debugger")
            .complete();

    /**
     * Built-in modules available from the Ignition 8.3 Docker image.
     *
     * <p>Each entry maps a stable logical module to its fully qualified
     * Ignition 8.3 identifier, dependencies, and contributed endpoints.
     * Modules omitted from this catalog are treated as unsupported.
     */
    private static final ModuleCatalog MODULES = ModuleCatalog.profile(PROFILE_NAME)
            .supported(IgnitionModule.ALARM_NOTIFICATION, "8.3.0", module("com.inductiveautomation.alarm-notification"))
            .supported(
                    IgnitionModule.ALLEN_BRADLEY_LEGACY_DRIVER,
                    "8.3.0",
                    module("com.inductiveautomation.opcua.drivers.ablegacy"))
            .supported(IgnitionModule.BACNET_DRIVER, "8.3.0", module("com.inductiveautomation.opcua.drivers.bacnet"))
            .supported(IgnitionModule.DNP3_DRIVER, "8.3.0", module("com.inductiveautomation.opcua.drivers.dnp3"))
            .supported(IgnitionModule.DNP3_DRIVER_V2, "8.3.0", module("com.inductiveautomation.opcua.drivers.dnp3v2"))
            .supported(IgnitionModule.ENTERPRISE_ADMINISTRATION, "8.3.0", module("com.inductiveautomation.eam"))
            .supported(IgnitionModule.EVENT_STREAMS, "8.3.0", module("com.inductiveautomation.eventstream"))
            .supported(IgnitionModule.HISTORIAN_CORE, "8.3.0", module("com.inductiveautomation.historian"))
            .supported(
                    IgnitionModule.SQL_HISTORIAN,
                    "8.3.0",
                    module("com.inductiveautomation.historian.sql").requires(IgnitionModule.HISTORIAN_CORE))
            .supported(
                    IgnitionModule.IEC_61850_DRIVER, "8.3.0", module("com.inductiveautomation.opcua.drivers.iec61850"))
            .supported(IgnitionModule.KAFKA_CONNECTOR, "8.3.0", module("com.inductiveautomation.connectors.kafka"))
            .supported(IgnitionModule.LOGIX_DRIVER, "8.3.0", module("com.inductiveautomation.opcua.drivers.logix"))
            .supported(
                    IgnitionModule.MICRO800_DRIVER, "8.3.0", module("com.inductiveautomation.opcua.drivers.micro800"))
            .supported(
                    IgnitionModule.MITSUBISHI_DRIVER,
                    "8.3.0",
                    module("com.inductiveautomation.opcua.drivers.mitsubishi"))
            .supported(IgnitionModule.MODBUS_DRIVER_V2, "8.3.0", module("com.inductiveautomation.opcua.drivers.modbus"))
            .supported(IgnitionModule.MONGODB_CONNECTOR, "8.3.0", module("com.inductiveautomation.connectors.mongodb"))
            .supported(IgnitionModule.OMRON_DRIVER, "8.3.0", module("com.inductiveautomation.opcua.drivers.omron"))
            .supported(
                    IgnitionModule.OPC_UA,
                    "8.3.0",
                    module("com.inductiveautomation.opcua").exposes(IgnitionEndpoint.OPC_UA))
            .supported(IgnitionModule.PERSPECTIVE, "8.3.0", module("com.inductiveautomation.perspective"))
            .supported(IgnitionModule.REPORTING, "8.3.0", module("com.inductiveautomation.reporting"))
            .supported(IgnitionModule.SFC, "8.3.0", module("com.inductiveautomation.sfc"))
            .supported(IgnitionModule.SIEMENS_DRIVER, "8.3.0", module("com.inductiveautomation.opcua.drivers.siemens"))
            .supported(
                    IgnitionModule.SIEMENS_SYMBOLIC_DRIVER,
                    "8.3.0",
                    module("com.inductiveautomation.opcua.drivers.siemens-symbolic"))
            .supported(IgnitionModule.SMS_NOTIFICATION, "8.3.0", module("com.inductiveautomation.sms-notification"))
            .supported(IgnitionModule.SQL_BRIDGE, "8.3.0", module("com.inductiveautomation.sqlbridge"))
            .supported(IgnitionModule.SYMBOL_FACTORY, "8.3.0", module("com.inductiveautomation.symbol-factory"))
            .supported(IgnitionModule.UDP_TCP_DRIVERS, "8.3.0", module("com.inductiveautomation.opcua.drivers.tcpudp"))
            .supported(IgnitionModule.VISION, "8.3.0", module("com.inductiveautomation.vision"))
            .supported(IgnitionModule.VOICE_NOTIFICATION, "8.3.0", module("com.inductiveautomation.phone-notification"))
            .supported(IgnitionModule.WEB_DEV, "8.3.0", module("com.inductiveautomation.webdev"))
            .supported(
                    IgnitionModule.POSTGRESQL_JDBC_DRIVER, "8.3.0", module("com.inductiveautomation.jdbc.postgresql"))
            .supported(IgnitionModule.MARIADB_JDBC_DRIVER, "8.3.0", module("com.inductiveautomation.jdbc.mariadb"))
            .supported(IgnitionModule.MSSQL_JDBC_DRIVER, "8.3.0", module("com.inductiveautomation.jdbc.mssql"))
            .complete();

    /**
     * Functions that translate requested capabilities into the Ignition 8.3
     * Docker image configuration.
     */
    private static final Map<IgnitionCapability, CapabilityApplier> APPLIERS = Map.ofEntries(
            Map.entry(LICENSE_ACCEPTANCE, Ignition83Profile::applyLicenseAcceptance),
            Map.entry(LEASED_LICENSE_ACTIVATION, Ignition83Profile::applyLeasedLicenseActivation),
            Map.entry(INITIAL_ADMIN_CONFIGURATION, Ignition83Profile::applyInitialAdminConfiguration),
            Map.entry(GATEWAY_NAME, Ignition83Profile::applyGatewayName),
            Map.entry(GATEWAY_EDITION, Ignition83Profile::applyGatewayEdition),
            Map.entry(GATEWAY_RESTORE, Ignition83Profile::applyGatewayRestore),
            Map.entry(DEBUG_MODE, Ignition83Profile::applyDebugMode),
            Map.entry(MAX_MEMORY, Ignition83Profile::applyMaxMemory),
            Map.entry(SUPPLEMENTAL_ARGUMENTS, Ignition83Profile::applySupplementalArguments),
            Map.entry(BUILT_IN_MODULE_SELECTION, Ignition83Profile::applyBuiltInModuleSelection),
            Map.entry(THIRD_PARTY_MODULE_INSTALLATION, Ignition83Profile::applyThirdPartyModules),
            Map.entry(PROCESS_IDENTITY, Ignition83Profile::applyProcessIdentity),
            Map.entry(QUICK_START_CONTROL, Ignition83Profile::applyQuickStartControl));

    @Override
    public String name() {
        return PROFILE_NAME;
    }

    @Override
    public boolean matches(IgnitionVersion version) {
        return version.isReleaseLine(8, 3);
    }

    @Override
    public CapabilityMatrix capabilities() {
        return CAPABILITIES;
    }

    @Override
    public ModuleCatalog modules() {
        return MODULES;
    }

    @Override
    public EndpointCatalog endpoints() {
        return ENDPOINTS;
    }

    @Override
    public CapabilityApplier applier(IgnitionCapability capability) {
        return APPLIERS.get(capability);
    }

    /**
     * Applies settings and endpoints that are present independently of
     * explicitly requested capabilities.
     */
    @Override
    public void applyDefaults(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("TZ", specification.timezone());

        plan.expose(ENDPOINTS.port(GATEWAY_HTTP), ENDPOINTS.port(GATEWAY_HTTPS), ENDPOINTS.port(GATEWAY_NETWORK));
    }

    /**
     * Adds the environment variable indicating that the Ignition EULA was
     * accepted.
     */
    private static void applyLicenseAcceptance(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        if (specification.licenseAccepted()) {
            plan.environment("ACCEPT_IGNITION_EULA", "Y");
        }
    }

    /**
     * Adds the license key and activation token used for leased-license
     * activation.
     */
    private static void applyLeasedLicenseActivation(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_LICENSE_KEY", specification.licenseKey());

        plan.environment("IGNITION_ACTIVATION_TOKEN", specification.activationToken());
    }

    /**
     * Adds the initial Gateway administrator username and password.
     */
    private static void applyInitialAdminConfiguration(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        GatewayCredentials credentials = specification.credentials();

        plan.environment("GATEWAY_ADMIN_USERNAME", credentials.username());

        plan.environment("GATEWAY_ADMIN_PASSWORD", credentials.password());
    }

    /**
     * Adds the startup argument used to assign the Gateway name.
     */
    private static void applyGatewayName(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.arguments("-n", specification.gatewayName());
    }

    /**
     * Adds the environment variable used to select the Gateway edition.
     */
    private static void applyGatewayEdition(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_EDITION", editionIdentifier(specification.edition()));
    }

    /**
     * Copies a Gateway backup into the container and configures it for
     * restoration during startup.
     */
    private static void applyGatewayRestore(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        GatewayRestore restore = specification.gatewayRestore();

        plan.copy(restore.backup(), RESTORE_PATH, ContainerFileCopy.READ_ONLY_MODE);

        plan.arguments("-r", RESTORE_PATH);

        plan.environment("GATEWAY_RESTORE_DISABLED", Boolean.toString(restore.restoreDisabled()));
    }

    /**
     * Enables Gateway JVM debugging and exposes the debugger endpoint.
     */
    private static void applyDebugMode(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        if (!specification.debugMode()) {
            return;
        }

        plan.argument("-d");
        plan.expose(ENDPOINTS.port(DEBUG));
    }

    /**
     * Adds the startup argument used to configure maximum Gateway memory.
     */
    private static void applyMaxMemory(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.arguments("-m", specification.maxMemory());
    }

    /**
     * Appends supplemental wrapper, JVM, or Gateway arguments after the
     * startup argument delimiter.
     */
    private static void applySupplementalArguments(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        List<String> arguments = specification.additionalArguments();

        if (arguments.isEmpty()) {
            return;
        }

        plan.argument("--");
        plan.arguments(arguments);
    }

    /**
     * Resolves the requested logical modules into Ignition 8.3 identifiers
     * and exposes any endpoints contributed by those modules.
     */
    private static void applyBuiltInModuleSelection(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        ModuleCatalog.Resolution resolution = MODULES.resolve(version, specification.modules());

        resolution.warnings().forEach(plan::warning);

        plan.environment("GATEWAY_MODULES_ENABLED", String.join(",", resolution.identifiers()));

        resolution.endpoints().stream().mapToInt(ENDPOINTS::port).forEach(plan::expose);
    }

    /**
     * Copies requested third-party module files into the Gateway module
     * directory.
     */
    private static void applyThirdPartyModules(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        for (Path module : specification.thirdPartyModules()) {
            plan.copy(module, moduleDestination(module), ContainerFileCopy.DEFAULT_MODE);
        }
    }

    /**
     * Configures the user and group IDs used by the Ignition process.
     */
    private static void applyProcessIdentity(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_UID", specification.uid().toString());

        plan.environment("IGNITION_GID", specification.gid().toString());
    }

    /**
     * Configures whether the Gateway Quick Start experience is enabled.
     */
    private static void applyQuickStartControl(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        boolean disableQuickStart = !specification.quickStartEnabled();

        plan.environment("DISABLE_QUICKSTART", Boolean.toString(disableQuickStart));
    }

    /**
     * Converts a Gateway edition into the lowercase identifier expected by
     * the Ignition Docker image.
     */
    private static String editionIdentifier(IgnitionGatewayEdition edition) {
        return edition.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the target path for a third-party module inside the container.
     */
    private static String moduleDestination(Path module) {
        Path fileName = module.getFileName();

        if (fileName == null) {
            throw new IllegalArgumentException("Module path does not have a filename: " + module);
        }

        return MODULE_DIRECTORY + "/" + fileName;
    }
}
