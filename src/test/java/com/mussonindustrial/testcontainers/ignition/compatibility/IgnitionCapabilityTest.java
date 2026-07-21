package com.mussonindustrial.testcontainers.ignition.compatibility;

import static org.junit.jupiter.api.Assertions.*;

import com.mussonindustrial.testcontainers.IgnitionTestImage;
import com.mussonindustrial.testcontainers.ignition.*;
import com.mussonindustrial.testcontainers.ignition.profiles.IgnitionProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.utility.DockerImageName;

@Execution(ExecutionMode.SAME_THREAD)
class IgnitionCapabilityTest {

    private static final String AVAILABILITY_WARNING_TEMPLATE =
            "Capability {} is documented for Ignition {} or newer, but image {} was requested. The '{}' profile translation will still be applied.";

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void profileDeclaresAndResolvesEveryCapability(IgnitionTestImage image) {
        try (IgnitionContainer container = new IgnitionContainer(image.getDockerImageName())) {

            IgnitionProfile profile = container.getProfile();
            CapabilityCatalog catalog = profile.capabilities();
            IgnitionVersion version = container.getIgnitionVersion();

            assertAll(Arrays.stream(IgnitionCapability.values()).map(capability -> () -> {
                assertTrue(catalog.capabilities().contains(capability), () -> "%s does not declare %s"
                        .formatted(profile.name(), capability));

                CapabilityCatalog.Entry entry = catalog.entry(capability);

                CapabilityCatalog.Resolution resolution = catalog.resolve(capability, version);

                if (entry instanceof CapabilityCatalog.SupportedEntry supported) {
                    assertAll(
                            () -> assertFalse(supported.appliers().isEmpty(), () -> "%s supports %s but has no appliers"
                                    .formatted(profile.name(), capability)),
                            () -> assertInstanceOf(
                                    CapabilityCatalog.SupportedResolution.class,
                                    resolution,
                                    () -> "%s resolved supported capability %s as %s"
                                            .formatted(profile.name(), capability, resolution)));
                } else {
                    assertAll(
                            () -> assertInstanceOf(
                                    CapabilityCatalog.UnsupportedEntry.class,
                                    entry,
                                    () -> "%s has an unknown entry type for %s: %s"
                                            .formatted(profile.name(), capability, entry)),
                            () -> assertInstanceOf(
                                    CapabilityCatalog.UnsupportedResolution.class,
                                    resolution,
                                    () -> "%s resolved unsupported capability %s as %s"
                                            .formatted(profile.name(), capability, resolution)));
                }
            }));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unavailableCapabilityCases")
    void warnsWhenRequestedCapabilityPredatesImage(
            String description,
            DockerImageName image,
            IgnitionCapability capability,
            IgnitionVersion minimumVersion,
            Consumer<IgnitionContainer> configuration) {
        try (CapturingIgnitionContainer container = new CapturingIgnitionContainer(image)) {

            configuration.accept(container);
            container.applyConfiguration();

            IgnitionVersion imageVersion = container.getIgnitionVersion();

            assertTrue(
                    containsAvailabilityWarning(container, capability, minimumVersion, imageVersion),
                    () -> "Expected a capability warning for %s.%nWarnings:%n%s"
                            .formatted(description, formatWarnings(container.warnings())));
        }
    }

    @Test
    void warnsWhenGatewayRestorePredatesImage(@TempDir Path temporaryDirectory) throws Exception {
        Path backup = Files.createFile(temporaryDirectory.resolve("backup.gwbk"));

        DockerImageName image = DockerImageName.parse("inductiveautomation/ignition:8.1.5");

        try (CapturingIgnitionContainer container = new CapturingIgnitionContainer(image)) {

            container.withGatewayBackup(backup);
            container.applyConfiguration();

            assertTrue(
                    containsAvailabilityWarning(
                            container,
                            IgnitionCapability.GATEWAY_RESTORE,
                            IgnitionVersion.parse("8.1.7"),
                            IgnitionVersion.parse("8.1.5")),
                    () -> "Expected a Gateway restore capability "
                            + "warning.%nWarnings:%n%s".formatted(formatWarnings(container.warnings())));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void doesNotWarnWhenCapabilityIsAvailable(IgnitionTestImage image) {
        try (CapturingIgnitionContainer container = new CapturingIgnitionContainer(image.getDockerImageName())) {

            container.withQuickStart(true);
            container.applyConfiguration();

            assertFalse(
                    containsAvailabilityWarning(container, IgnitionCapability.QUICK_START_CONTROL),
                    () -> "Unexpected capability warning for "
                            + "%s.%nWarnings:%n%s".formatted(image, formatWarnings(container.warnings())));
        }
    }

    private static boolean containsAvailabilityWarning(
            CapturingIgnitionContainer container,
            IgnitionCapability capability,
            IgnitionVersion minimumVersion,
            IgnitionVersion imageVersion) {
        List<Object> expectedArguments = List.of(
                capability, minimumVersion, imageVersion, container.getProfile().name());

        return container.warnings().stream()
                .anyMatch(warning -> warning.template().equals(AVAILABILITY_WARNING_TEMPLATE)
                        && warning.arguments().equals(expectedArguments));
    }

    private static boolean containsAvailabilityWarning(
            CapturingIgnitionContainer container, IgnitionCapability capability) {
        return container.warnings().stream()
                .anyMatch(warning -> warning.template().equals(AVAILABILITY_WARNING_TEMPLATE)
                        && !warning.arguments().isEmpty()
                        && warning.arguments().get(0) == capability);
    }

    private static String formatWarnings(List<CapturingIgnitionContainer.WarningCall> warnings) {
        if (warnings.isEmpty()) {
            return "<none>";
        }

        return warnings.stream()
                .map(CapturingIgnitionContainer.WarningCall::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static Stream<Arguments> unavailableCapabilityCases() {
        return Stream.of(
                Arguments.of(
                        "license acceptance",
                        image("8.1.5"),
                        IgnitionCapability.LICENSE_ACCEPTANCE,
                        IgnitionVersion.parse("8.1.7"),
                        configuration(IgnitionContainer::acceptLicense)),
                Arguments.of(
                        "initial administrator configuration",
                        image("8.1.7"),
                        IgnitionCapability.INITIAL_ADMIN_CONFIGURATION,
                        IgnitionVersion.parse("8.1.8"),
                        configuration(container -> container.withCredentials("admin", "password"))),
                Arguments.of(
                        "Gateway edition",
                        image("8.1.7"),
                        IgnitionCapability.GATEWAY_EDITION,
                        IgnitionVersion.parse("8.1.8"),
                        configuration(container -> container.withEdition(IgnitionGatewayEdition.STANDARD))),
                Arguments.of(
                        "leased license activation",
                        image("8.1.7"),
                        IgnitionCapability.LEASED_LICENSE_ACTIVATION,
                        IgnitionVersion.parse("8.1.8"),
                        configuration(container -> {
                            container.withLicenseKey("license-key");
                            container.withActivationToken("activation-token");
                        })),
                Arguments.of(
                        "supplemental arguments",
                        image("8.1.7"),
                        IgnitionCapability.SUPPLEMENTAL_ARGUMENTS,
                        IgnitionVersion.parse("8.1.8"),
                        configuration(container -> container.withAdditionalArgs("gateway.resolveHostNames=true"))),
                Arguments.of(
                        "built-in module selection",
                        image("8.1.16"),
                        IgnitionCapability.BUILT_IN_MODULE_SELECTION,
                        IgnitionVersion.parse("8.1.17"),
                        configuration(container -> container.withModules(IgnitionModule.OPC_UA))),
                Arguments.of(
                        "process identity",
                        image("8.1.16"),
                        IgnitionCapability.PROCESS_IDENTITY,
                        IgnitionVersion.parse("8.1.17"),
                        configuration(container -> {
                            container.withUid(1000);
                            container.withGid(1000);
                        })),
                Arguments.of(
                        "Quick Start control",
                        image("8.1.22"),
                        IgnitionCapability.QUICK_START_CONTROL,
                        IgnitionVersion.parse("8.1.23"),
                        configuration(container -> container.withQuickStart(true))));
    }

    private static DockerImageName image(String version) {
        return DockerImageName.parse("inductiveautomation/ignition:" + version);
    }

    private static Consumer<IgnitionContainer> configuration(Consumer<IgnitionContainer> configuration) {
        return configuration;
    }
}
