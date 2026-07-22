package com.mussonindustrial.testcontainers.ignition.internal;

import com.mussonindustrial.testcontainers.ignition.IgnitionCapability;
import com.mussonindustrial.testcontainers.ignition.IgnitionGatewayEdition;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import com.mussonindustrial.testcontainers.ignition.ThirdPartyModule;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Stores the normalized configuration requested for an Ignition container.
 *
 * <p>Version-specific behavior is applied later by an Ignition profile.
 */
public final class IgnitionContainerSpec {

    /** Capabilities explicitly requested by the user. */
    private final EnumSet<IgnitionCapability> requestedCapabilities = EnumSet.of(IgnitionCapability.GATEWAY_EDITION);

    /** Whether the Ignition license agreement was accepted. */
    private boolean licenseAccepted;

    /** Token used to activate a leased license. */
    private String activationToken;

    /** Key used to activate a leased license. */
    private String licenseKey;

    /** Initial Gateway administrator credentials. */
    private GatewayCredentials credentials;

    /** Gateway backup restoration configuration. */
    private GatewayRestore gatewayRestore;

    /** Requested Gateway name. */
    private String gatewayName;

    /** Requested Gateway edition. */
    private IgnitionGatewayEdition edition = IgnitionGatewayEdition.STANDARD;

    /** Whether Gateway JVM debugging is enabled. */
    private boolean debugMode;

    /** Whether unsigned modules are allowed. */
    private boolean allowUnsignedModules;

    /** Requested maximum Gateway memory. */
    private String maxMemory;

    /**
     * Explicitly selected built-in modules.
     *
     * <p>The requested-capability flag distinguishes an empty selection from
     * a selection that was never configured.
     */
    private Set<IgnitionModule> modules = Set.of();

    /** Parsed third-party modules keyed by the archive path. */
    private final Map<Path, ThirdPartyModule> thirdPartyModules = new LinkedHashMap<>();

    /** User ID used by the Ignition process. */
    private Integer uid;

    /** Group ID used by the Ignition process. */
    private Integer gid;

    /** Requested Quick Start state, or {@code null} when unspecified. */
    private Boolean quickStartEnabled;

    /** Container timezone. */
    private String timezone = "Etc/UTC";

    /** Supplemental container startup arguments. */
    private List<String> additionalArguments = List.of();

    /**
     * Records a requested capability.
     *
     * @param capability requested capability
     */
    public void request(IgnitionCapability capability) {
        requestedCapabilities.add(Objects.requireNonNull(capability, "capability"));
    }

    /**
     * Returns whether a capability was requested.
     *
     * @param capability capability to inspect
     * @return {@code true} when requested
     */
    public boolean isRequested(IgnitionCapability capability) {
        return requestedCapabilities.contains(Objects.requireNonNull(capability, "capability"));
    }

    /**
     * Returns the requested capabilities.
     *
     * @return immutable capability set
     */
    public Set<IgnitionCapability> requestedCapabilities() {
        return Collections.unmodifiableSet(EnumSet.copyOf(requestedCapabilities));
    }

    /** Records acceptance of the Ignition license agreement. */
    public void acceptLicense() {
        licenseAccepted = true;
    }

    /**
     * Returns whether the license agreement was accepted.
     *
     * @return license acceptance state
     */
    public boolean licenseAccepted() {
        return licenseAccepted;
    }

    /**
     * Sets the leased-license activation token.
     *
     * @param activationToken activation token
     */
    public void activationToken(String activationToken) {
        this.activationToken = requireNonBlank(activationToken, "activationToken");
    }

    /**
     * Returns the leased-license activation token.
     *
     * @return activation token
     */
    public String activationToken() {
        return activationToken;
    }

    /**
     * Sets the leased-license key.
     *
     * @param licenseKey license key
     */
    public void licenseKey(String licenseKey) {
        this.licenseKey = requireNonBlank(licenseKey, "licenseKey");
    }

    /**
     * Returns the leased-license key.
     *
     * @return license key
     */
    public String licenseKey() {
        return licenseKey;
    }

    /**
     * Sets the initial Gateway administrator credentials.
     *
     * @param username administrator username
     * @param password administrator password
     */
    public void credentials(String username, String password) {
        credentials =
                new GatewayCredentials(requireNonBlank(username, "username"), requireNonBlank(password, "password"));
    }

    /**
     * Returns the initial Gateway administrator credentials.
     *
     * @return administrator credentials
     */
    public GatewayCredentials credentials() {
        return credentials;
    }

    /**
     * Configures Gateway backup restoration.
     *
     * @param backup Gateway backup file
     * @param restoreDisabled whether disabled resources remain disabled
     */
    public void gatewayRestore(Path backup, boolean restoreDisabled) {
        gatewayRestore = new GatewayRestore(Objects.requireNonNull(backup, "backup"), restoreDisabled);
    }

    /**
     * Returns the Gateway restoration configuration.
     *
     * @return restoration configuration
     */
    public GatewayRestore gatewayRestore() {
        return gatewayRestore;
    }

    /**
     * Sets the Gateway name.
     *
     * @param gatewayName Gateway name
     */
    public void gatewayName(String gatewayName) {
        this.gatewayName = requireNonBlank(gatewayName, "gatewayName");
    }

    /**
     * Returns the Gateway name.
     *
     * @return Gateway name
     */
    public String gatewayName() {
        return gatewayName;
    }

    /**
     * Sets the Gateway edition.
     *
     * @param edition Gateway edition
     */
    public void edition(IgnitionGatewayEdition edition) {
        this.edition = Objects.requireNonNull(edition, "edition");
    }

    /**
     * Returns the Gateway edition.
     *
     * @return Gateway edition
     */
    public IgnitionGatewayEdition edition() {
        return edition;
    }

    /**
     * Sets whether Gateway JVM debugging is enabled.
     *
     * @param debugMode debug state
     */
    public void debugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Returns whether Gateway JVM debugging is enabled.
     *
     * @return debug state
     */
    public boolean debugMode() {
        return debugMode;
    }

    /**
     * Sets whether unsigned modules are allowed.
     *
     * @param allowUnsignedModules unsigned modules state
     */
    public void allowUnsignedModules(boolean allowUnsignedModules) {
        this.allowUnsignedModules = allowUnsignedModules;
    }

    /**
     * Returns whether unsigned modules are allowed.
     *
     * @return unsigned modules state
     */
    public boolean allowUnsignedModules() {
        return allowUnsignedModules;
    }

    /**
     * Sets the maximum Gateway memory.
     *
     * @param maxMemory maximum memory value
     */
    public void maxMemory(String maxMemory) {
        this.maxMemory = requireNonBlank(maxMemory, "maxMemory");
    }

    /**
     * Returns the maximum Gateway memory.
     *
     * @return maximum memory value
     */
    public String maxMemory() {
        return maxMemory;
    }

    /**
     * Sets the built-in module selection.
     *
     * @param modules selected modules
     */
    public void modules(Set<IgnitionModule> modules) {
        Objects.requireNonNull(modules, "modules");

        LinkedHashSet<IgnitionModule> copy = new LinkedHashSet<>();

        for (IgnitionModule module : modules) {
            copy.add(Objects.requireNonNull(module, "modules cannot contain null"));
        }

        this.modules = Collections.unmodifiableSet(copy);
    }

    /**
     * Returns the selected built-in modules.
     *
     * @return immutable module set
     */
    public Set<IgnitionModule> modules() {
        return modules;
    }

    /**
     * Adds a third-party module archive.
     *
     * <p>The module descriptor is read immediately.
     *
     * @param archive module archive
     * @throws FileNotFoundException if a module cannot be read
     */
    public void addThirdPartyModule(Path archive) throws FileNotFoundException {
        addThirdPartyModule(ThirdPartyModule.read(archive));
    }

    /**
     * Adds a parsed third-party module.
     *
     * <p>A module with the same archive path replaces the previous entry
     * without changing its position.
     *
     * @param module third-party module
     */
    public void addThirdPartyModule(ThirdPartyModule module) {
        Objects.requireNonNull(module, "module");

        thirdPartyModules.put(module.archive(), module);
    }

    /**
     * Replaces the configured third-party modules.
     *
     * @param modules parsed third-party modules
     */
    public void thirdPartyModules(Iterable<ThirdPartyModule> modules) {
        Objects.requireNonNull(modules, "modules");

        Map<Path, ThirdPartyModule> replacements = new LinkedHashMap<>();

        for (ThirdPartyModule module : modules) {
            ThirdPartyModule validated = Objects.requireNonNull(module, "modules cannot contain null");

            replacements.put(validated.archive(), validated);
        }

        thirdPartyModules.clear();
        thirdPartyModules.putAll(replacements);
    }

    /**
     * Returns the configured third-party modules.
     *
     * @return immutable module list
     */
    public List<ThirdPartyModule> thirdPartyModules() {
        return List.copyOf(thirdPartyModules.values());
    }

    /**
     * Sets the Ignition process user ID.
     *
     * @param uid process user ID
     */
    public void uid(int uid) {
        if (uid < 0) {
            throw new IllegalArgumentException("UID cannot be negative: " + uid);
        }

        this.uid = uid;
    }

    /**
     * Returns the Ignition process user ID.
     *
     * @return process user ID
     */
    public Integer uid() {
        return uid;
    }

    /**
     * Sets the Ignition process group ID.
     *
     * @param gid process group ID
     */
    public void gid(int gid) {
        if (gid < 0) {
            throw new IllegalArgumentException("GID cannot be negative: " + gid);
        }

        this.gid = gid;
    }

    /**
     * Returns the Ignition process group ID.
     *
     * @return process group ID
     */
    public Integer gid() {
        return gid;
    }

    /**
     * Sets whether Gateway Quick Start is enabled.
     *
     * @param quickStartEnabled Quick Start state
     */
    public void quickStartEnabled(boolean quickStartEnabled) {
        this.quickStartEnabled = quickStartEnabled;
    }

    /**
     * Returns the requested Gateway Quick Start state.
     *
     * @return Quick Start state, or {@code null} when unspecified
     */
    public Boolean quickStartEnabled() {
        return quickStartEnabled;
    }

    /**
     * Sets the container timezone.
     *
     * @param timezone timezone identifier
     */
    public void timezone(String timezone) {
        this.timezone = requireNonBlank(timezone, "timezone");
    }

    /**
     * Returns the container timezone.
     *
     * @return timezone identifier
     */
    public String timezone() {
        return timezone;
    }

    /**
     * Sets the supplemental startup arguments.
     *
     * @param additionalArguments startup arguments
     */
    public void additionalArguments(List<String> additionalArguments) {
        Objects.requireNonNull(additionalArguments, "additionalArguments");

        for (String argument : additionalArguments) {
            Objects.requireNonNull(argument, "additionalArguments cannot contain null");

            if (argument.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Additional arguments cannot contain " + "a null character");
            }
        }

        this.additionalArguments = List.copyOf(additionalArguments);
    }

    /**
     * Returns the supplemental startup arguments.
     *
     * @return immutable argument list
     */
    public List<String> additionalArguments() {
        return additionalArguments;
    }

    /**
     * Validates the requested configuration.
     *
     * <p>Version compatibility is handled separately by the selected profile.
     */
    public void validate() {
        requireNonBlank(timezone, "timezone");

        for (IgnitionCapability capability : requestedCapabilities) {
            validateRequestedCapability(capability);
        }
    }

    /** Validates the configuration required by one capability. */
    private void validateRequestedCapability(IgnitionCapability capability) {
        switch (capability) {
            case LICENSE_ACCEPTANCE ->
                requireConfigured(licenseAccepted, capability, "The Ignition license was not accepted");

            case LEASED_LICENSE_ACTIVATION -> {
                requireConfigured(licenseKey != null, capability, "A license key was not configured");

                requireConfigured(activationToken != null, capability, "An activation token was not configured");
            }

            case INITIAL_ADMIN_CONFIGURATION ->
                requireConfigured(
                        credentials != null, capability, "Gateway administrator credentials " + "were not configured");

            case GATEWAY_RESTORE -> {
                requireConfigured(gatewayRestore != null, capability, "A Gateway backup was not configured");

                requireConfigured(
                        gatewayRestore == null || Files.isRegularFile(gatewayRestore.backup()),
                        capability,
                        "Gateway backup does not exist: "
                                + (gatewayRestore == null ? "<not configured>" : gatewayRestore.backup()));
            }

            case GATEWAY_NAME ->
                requireConfigured(gatewayName != null, capability, "A Gateway name was not configured");

            case GATEWAY_EDITION ->
                requireConfigured(edition != null, capability, "A Gateway edition was not configured");

            case DEBUG_MODE, UNSIGNED_MODULES -> {
                // Boolean configuration is always valid.
            }

            case MAX_MEMORY -> requireConfigured(maxMemory != null, capability, "Maximum memory was not configured");

            case BUILT_IN_MODULE_SELECTION ->
                requireConfigured(modules != null, capability, "The built-in module selection " + "was not configured");

            case THIRD_PARTY_MODULE_INSTALLATION -> {
                for (ThirdPartyModule module : thirdPartyModules.values()) {
                    requireConfigured(
                            Files.isRegularFile(module.archive()),
                            capability,
                            "Third-party module does not exist: " + module.archive());
                }
            }

            case PROCESS_IDENTITY -> {
                requireConfigured(uid != null, capability, "A process UID was not configured");

                requireConfigured(gid != null, capability, "A process GID was not configured");
            }

            case QUICK_START_CONTROL ->
                requireConfigured(quickStartEnabled != null, capability, "Quick Start behavior was not configured");

            case SUPPLEMENTAL_ARGUMENTS ->
                requireConfigured(
                        additionalArguments != null, capability, "Supplemental arguments were not configured");
        }
    }

    /**
     * Requires a capability-specific configuration condition.
     */
    private static void requireConfigured(boolean condition, IgnitionCapability capability, String message) {
        if (!condition) {
            throw new IllegalStateException(
                    "Invalid configuration for capability %s: %s".formatted(capability, message));
        }
    }

    /** Validates and normalizes a required string. */
    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }

        if (trimmed.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(name + " cannot contain a null character");
        }

        return trimmed;
    }
}
