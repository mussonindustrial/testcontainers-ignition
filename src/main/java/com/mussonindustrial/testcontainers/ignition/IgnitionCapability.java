package com.mussonindustrial.testcontainers.ignition;

/**
 * Identifies a configurable Ignition container capability.
 */
public enum IgnitionCapability {

    /** Accepts the Ignition license agreement. */
    LICENSE_ACCEPTANCE,

    /** Activates a leased Ignition license. */
    LEASED_LICENSE_ACTIVATION,

    /** Configures the initial Gateway administrator account. */
    INITIAL_ADMIN_CONFIGURATION,

    /** Configures the Gateway name. */
    GATEWAY_NAME,

    /** Selects the Gateway edition. */
    GATEWAY_EDITION,

    /** Restores a Gateway backup during startup. */
    GATEWAY_RESTORE,

    /** Enables Gateway JVM debugging. */
    DEBUG_MODE,

    /** Configures the maximum Gateway memory. */
    MAX_MEMORY,

    /** Adds supplemental startup arguments. */
    SUPPLEMENTAL_ARGUMENTS,

    /** Selects built-in Ignition modules. */
    BUILT_IN_MODULE_SELECTION,

    /** Installs third-party Ignition modules. */
    THIRD_PARTY_MODULE_INSTALLATION,

    /** Configures the Ignition process user and group. */
    PROCESS_IDENTITY,

    /** Controls the Gateway Quick Start experience. */
    QUICK_START_CONTROL
}
