package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mussonindustrial.testcontainers.IgnitionTestImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.output.WaitingConsumer;

@Tag("integration")
@Execution(ExecutionMode.SAME_THREAD)
class IgnitionContainerIntegrationTest {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

    private static final String RUNNING_RESPONSE = "{\"state\":\"RUNNING\"}";

    private static final Path GATEWAY_BACKUP = Path.of("src/test/resources/backup.gwbk");

    private static final Path OPC_UA_BACKUP = Path.of("src/test/resources/opcua.gwbk");

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(15);

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();

    @ParameterizedTest(name = "{0}")
    @EnumSource(IgnitionTestImage.class)
    void startsAndReportsRunning(IgnitionTestImage image) throws Exception {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())) {

            ignition.withCredentials(USERNAME, PASSWORD).acceptLicense();

            ignition.start();

            HttpResponse<String> response = requestStatusPing(ignition);

            URI gatewayUri = URI.create(ignition.getGatewayUrl());

            IllegalStateException opcUaException =
                    assertThrows(IllegalStateException.class, ignition::getMappedOpcUaPort);

            assertAll(
                    () -> assertTrue(ignition.isRunning()),
                    () -> assertEquals(image.getVersion(), ignition.getIgnitionVersion()),
                    () -> assertEquals(USERNAME, ignition.getUsername()),
                    () -> assertEquals(PASSWORD, ignition.getPassword()),
                    () -> assertEquals(ignition.getMappedGatewayPort(), gatewayUri.getPort()),
                    () -> assertEquals(200, response.statusCode()),
                    () -> assertEquals(RUNNING_RESPONSE, response.body()),
                    () -> assertTrue(opcUaException.getMessage().contains("not exposed")));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("representativeImages")
    void startsWithGatewayBackup(IgnitionTestImage image) throws Exception {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())) {

            ignition.withCredentials(USERNAME, PASSWORD)
                    .withGatewayBackup(GATEWAY_BACKUP, false)
                    .acceptLicense();

            ignition.start();

            HttpResponse<String> response = requestStatusPing(ignition);

            assertAll(
                    () -> assertTrue(ignition.isRunning()),
                    () -> assertEquals(200, response.statusCode()),
                    () -> assertEquals(RUNNING_RESPONSE, response.body()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("representativeImages")
    void connectsToOpcUaEndpoint(IgnitionTestImage image) throws Exception {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())) {

            ignition.withModules(IgnitionModule.OPC_UA)
                    .withGatewayBackup(OPC_UA_BACKUP)
                    .acceptLicense();

            ignition.start();

            OpcUaClient client = createUnsecuredOpcUaClient(ignition);

            boolean connected = false;

            try {
                client.connect().get(30, TimeUnit.SECONDS);

                connected = true;

                assertTrue(ignition.getMappedOpcUaPort() > 0);
            } finally {
                if (connected) {
                    client.disconnect().get(15, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Test
    void rejectsConfigurationAfterStartup() throws Exception {
        IgnitionTestImage image = IgnitionTestImage.IGNITION_8_1_49;

        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())) {

            ignition.withCredentials(USERNAME, PASSWORD).acceptLicense();

            ignition.start();

            IllegalStateException exception =
                    assertThrows(IllegalStateException.class, () -> ignition.withGatewayName("too-late"));

            assertTrue(exception.getMessage().contains("before the container is running"));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("representativeImages")
    @EnabledIf("thirdPartyModuleConfigured")
    void installsThirdPartyModule(IgnitionTestImage image) throws Exception {
        Path module = Path.of(requiredEnvironmentVariable("IGNITION_TEST_THIRD_PARTY_MODULE"));

        String startupLog = requiredEnvironmentVariable("IGNITION_TEST_THIRD_PARTY_MODULE_LOG");

        WaitingConsumer moduleLogs = new WaitingConsumer();

        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())) {

            ignition.withCredentials(USERNAME, PASSWORD)
                    .withThirdPartyModules(module)
                    .withLogConsumer(moduleLogs)
                    .acceptLicense();

            ignition.start();

            moduleLogs.waitUntil(frame -> frame.getUtf8String().contains(startupLog), 30, TimeUnit.SECONDS);
        }
    }

    private static HttpResponse<String> requestStatusPing(IgnitionContainer ignition)
            throws IOException, InterruptedException {
        URI uri = URI.create(ignition.getGatewayUrl() + "/StatusPing");

        HttpRequest request =
                HttpRequest.newBuilder(uri).timeout(HTTP_TIMEOUT).GET().build();

        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static OpcUaClient createUnsecuredOpcUaClient(IgnitionContainer ignition) throws UaException {
        return OpcUaClient.create(
                ignition.getOpcUaDiscoveryUrl(),
                endpoints -> endpoints.stream()
                        .filter(endpoint ->
                                Objects.equals(endpoint.getSecurityPolicyUri(), SecurityPolicy.None.getUri()))
                        .findFirst()
                        .map(endpoint ->
                                EndpointUtil.updateUrl(endpoint, ignition.getHost(), ignition.getMappedOpcUaPort())),
                OpcUaClientConfigBuilder::build);
    }

    private static Stream<IgnitionTestImage> representativeImages() {
        return IgnitionTestImage.representativeImages();
    }

    private static boolean thirdPartyModuleConfigured() {
        return isNonBlankEnvironmentVariable("IGNITION_TEST_THIRD_PARTY_MODULE")
                && isNonBlankEnvironmentVariable("IGNITION_TEST_THIRD_PARTY_MODULE_LOG");
    }

    private static boolean isNonBlankEnvironmentVariable(String name) {
        String value = System.getenv(name);

        return value != null && !value.isBlank();
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = Objects.requireNonNull(System.getenv(name), () -> "Environment variable is not set: " + name);

        if (value.isBlank()) {
            throw new IllegalStateException("Environment variable is blank: " + name);
        }

        return value;
    }
}
