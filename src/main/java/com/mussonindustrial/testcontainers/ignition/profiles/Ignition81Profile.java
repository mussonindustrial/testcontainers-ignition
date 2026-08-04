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
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilityCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.EndpointCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.ModuleCatalog;

/**
 * Docker API profile for the Ignition 8.1 release line.
 *
 * <p>This profile defines the capabilities, modules, endpoints, and container
 * configuration supported by Ignition 8.1 images.
 */
public final class Ignition81Profile extends BaseIgnition8Profile {

    /** Human-readable profile name. */
    private static final String PROFILE_NAME = "Ignition 8.1";

    /** Supported capabilities and their versioned appliers. */
    private final CapabilityCatalog CAPABILITIES = CapabilityCatalog.profile(PROFILE_NAME)
            .supported(GATEWAY_NAME, "8.1.0", this::applyGatewayName)
            .supported(DEBUG_MODE, "8.1.0", this::applyDebugMode)
            .supported(MAX_MEMORY, "8.1.0", this::applyMaxMemory)
            .supported(THIRD_PARTY_MODULE_INSTALLATION, "8.1.0", this::applyThirdPartyModules)
            .supported(LICENSE_ACCEPTANCE, "8.1.7", this::applyLicenseAcceptance)
            .supported(GATEWAY_RESTORE, "8.1.7", this::applyGatewayRestore)
            .supported(INITIAL_ADMIN_CONFIGURATION, "8.1.8", this::applyInitialAdminConfiguration)
            .supported(GATEWAY_EDITION, "8.1.8", this::applyGatewayEdition)
            .supported(LEASED_LICENSE_ACTIVATION, "8.1.8", this::applyLeasedLicenseActivation)
            .supported(SUPPLEMENTAL_ARGUMENTS, "8.1.8", this::applySupplementalArguments)
            .implementedBy(UNSIGNED_MODULES, "8.1.8", SUPPLEMENTAL_ARGUMENTS)
            .supported(BUILT_IN_MODULE_SELECTION, "8.1.17", this::applyBuiltInModuleSelection)
            .supported(PROCESS_IDENTITY, "8.1.17", this::applyProcessIdentity)
            .supported(QUICK_START_CONTROL, "8.1.23", this::applyQuickStartControl)
            .complete();

    /** Network endpoints defined by Ignition 8.1 images. */
    private final EndpointCatalog ENDPOINTS = EndpointCatalog.profile(PROFILE_NAME)
            .endpoint(GATEWAY_HTTP, 8088, "Gateway HTTP")
            .endpoint(GATEWAY_HTTPS, 8043, "Gateway HTTPS")
            .endpoint(GATEWAY_NETWORK, 8060, "Gateway Network")
            .endpoint(OPC_UA, 62541, "OPC UA server and discovery")
            .endpoint(DEBUG, 8000, "Gateway JVM debugger")
            .complete();

    /** Built-in modules available from Ignition 8.1 images. */
    private final ModuleCatalog MODULES = ModuleCatalog.profile(PROFILE_NAME)
            .supported(IgnitionModule.ALARM_NOTIFICATION, "8.1.17", module("alarm-notification"))
            .supported(IgnitionModule.ALLEN_BRADLEY_LEGACY_DRIVER, "8.1.17", module("allen-bradley-drivers"))
            .supported(IgnitionModule.BACNET_DRIVER, "8.1.17", module("bacnet-driver"))
            .supported(IgnitionModule.DNP3_DRIVER, "8.1.17", module("dnp3-driver"))
            .supported(IgnitionModule.DNP3_DRIVER_V2, "8.1.36", module("dnp3-driver-v2"))
            .supported(IgnitionModule.ENTERPRISE_ADMINISTRATION, "8.1.17", module("enterprise-administration"))
            .supported(IgnitionModule.SQL_HISTORIAN, "8.1.17", module("tag-historian"))
            .supported(IgnitionModule.IEC_61850_DRIVER, "8.1.25", module("iec-61850-driver"))
            .supported(IgnitionModule.LOGIX_DRIVER, "8.1.17", module("logix-driver"))
            .supported(IgnitionModule.MICRO800_DRIVER, "8.1.39", module("micro800-driver"))
            .supported(IgnitionModule.MITSUBISHI_DRIVER, "8.1.17", module("mitsubishi-driver"))
            .supported(IgnitionModule.MODBUS_DRIVER_V2, "8.1.17", module("modbus-driver-v2"))
            .supported(IgnitionModule.OMRON_DRIVER, "8.1.17", module("omron-driver"))
            .supported(IgnitionModule.OPC_UA, "8.1.17", module("opc-ua").exposes(OPC_UA))
            .supported(IgnitionModule.PERSPECTIVE, "8.1.17", module("perspective"))
            .supported(IgnitionModule.REPORTING, "8.1.17", module("reporting"))
            .supported(IgnitionModule.SERIAL_SUPPORT_CLIENT, "8.1.17", module("serial-support-client"))
            .supported(IgnitionModule.SERIAL_SUPPORT_GATEWAY, "8.1.17", module("serial-support-gateway"))
            .supported(IgnitionModule.SFC, "8.1.17", module("sfc"))
            .supported(IgnitionModule.SIEMENS_DRIVER, "8.1.17", module("siemens-drivers"))
            .supported(IgnitionModule.SMS_NOTIFICATION, "8.1.17", module("sms-notification"))
            .supported(IgnitionModule.SQL_BRIDGE, "8.1.17", module("sql-bridge"))
            .supported(IgnitionModule.SYMBOL_FACTORY, "8.1.17", module("symbol-factory"))
            .supported(IgnitionModule.UDP_TCP_DRIVERS, "8.1.17", module("udp-tcp-drivers"))
            .supported(IgnitionModule.VISION, "8.1.17", module("vision"))
            .supported(IgnitionModule.VOICE_NOTIFICATION, "8.1.17", module("voice-notification"))
            .supported(IgnitionModule.WEB_BROWSER, "8.1.17", module("web-browser"))
            .supported(IgnitionModule.WEB_DEV, "8.1.17", module("web-developer"))
            .complete();

    /** Returns the profile name. */
    @Override
    public String name() {
        return PROFILE_NAME;
    }

    /** Returns whether the version belongs to the 8.1 release line. */
    @Override
    public boolean matches(IgnitionVersion version) {
        return version.isReleaseLine(8, 1);
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
}
