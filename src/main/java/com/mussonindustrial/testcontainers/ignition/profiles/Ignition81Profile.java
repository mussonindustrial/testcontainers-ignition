package com.mussonindustrial.testcontainers.ignition.profiles;

import static com.mussonindustrial.testcontainers.ignition.IgnitionCapability.*;
import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.*;
import static com.mussonindustrial.testcontainers.ignition.compatibility.ModuleDefinition.module;

import com.mussonindustrial.testcontainers.ignition.*;
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
 * Docker API profile for the Ignition 8.1 release line.
 *
 * <p>This profile declares the capabilities, modules, endpoints, and
 * container configuration behavior supported by Ignition 8.1 images.
 */
public final class Ignition81Profile implements IgnitionProfile {

    /** Human-readable name for this profile. */
    private static final String PROFILE_NAME = "Ignition 8.1";

    /** Root installation directory in the Ignition 8.1 Docker image. */
    private static final String INSTALLATION_DIRECTORY = "/usr/local/bin/ignition";

    /** Directory where third-party module files are installed. */
    private static final String MODULE_DIRECTORY = INSTALLATION_DIRECTORY + "/user-lib/modules";

    /** Container path used for a staged Gateway backup. */
    private static final String RESTORE_PATH = "/restore.gwbk";

    /**
     * Capabilities provided by the Ignition 8.1 Docker image and the first
     * patch release in which each capability is available.
     */
    private static final CapabilityMatrix CAPABILITIES = CapabilityMatrix.profile(PROFILE_NAME)
            .supported(GATEWAY_NAME, "8.1.0")
            .supported(DEBUG_MODE, "8.1.0")
            .supported(MAX_MEMORY, "8.1.0")
            .supported(THIRD_PARTY_MODULE_INSTALLATION, "8.1.0")
            .supported(LICENSE_ACCEPTANCE, "8.1.7")
            .supported(GATEWAY_RESTORE, "8.1.7")
            .supported(INITIAL_ADMIN_CONFIGURATION, "8.1.8")
            .supported(GATEWAY_EDITION, "8.1.8")
            .supported(LEASED_LICENSE_ACTIVATION, "8.1.8")
            .supported(SUPPLEMENTAL_ARGUMENTS, "8.1.8")
            .supported(BUILT_IN_MODULE_SELECTION, "8.1.17")
            .supported(PROCESS_IDENTITY, "8.1.17")
            .supported(QUICK_START_CONTROL, "8.1.23")
            .complete();

    /**
     * Network endpoints available from the Ignition 8.1 Docker image.
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
     * Built-in modules available from the Ignition 8.1 Docker image.
     *
     * <p>Each entry maps a stable logical module to its Ignition 8.1 image
     * identifier and any endpoints contributed by that module.
     */
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
            .supported(IgnitionModule.OPC_UA, "8.1.17", module("opc-ua").exposes(IgnitionEndpoint.OPC_UA))
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

    /**
     * Functions that translate requested capabilities into the Ignition 8.1
     * Docker image configuration.
     */
    private static final Map<IgnitionCapability, CapabilityApplier> APPLIERS = Map.ofEntries(
            Map.entry(LICENSE_ACCEPTANCE, Ignition81Profile::applyLicenseAcceptance),
            Map.entry(LEASED_LICENSE_ACTIVATION, Ignition81Profile::applyLeasedLicenseActivation),
            Map.entry(INITIAL_ADMIN_CONFIGURATION, Ignition81Profile::applyInitialAdminConfiguration),
            Map.entry(GATEWAY_NAME, Ignition81Profile::applyGatewayName),
            Map.entry(GATEWAY_EDITION, Ignition81Profile::applyGatewayEdition),
            Map.entry(GATEWAY_RESTORE, Ignition81Profile::applyGatewayRestore),
            Map.entry(DEBUG_MODE, Ignition81Profile::applyDebugMode),
            Map.entry(MAX_MEMORY, Ignition81Profile::applyMaxMemory),
            Map.entry(SUPPLEMENTAL_ARGUMENTS, Ignition81Profile::applySupplementalArguments),
            Map.entry(BUILT_IN_MODULE_SELECTION, Ignition81Profile::applyBuiltInModuleSelection),
            Map.entry(THIRD_PARTY_MODULE_INSTALLATION, Ignition81Profile::applyThirdPartyModules),
            Map.entry(PROCESS_IDENTITY, Ignition81Profile::applyProcessIdentity),
            Map.entry(QUICK_START_CONTROL, Ignition81Profile::applyQuickStartControl));

    @Override
    public String name() {
        return PROFILE_NAME;
    }

    @Override
    public boolean matches(IgnitionVersion version) {
        return version.isReleaseLine(8, 1);
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
     * Resolves the requested logical modules into Ignition 8.1 identifiers
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
