package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

class IgnitionVersionTest {

    @Test
    void parsesIgnition81Version() {
        assertEquals(new IgnitionVersion(8, 1, 48), IgnitionVersion.parse("8.1.48"));
    }

    @Test
    void parsesCalendarVersion() {
        assertEquals(new IgnitionVersion(2027, 2, 0), IgnitionVersion.parse("2027.2.0"));
    }

    @Test
    void recognizesReleaseLine() {
        IgnitionVersion version = IgnitionVersion.parse("2027.2.3");

        assertTrue(version.isReleaseLine(2027, 2));
    }

    @Test
    void comparesPatchVersions() {
        assertTrue(IgnitionVersion.parse("8.1.17").isAtLeast("8.1.16"));
    }

    @Test
    void prereleaseSortsBeforeStableRelease() {
        assertTrue(IgnitionVersion.parse("8.3.0-rc1").isBefore("8.3.0"));
    }

    @Test
    void betaSortsBeforeReleaseCandidate() {
        assertTrue(IgnitionVersion.parse("2027.2.0-beta2").isBefore("2027.2.0-rc1"));
    }

    @Test
    void rejectsFloatingTag() {
        DockerImageName image = DockerImageName.parse("inductiveautomation/ignition:latest");

        assertThrows(IllegalArgumentException.class, () -> IgnitionVersion.from(image));
    }
}
