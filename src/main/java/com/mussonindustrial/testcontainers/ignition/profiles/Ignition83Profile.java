package com.mussonindustrial.testcontainers.ignition.profiles;

import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.*;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.DEBUG;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.GATEWAY_HTTP;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.GATEWAY_HTTPS;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.GATEWAY_NETWORK;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.OPC_UA;
import static com.mussonindustrial.testcontainers.ignition.compatibility.ModuleDefinition.module;

import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import com.mussonindustrial.testcontainers.ignition.ThirdPartyModule;
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilityCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.EndpointCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.ModuleCatalog;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerFileCopy;
import com.mussonindustrial.testcontainers.ignition.internal.ContainerPlan;
import com.mussonindustrial.testcontainers.ignition.internal.IgnitionContainerSpec;
import java.util.LinkedHashSet;

/**
 * Docker API profile for the Ignition 8.3 release line.
 *
 * <p>This profile defines the capabilities, modules, endpoints, and container
 * configuration supported by Ignition 8.3 images.
 */
public final class Ignition83Profile extends BaseIgnition8Profile {

    /** Human-readable profile name. */
    private static final String PROFILE_NAME = "Ignition 8.3";

    /** Supported capabilities and their versioned appliers. */
    private final CapabilityCatalog CAPABILITIES = CapabilityCatalog.profile(PROFILE_NAME)
            .supported(LICENSE_ACCEPTANCE, "8.3.0", this::applyLicenseAcceptance)
            .supported(LEASED_LICENSE_ACTIVATION, "8.3.0", this::applyLeasedLicenseActivation)
            .supported(INITIAL_ADMIN_CONFIGURATION, "8.3.0", this::applyInitialAdminConfiguration)
            .supported(GATEWAY_NAME, "8.3.0", this::applyGatewayName)
            .supported(GATEWAY_EDITION, "8.3.0", this::applyGatewayEdition)
            .supported(GATEWAY_RESTORE, "8.3.0", this::applyGatewayRestore)
            .supported(DEBUG_MODE, "8.3.0", this::applyDebugMode)
            .implementedBy(UNSIGNED_MODULES, "8.3.0", SUPPLEMENTAL_ARGUMENTS)
            .supported(MAX_MEMORY, "8.3.0", this::applyMaxMemory)
            .supported(SUPPLEMENTAL_ARGUMENTS, "8.3.0", this::applySupplementalArguments)
            .supported(BUILT_IN_MODULE_SELECTION, "8.3.0", this::applyBuiltInModuleSelection)
            .supported(THIRD_PARTY_MODULE_INSTALLATION, "8.3.0", this::applyThirdPartyModules)
            .supported(PROCESS_IDENTITY, "8.3.0", this::applyProcessIdentity)
            .supported(QUICK_START_CONTROL, "8.3.0", this::applyQuickStartControl)
            .complete();

    /** Network endpoints defined by Ignition 8.3 images. */
    private final EndpointCatalog ENDPOINTS = EndpointCatalog.profile(PROFILE_NAME)
            .endpoint(GATEWAY_HTTP, 8088, "Gateway HTTP")
            .endpoint(GATEWAY_HTTPS, 8043, "Gateway HTTPS")
            .endpoint(GATEWAY_NETWORK, 8060, "Gateway Network")
            .endpoint(OPC_UA, 62541, "OPC UA server and discovery")
            .endpoint(DEBUG, 8000, "Gateway JVM debugger")
            .complete();

    /** Built-in modules available from Ignition 8.3 images. */
    private final ModuleCatalog MODULES = ModuleCatalog.profile(PROFILE_NAME)
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
                    module("com.inductiveautomation.opcua").exposes(OPC_UA))
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

    /** Returns the profile name. */
    @Override
    public String name() {
        return PROFILE_NAME;
    }

    /** Returns whether the version belongs to the 8.3 release line. */
    @Override
    public boolean matches(IgnitionVersion version) {
        return version.isReleaseLine(8, 3);
    }

    /** Returns the capability catalog. */
    @Override
    public CapabilityCatalog capabilities() {
        return CAPABILITIES;
    }

    /** Returns the built-in module catalog. */
    @Override
    public ModuleCatalog modules() {
        return MODULES;
    }

    /** Returns the endpoint catalog. */
    @Override
    public EndpointCatalog endpoints() {
        return ENDPOINTS;
    }

    /**
     * Applies built-in and third-party module selection.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyBuiltInModuleSelection(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        ModuleCatalog.Resolution resolution = MODULES.resolve(version, specification.modules());

        resolution.warnings().forEach(plan::warning);

        LinkedHashSet<String> identifiers = new LinkedHashSet<>(resolution.identifiers());

        for (ThirdPartyModule module : specification.thirdPartyModules()) {
            identifiers.addAll(module.gatewayDependencies());
            identifiers.add(module.identifier());
        }

        plan.environment("GATEWAY_MODULES_ENABLED", String.join(",", identifiers));

        resolution.endpoints().stream().mapToInt(ENDPOINTS::port).forEach(plan::expose);
    }

    /**
     * Installs third-party modules and accepts their certificates.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyThirdPartyModules(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        LinkedHashSet<String> acceptedCertificates = new LinkedHashSet<>();

        for (ThirdPartyModule module : specification.thirdPartyModules()) {
            plan.copy(module.archive(), moduleDestination(module.archive()), ContainerFileCopy.DEFAULT_MODE);
            acceptedCertificates.add(module.identifier());
        }

        if (!acceptedCertificates.isEmpty()) {
            plan.environment("ACCEPT_MODULE_CERTS", String.join(",", acceptedCertificates));
            plan.environment("ACCEPT_MODULE_LICENSES", String.join(",", acceptedCertificates));
        }
    }
}
