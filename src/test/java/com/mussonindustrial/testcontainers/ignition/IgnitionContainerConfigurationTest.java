package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mussonindustrial.testcontainers.IgnitionTestImage;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.utility.DockerImageName;

class IgnitionContainerConfigurationTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void selectsVersionAndProfile(IgnitionTestImage image) {
        try (IgnitionContainer container = new IgnitionContainer(image.getDockerImageName())) {

            assertAll(
                    () -> assertEquals(image.getVersion(), container.getIgnitionVersion()),
                    () -> assertNotNull(container.getProfile()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void storesAdministratorCredentials(IgnitionTestImage image) {
        try (IgnitionContainer container =
                new IgnitionContainer(image.getDockerImageName()).withCredentials("admin", "password")) {

            assertAll(
                    () -> assertEquals("admin", container.getUsername()),
                    () -> assertEquals("password", container.getPassword()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void credentialsAreUnsetByDefault(IgnitionTestImage image) {
        try (IgnitionContainer container = new IgnitionContainer(image.getDockerImageName())) {

            assertAll(() -> assertNull(container.getUsername()), () -> assertNull(container.getPassword()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void rejectsMissingGatewayBackupBeforeStartup(IgnitionTestImage image) {
        Path backup = Path.of("src/test/resources/" + "not-a-valid-backup.gwbk");

        try (IgnitionContainer container = new IgnitionContainer(image.getDockerImageName())) {

            FileNotFoundException exception =
                    assertThrows(FileNotFoundException.class, () -> container.withGatewayBackup(backup));

            assertAll(
                    () -> assertTrue(exception.getMessage().contains(backup.toString())),
                    () -> assertTrue(exception.getMessage().contains("does not exist")));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void rejectsMissingThirdPartyModuleBeforeStartup(IgnitionTestImage image) {
        Path module = Path.of("src/test/resources/" + "not-a-valid-module.modl");

        try (IgnitionContainer container = new IgnitionContainer(image.getDockerImageName())) {

            FileNotFoundException exception =
                    assertThrows(FileNotFoundException.class, () -> container.withThirdPartyModules(module));

            assertAll(
                    () -> assertTrue(exception.getMessage().contains(module.toString())),
                    () -> assertTrue(exception.getMessage().contains("does not exist")));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void rejectsNullModuleEntry(IgnitionTestImage image) {
        try (IgnitionContainer container = new IgnitionContainer(image.getDockerImageName())) {

            assertThrows(NullPointerException.class, () -> container.withModules(IgnitionModule.OPC_UA, null));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void rejectsNullAdditionalArgument(IgnitionTestImage image) {
        try (IgnitionContainer container = new IgnitionContainer(image.getDockerImageName())) {

            assertThrows(
                    NullPointerException.class,
                    () -> container.withAdditionalArgs("gateway.resolveHostNames=true", null));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void defaultPlanExposesGatewayEndpoints(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.applyConfiguration();

            int httpPort = containerPort(container, IgnitionEndpoint.GATEWAY_HTTP);

            int httpsPort = containerPort(container, IgnitionEndpoint.GATEWAY_HTTPS);

            int gatewayNetworkPort = containerPort(container, IgnitionEndpoint.GATEWAY_NETWORK);

            assertAll(
                    () -> assertTrue(container.getExposedPorts().contains(httpPort)),
                    () -> assertTrue(container.getExposedPorts().contains(httpsPort)),
                    () -> assertTrue(container.getExposedPorts().contains(gatewayNetworkPort)));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void opcUaIsNotExposedByDefault(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.applyConfiguration();

            int opcUaPort = containerPort(container, IgnitionEndpoint.OPC_UA);

            assertFalse(container.getExposedPorts().contains(opcUaPort));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void opcUaModuleExposesOpcUaEndpoint(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.withModules(IgnitionModule.OPC_UA);
            container.applyConfiguration();

            int opcUaPort = containerPort(container, IgnitionEndpoint.OPC_UA);

            assertTrue(container.getExposedPorts().contains(opcUaPort));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void emptyModuleSelectionDoesNotExposeOpcUa(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.withModules();
            container.applyConfiguration();

            int opcUaPort = containerPort(container, IgnitionEndpoint.OPC_UA);

            assertFalse(container.getExposedPorts().contains(opcUaPort));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void debugModeExposesDebugEndpoint(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.withDebugMode(true);
            container.applyConfiguration();

            int debugPort = containerPort(container, IgnitionEndpoint.DEBUG);

            assertTrue(container.getExposedPorts().contains(debugPort));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void translatesAdditionalArguments(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.withAdditionalArgs("gateway.resolveHostNames=true", "gateway.useProxyForwardedHeader=true");

            container.applyConfiguration();

            assertArrayEquals(
                    new String[] {"--", "gateway.resolveHostNames=true", "gateway.useProxyForwardedHeader=true"},
                    container.getCommandParts());
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void rawCommandOverridesGeneratedCommand(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.withCommand("custom-command");
            container.withAdditionalArgs("gateway.resolveHostNames=true");

            container.applyConfiguration();

            assertArrayEquals(new String[] {"custom-command"}, container.getCommandParts());
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void preservesAdditionalExposedPort(IgnitionTestImage image) {
        try (InspectableIgnitionContainer container = new InspectableIgnitionContainer(image.getDockerImageName())) {

            container.withAdditionalExposedPort(12345);
            container.applyConfiguration();

            assertTrue(container.getExposedPorts().contains(12345));
        }
    }

    private static int containerPort(IgnitionContainer container, IgnitionEndpoint endpoint) {
        return container.getProfile().endpoints().port(endpoint);
    }

    private static final class InspectableIgnitionContainer extends IgnitionContainer {

        private InspectableIgnitionContainer(DockerImageName dockerImageName) {
            super(dockerImageName);
        }

        private void applyConfiguration() {
            configure();
        }
    }
}
