package com.mussonindustrial.testcontainers.ignition.profiles;

import static com.mussonindustrial.testcontainers.ignition.IgnitionEndpoint.*;

import com.mussonindustrial.testcontainers.ignition.IgnitionGatewayEdition;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import com.mussonindustrial.testcontainers.ignition.ThirdPartyModule;
import com.mussonindustrial.testcontainers.ignition.compatibility.EndpointCatalog;
import com.mussonindustrial.testcontainers.ignition.compatibility.ModuleCatalog;
import com.mussonindustrial.testcontainers.ignition.internal.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Base Docker API profile for the Ignition 8 release line.
 */
public abstract class BaseIgnition8Profile implements IgnitionProfile {

    /** Root installation directory. */
    public static final String INSTALLATION_DIRECTORY = "/usr/local/bin/ignition";

    /** Third-party module directory. */
    public static final String MODULE_DIRECTORY = INSTALLATION_DIRECTORY + "/user-lib/modules";

    /** Staged Gateway backup path. */
    public static final String RESTORE_PATH = "/restore.gwbk";

    /**
     * Applies profile-wide defaults.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    @Override
    public void applyDefaults(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        EndpointCatalog endpoints = endpoints();

        plan.environment("TZ", specification.timezone());
        plan.expose(endpoints.port(GATEWAY_HTTP), endpoints.port(GATEWAY_HTTPS), endpoints.port(GATEWAY_NETWORK));
    }

    /**
     * Applies license acceptance.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyLicenseAcceptance(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        if (specification.licenseAccepted()) {
            plan.environment("ACCEPT_IGNITION_EULA", "Y");
        }
    }

    /**
     * Applies leased-license activation.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyLeasedLicenseActivation(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_LICENSE_KEY", specification.licenseKey());
        plan.environment("IGNITION_ACTIVATION_TOKEN", specification.activationToken());
    }

    /**
     * Applies initial administrator credentials.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyInitialAdminConfiguration(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        GatewayCredentials credentials = specification.credentials();

        plan.environment("GATEWAY_ADMIN_USERNAME", credentials.username());
        plan.environment("GATEWAY_ADMIN_PASSWORD", credentials.password());
    }

    /**
     * Applies the Gateway name.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyGatewayName(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.arguments("-n", specification.gatewayName());
    }

    /**
     * Applies the Gateway edition.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyGatewayEdition(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_EDITION", editionIdentifier(specification.edition()));
    }

    /**
     * Applies Gateway backup restoration.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyGatewayRestore(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        GatewayRestore restore = specification.gatewayRestore();

        plan.copy(restore.backup(), RESTORE_PATH, ContainerFileCopy.READ_ONLY_MODE);
        plan.arguments("-r", RESTORE_PATH);
        plan.environment("GATEWAY_RESTORE_DISABLED", Boolean.toString(restore.restoreDisabled()));
    }

    /**
     * Applies Gateway JVM debugging.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyDebugMode(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        if (!specification.debugMode()) {
            return;
        }

        plan.argument("-d");
        plan.expose(endpoints().port(DEBUG));
    }

    /**
     * Applies the maximum Gateway memory.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyMaxMemory(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.arguments("-m", specification.maxMemory());
    }

    /**
     * Applies supplemental startup arguments.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applySupplementalArguments(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        List<String> arguments = specification.additionalArguments();

        if (specification.allowUnsignedModules()) {
            arguments.add("-Dignition.allowunsignedmodules=true");
        }

        if (arguments.isEmpty()) {
            return;
        }

        plan.argument("--");
        plan.arguments(arguments);
    }

    /**
     * Applies built-in module selection.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyBuiltInModuleSelection(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        LinkedHashSet<IgnitionModule> builtInModules = new LinkedHashSet<>(specification.modules());
        specification.thirdPartyModules().forEach(module -> {
            module.gatewayDependencies().forEach(dependency -> {
                modules().find(dependency).ifPresent(builtInModules::add);
            });
        });

        ModuleCatalog.Resolution resolution = modules().resolve(version, builtInModules);
        resolution.warnings().forEach(plan::warning);

        plan.environment("GATEWAY_MODULES_ENABLED", String.join(",", resolution.identifiers()));

        resolution.endpoints().stream().mapToInt(endpoints()::port).forEach(plan::expose);
    }

    /**
     * Installs third-party modules.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyThirdPartyModules(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        for (ThirdPartyModule module : specification.thirdPartyModules()) {
            plan.copy(module.archive(), moduleDestination(module.archive()), ContainerFileCopy.DEFAULT_MODE);
        }
    }

    /**
     * Applies the Ignition process identity.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyProcessIdentity(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        plan.environment("IGNITION_UID", specification.uid().toString());
        plan.environment("IGNITION_GID", specification.gid().toString());
    }

    /**
     * Applies Gateway Quick Start control.
     *
     * @param version Ignition image version
     * @param specification container specification
     * @param plan container plan
     */
    protected void applyQuickStartControl(
            IgnitionVersion version, IgnitionContainerSpec specification, ContainerPlan.Builder plan) {
        boolean disableQuickStart = !specification.quickStartEnabled();

        plan.environment("DISABLE_QUICKSTART", Boolean.toString(disableQuickStart));
    }

    /**
     * Returns the Docker edition identifier.
     *
     * @param edition Gateway edition
     * @return edition identifier
     */
    protected String editionIdentifier(IgnitionGatewayEdition edition) {
        return edition.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the container destination for a module.
     *
     * @param module module path
     * @return module destination path
     */
    protected String moduleDestination(Path module) {
        Path fileName = module.getFileName();

        if (fileName == null) {
            throw new IllegalArgumentException("Module path does not have a filename: " + module);
        }

        return MODULE_DIRECTORY + "/" + fileName;
    }
}
