package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mussonindustrial.testcontainers.IgnitionTestImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.containers.output.WaitingConsumer;

public class IgnitionContainerTest {

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldUseGatewayBackup(IgnitionTestImage image) throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withGatewayBackup("./src/test/resources/backup.gwbk", false)
                .withCredentials("admin", "password")
                .acceptLicense()) {

            ignition.start();
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldFailIfGatewayBackupNotPresent(IgnitionTestImage image) {
        Path backup = Path.of("./src/test/resources/not-a-valid-backup.gwbk");

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                    .withGatewayBackup(backup)
                    .acceptLicense()) {

                ignition.start();
            }
        });
        assertEquals(String.format("gateway backup '%s' does not exist", backup), exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldUseListedModules(IgnitionTestImage image) {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withModules(GatewayModule.OPC_UA)
                .withCredentials("admin", "password")
                .acceptLicense()) {

            ignition.start();
            assertEquals(
                    GatewayModule.OPC_UA.getIdentifier(), ignition.getEnvMap().get("GATEWAY_MODULES_ENABLED"));
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldUseAdditionalArguments(IgnitionTestImage image) {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withAdditionalArgs("gateway.resolveHostNames=true", "gateway.useProxyForwardedHeader=true")
                .withCredentials("admin", "password")
                .acceptLicense()) {

            ignition.start();
            assertArrayEquals(
                    new String[] {"--", "gateway.resolveHostNames=true", "gateway.useProxyForwardedHeader=true"},
                    ignition.getCommandParts());
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldReturnCorrectUrl(IgnitionTestImage image) {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withCredentials("admin", "password")
                .acceptLicense()) {

            ignition.start();
            String statusPingUrl = ignition.getGatewayUrl() + "/StatusPing";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request =
                    HttpRequest.newBuilder().uri(URI.create(statusPingUrl)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals("{\"state\":\"RUNNING\"}", response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    @EnabledIfEnvironmentVariable(named = "IGNITION_TEST_UNSIGNED_MODULE", matches = ".+")
    public void shouldUseUnsignedThirdPartyModule(IgnitionTestImage image)
            throws FileNotFoundException, TimeoutException {
        String modulePath = System.getenv("IGNITION_TEST_UNSIGNED_MODULE");
        WaitingConsumer moduleLog = new WaitingConsumer();

        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withDeveloperMode()
                .withThirdPartyModule(modulePath)
                .withCredentials("admin", "password")
                .withLogConsumer(moduleLog)
                .acceptLicense()) {

            ignition.start();
            moduleLog.waitUntil(
                    frame ->
                            frame.getUtf8String().contains("Starting up module 'com.kevinherron.modbus-server-driver'"),
                    10,
                    TimeUnit.SECONDS);
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldFailIfThirdPartyModulesNotPresent(IgnitionTestImage image) {
        Path module = Path.of("./src/test/resources/not-a-valid-module.modl");

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                    .withThirdPartyModules(module)
                    .withCredentials("admin", "password")
                    .acceptLicense()) {

                ignition.start();
            }
        });
        assertEquals(String.format("module '%s' does not exist", module), exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldMapOpcUaEndpoint(IgnitionTestImage image) throws FileNotFoundException, UaException {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withModules(GatewayModule.OPC_UA)
                .withGatewayBackup("./src/test/resources/opcua.gwbk")
                .acceptLicense()) {

            ignition.start();

            OpcUaClient opcUaClient = getUnsecureOpcUaClient(ignition);
            opcUaClient.connect();
            opcUaClient.disconnect();
        }
    }

    private OpcUaClient getUnsecureOpcUaClient(IgnitionContainer ignition) throws UaException {
        return OpcUaClient.create(
                ignition.getOpcUaDiscoveryUrl(),
                endpoints -> endpoints.stream()
                        .filter(e -> Objects.equals(e.getSecurityPolicyUri(), SecurityPolicy.None.getUri()))
                        .findFirst()
                        .map(e -> EndpointUtil.updateUrl(e, ignition.getHost(), ignition.getMappedOpcUaPort())),
                OpcUaClientConfigBuilder::build);
    }
}
