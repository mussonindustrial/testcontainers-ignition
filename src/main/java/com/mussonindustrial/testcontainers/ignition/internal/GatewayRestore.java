package com.mussonindustrial.testcontainers.ignition.internal;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Describes a Gateway backup restoration request.
 *
 * @param backup Gateway backup file
 * @param restoreDisabled whether disabled resources remain disabled
 */
public record GatewayRestore(Path backup, boolean restoreDisabled) {

    /**
     * Validates the restoration request.
     *
     * @param backup Gateway backup file
     * @param restoreDisabled whether disabled resources remain disabled
     */
    public GatewayRestore {
        Objects.requireNonNull(backup, "backup");
    }
}
