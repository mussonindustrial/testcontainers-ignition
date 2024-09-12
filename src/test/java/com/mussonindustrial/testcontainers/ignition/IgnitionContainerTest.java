package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.*;

import com.mussonindustrial.testcontainers.IgnitionTestImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Objects;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class IgnitionContainerTest {

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldUseGatewayBackup(IgnitionTestImage image) throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withGatewayBackup("./src/test/resources/backup.gwbk", false)
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
                    .withGatewayBackup(backup, false)
                    .acceptLicense()) {

                ignition.start();
            }
        });
        assertEquals(exception.getMessage(), String.format("gateway backup '%s' does not exist", backup));
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldUseListedModules(IgnitionTestImage image) {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withModules(GatewayModule.OPC_UA)
                .acceptLicense()) {

            ignition.waitingFor(Wait.forLogMessage(".*Processing GATEWAY_MODULES_ENABLED=opc-ua.*\\n", 1));
            ignition.start();
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldUseAdditionalArguments(IgnitionTestImage image) {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withAdditionalArgs("gateway.resolveHostNames=true", "gateway.useProxyForwardedHeader=true")
                .acceptLicense()) {

            WaitAllStrategy strategy = new WaitAllStrategy();
            strategy.withStrategy(Wait.forLogMessage(".*Collecting gateway arg: gateway.resolveHostNames=true\\n", 1))
                    .withStrategy(
                            Wait.forLogMessage(".*Collecting gateway arg: gateway.useProxyForwardedHeader=true\\n", 1));

            ignition.waitingFor(strategy);
            ignition.start();
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldReturnCorrectUrl(IgnitionTestImage image) {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())) {

            ignition.start();
            String url = ignition.getGatewayUrl();
            String statusPingUrl = url + "/StatusPing";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request =
                    HttpRequest.newBuilder().uri(URI.create(statusPingUrl)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertTrue(response.body().contains("\"state\":\"RUNNING\""));

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @EnumSource(IgnitionTestImage.class)
    public void shouldUseThirdPartyModules(IgnitionTestImage image) throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer(image.getDockerImageName())
                .withThirdPartyModules(
                        "./src/test/resources/Embr-EventStream-0.4.0.modl",
                        "./src/test/resources/Embr-Thermodynamics-0.1.2.modl")
                .withCredentials("admin", "password")
                .acceptLicense()) {

            WaitAllStrategy waitStrategy = new WaitAllStrategy();
            waitStrategy = waitStrategy
                    .withStrategy(Wait.forLogMessage(".*Embr Event Stream.*\\n", 1))
                    .withStrategy(Wait.forLogMessage(".*Embr Thermodynamics.*\\n", 1));

            ignition.waitingFor(waitStrategy);
            ignition.start();
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
                (endpoints) -> endpoints.stream()
                        .filter(e -> Objects.equals(e.getSecurityPolicyUri(), SecurityPolicy.None.getUri()))
                        .findFirst()
                        .map(e -> EndpointUtil.updateUrl(e, ignition.getHost(), ignition.getMappedOpcUaPort())),
                OpcUaClientConfigBuilder::build);
    }
}
