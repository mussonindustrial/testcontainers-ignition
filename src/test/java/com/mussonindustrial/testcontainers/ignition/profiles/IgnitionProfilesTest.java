package com.mussonindustrial.testcontainers.ignition.profiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import org.junit.jupiter.api.Test;

class IgnitionProfilesTest {

    @Test
    void resolvesIgnition81Profile() {
        IgnitionProfile profile = IgnitionProfiles.resolve(IgnitionVersion.parse("8.1.48"));
        assertInstanceOf(Ignition81Profile.class, profile);
    }

    @Test
    void supportsAllIgnition81PatchVersions() {
        assertTrue(IgnitionProfiles.supports(IgnitionVersion.parse("8.1.0")));
        assertTrue(IgnitionProfiles.supports(IgnitionVersion.parse("8.1.48")));
    }

    @Test
    void resolvesIgnition83Profile() {
        IgnitionProfile profile = IgnitionProfiles.resolve(IgnitionVersion.parse("8.3.8"));
        assertInstanceOf(Ignition83Profile.class, profile);
    }

    @Test
    void supportsAllIgnition83PatchVersions() {
        assertTrue(IgnitionProfiles.supports(IgnitionVersion.parse("8.3.0")));
        assertTrue(IgnitionProfiles.supports(IgnitionVersion.parse("8.3.8")));
    }

    @Test
    void rejectsUnknownReleaseLine() {
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class, () -> IgnitionProfiles.resolve(IgnitionVersion.parse("2028.1.0")));

        assertTrue(exception.getMessage().contains("2028.1.0"));
    }

    @Test
    void exposesImmutableProfileList() {
        assertThrows(UnsupportedOperationException.class, () -> IgnitionProfiles.all()
                .clear());
    }

    @Test
    void registryIsValid() {
        IgnitionProfiles.validate();
    }

    @Test
    void declaresExpectedInitialProfile() {
        assertEquals("Ignition 8.1", IgnitionProfiles.all().get(0).name());
    }
}
