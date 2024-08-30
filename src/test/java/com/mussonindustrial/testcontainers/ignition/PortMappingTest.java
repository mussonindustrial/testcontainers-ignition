package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public class PortMappingTest {

    @Test
    public void workingMappedHttpPort() {
        try (IgnitionContainer ignition =
                new IgnitionContainer("inductiveautomation/ignition:8.1.33").acceptLicense()) {
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
}
