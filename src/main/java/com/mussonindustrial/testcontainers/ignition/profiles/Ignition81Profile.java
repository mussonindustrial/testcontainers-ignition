package com.mussonindustrial.testcontainers.ignition.profiles;

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
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.DEBUG;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.GATEWAY_HTTP;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.GATEWAY_HTTPS;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.GATEWAY_NETWORK;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.OPC_UA;
import static com.mussonindustrial.testcontainers.ignition.compatibility.ModuleDefinition.module;

import com.mussonindustrial.testcontainers.ignition.IgnitionGatewayEdition;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import com.mussonindustrial.testcontainers.ignition.ThirdPartyModule;
import com.mussonindustrial.testcontainers.ignition.compatibility.CapabilityCatalog;
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

/**
 * Docker API profile for the Ignition 8.1 release line.
 *
 * <p>This profile defines the capabilities, modules, endpoints, and container
 * configuration supported by Ignition 8.1 images.
 */
public final class Ignition81Profile implements IgnitionProfile {

    /** Human-readable profile name. */
    private static final String PROFILE_NAME = "Ignition 8.1";

    /** Root installation directory. */
    private static final String INSTALLATION_DIRECTORY = "/usr/local/bin/ignition";

    /** Third-party module directory. */
    private static final String MODULE_DIRECTORY = INSTALLATION_DIRECTORY + "/user-lib/modules";

    /** Staged Gateway backup path. */
    private static final String RESTORE_PATH = "/restore.gwbk";

    /** Supported capabilities and their versioned appliers. */
    private static final CapabilityCatalog CAPABILITIES = CapabilityCatalog.profile(PROFILE_NAME)
            .supported(GATEWAY_NAME, "8.1.0", Ignition81Profile::applyGatewayName)
            .supported(DEBUG_MODE, "8.1.0", Ignition81Profile::applyDebugMode)
            .supported(MAX_MEMORY, "8.1.0", Ignition81Profile::applyMaxMemory)
            .supported(THIRD_PARTY_MODULE_INSTALLATION, "8.1.0", Ignition81Profile::applyThirdPartyModules)
            .supported(LICENSE_ACCEPTANCE, "8.1.7", Ignition81Profile::applyLicenseAcceptance)
            .supported(GATEWAY_RESTORE, "8.1.7", Ignition81Profile::applyGatewayRestore)
            .supported(INITIAL_ADMIN_CONFIGURATION, "8.1.8", Ignition81Profile::applyInitialAdminConfiguration)
            .supported(GATEWAY_EDITION, "8.1.8", Ignition81Profile::applyGatewayEdition)
            .supported(LEASED_LICENSE_ACTIVATION, "8.1.8", Ignition81Profile::applyLeasedLicenseActivation)
            .supported(SUPPLEMENTAL_ARGUMENTS, "8.1.8", Ignition81Profile::applySupplementalArguments)
            .supported(BUILT_IN_MODULE_SELECTION, "8.1.17", Ignition81Profile::applyBuiltInModuleSelection)
            .supported(PROCESS_IDENTITY, "8.1.17", Ignition81Profile::applyProcessIdentity)
            .supported(QUICK_START_CONTROL, "8.1.23", Ignition81Profile::applyQuickStartControl)
            .complete();

    /** Network endpoints defined by Ignition 8.1 images. */
    private static final EndpointCatalog ENDPOINTS = EndpointCatalog.profile(PROFILE_NAME)
            .endpoint(GATEWAY_HTTP, 8088, "Gateway HTTP")
            .endpoint(GATEWAY_HTTPS, 8043, "Gateway HTTPS")
            .endpoint(GATEWAY_NETWORK, 8060, "Gateway Network")
            .endpoint(OPC_UA, 62541, "OPC UA server and discovery")
            .endpoint(DEBUG, 8000, "Gateway JVM debugger")
            .complete();

    /** Built-in modules available from Ignition 8.1 images. */
    private static final ModuleCatalog MODULES = ModuleCatalog.profile(PROFILE_NAME)
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

    /** Applies profile-wide defaults. */
    @Override
    public void applyDefaults(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("TZ", specification.timezone());

        plan.expose(ENDPOINTS.port(GATEWAY_HTTP), ENDPOINTS.port(GATEWAY_HTTPS), ENDPOINTS.port(GATEWAY_NETWORK));
    }

    /** Applies license acceptance. */
    private static void applyLicenseAcceptance(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        if (specification.licenseAccepted()) {
            plan.environment("ACCEPT_IGNITION_EULA", "Y");
        }
    }

    /** Applies leased-license activation. */
    private static void applyLeasedLicenseActivation(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_LICENSE_KEY", specification.licenseKey());

        plan.environment("IGNITION_ACTIVATION_TOKEN", specification.activationToken());
    }

    /** Applies initial administrator credentials. */
    private static void applyInitialAdminConfiguration(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        GatewayCredentials credentials = specification.credentials();

        plan.environment("GATEWAY_ADMIN_USERNAME", credentials.username());

        plan.environment("GATEWAY_ADMIN_PASSWORD", credentials.password());
    }

    /** Applies the Gateway name. */
    private static void applyGatewayName(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.arguments("-n", specification.gatewayName());
    }

    /** Applies the Gateway edition. */
    private static void applyGatewayEdition(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_EDITION", editionIdentifier(specification.edition()));
    }

    /** Applies Gateway backup restoration. */
    private static void applyGatewayRestore(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        GatewayRestore restore = specification.gatewayRestore();

        plan.copy(restore.backup(), RESTORE_PATH, ContainerFileCopy.READ_ONLY_MODE);

        plan.arguments("-r", RESTORE_PATH);

        plan.environment("GATEWAY_RESTORE_DISABLED", Boolean.toString(restore.restoreDisabled()));
    }

    /** Applies Gateway JVM debugging. */
    private static void applyDebugMode(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        if (!specification.debugMode()) {
            return;
        }

        plan.argument("-d");
        plan.expose(ENDPOINTS.port(DEBUG));
    }

    /** Applies the maximum Gateway memory. */
    private static void applyMaxMemory(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.arguments("-m", specification.maxMemory());
    }

    /** Applies supplemental startup arguments. */
    private static void applySupplementalArguments(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        List<String> arguments = specification.additionalArguments();

        if (arguments.isEmpty()) {
            return;
        }

        plan.argument("--");
        plan.arguments(arguments);
    }

    /** Applies built-in module selection. */
    private static void applyBuiltInModuleSelection(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        ModuleCatalog.Resolution resolution = MODULES.resolve(version, specification.modules());
        resolution.warnings().forEach(plan::warning);

        plan.environment("GATEWAY_MODULES_ENABLED", String.join(",", resolution.identifiers()));

        resolution.endpoints().stream().mapToInt(ENDPOINTS::port).forEach(plan::expose);
    }

    /** Installs third-party modules. */
    private static void applyThirdPartyModules(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        for (ThirdPartyModule module : specification.thirdPartyModules()) {
            plan.copy(module.archive(), moduleDestination(module.archive()), ContainerFileCopy.DEFAULT_MODE);
        }
    }

    /** Applies the Ignition process identity. */
    private static void applyProcessIdentity(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_UID", specification.uid().toString());

        plan.environment("IGNITION_GID", specification.gid().toString());
    }

    /** Applies Gateway Quick Start control. */
    private static void applyQuickStartControl(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        boolean disableQuickStart = !specification.quickStartEnabled();

        plan.environment("DISABLE_QUICKSTART", Boolean.toString(disableQuickStart));
    }

    /** Returns the Docker edition identifier. */
    private static String editionIdentifier(IgnitionGatewayEdition edition) {
        return edition.name().toLowerCase(Locale.ROOT);
    }

    /** Returns the container destination for a module. */
    private static String moduleDestination(Path module) {
        Path fileName = module.getFileName();

        if (fileName == null) {
            throw new IllegalArgumentException("Module path does not have a filename: " + module);
        }

        return MODULE_DIRECTORY + "/" + fileName;
    }
}
