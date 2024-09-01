package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mussonindustrial.testcontainers.IgnitionTestImages;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

@Execution(ExecutionMode.CONCURRENT)
public class IgnitionContainerTest {

    @Test
    public void shouldUseGatewayBackup() throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer(IgnitionTestImages.IGNITION_TEST_IMAGE)
                .withGatewayBackup("./src/test/resources/backup.gwbk", false)) {
            ignition.start();
        }
    }

    @Test
    public void shouldFailIfGatewayBackupNotPresent() {

        Path backup = Path.of("./src/test/resources/not-a-valid-backup.gwbk");

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            try (IgnitionContainer ignition =
                    new IgnitionContainer("inductiveautomation/ignition:8.1.33").withGatewayBackup(backup, false)) {
                ignition.start();
            }
        });
        assertEquals(String.format("gateway backup '%s' does not exist", backup), exception.getMessage());
    }

    @Test
    public void shouldUseListedModules() {
        try (IgnitionContainer ignition =
                new IgnitionContainer(IgnitionTestImages.IGNITION_TEST_IMAGE).withModules(Module.OPC_UA)) {
            ignition.waitingFor(Wait.forLogMessage(".*Processing GATEWAY_MODULES_ENABLED=opc-ua.*\\n", 1));
            ignition.start();
        }
    }

    @Test
    public void shouldUseAdditionalArguments() {
        try (IgnitionContainer ignition = new IgnitionContainer(IgnitionTestImages.IGNITION_TEST_IMAGE)
                .withAdditionalArgs("gateway.resolveHostNames=true", "gateway.useProxyForwardedHeader=true")) {

            WaitAllStrategy strategy = new WaitAllStrategy();
            strategy.withStrategy(Wait.forLogMessage(".*Collecting gateway arg: gateway.resolveHostNames=true\\n", 1))
                    .withStrategy(
                            Wait.forLogMessage(".*Collecting gateway arg: gateway.useProxyForwardedHeader=true\\n", 1));

            ignition.waitingFor(strategy);
            ignition.start();
        }
    }

    @Test
    public void shouldReturnCorrectUrl() {
        try (IgnitionContainer ignition = new IgnitionContainer(IgnitionTestImages.IGNITION_TEST_IMAGE)) {
            ignition.start();
            String url = ignition.getGatewayUrl();
            String statusPingUrl = url + "/StatusPing";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request =
                    HttpRequest.newBuilder().uri(URI.create(statusPingUrl)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(response.body(), "{\"state\":\"RUNNING\"}");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldUseThirdPartyModules() throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer(IgnitionTestImages.IGNITION_TEST_IMAGE)
                .withThirdPartyModules(
                        "./src/test/resources/Embr-EventStream-0.4.0.modl",
                        "./src/test/resources/Embr-Thermodynamics-0.1.2.modl")) {

            WaitAllStrategy waitStrategy = new WaitAllStrategy();
            waitStrategy = waitStrategy
                    .withStrategy(Wait.forLogMessage(".*Embr Event Stream.*\\n", 1))
                    .withStrategy(Wait.forLogMessage(".*Embr Thermodynamics.*\\n", 1));

            ignition.waitingFor(waitStrategy);
            ignition.start();
        }
    }

    @Test
    public void shouldFailIfThirdPartyModulesNotPresent() {

        Path module = Path.of("./src/test/resources/not-a-valid-module.modl");

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            try (IgnitionContainer ignition =
                    new IgnitionContainer("inductiveautomation/ignition:8.1.33").withThirdPartyModules(module)) {
                ignition.start();
            }
        });
        assertEquals(String.format("module '%s' does not exist", module), exception.getMessage());
    }
}
