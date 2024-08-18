package com.mussonindustrial.testcontainers.ignition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class ParameterizedIgnitionContainerTest {

    @Test
    @DisplayName("Start a container with specified modules.")
    public void startWithModules() {
        ModuleIdentifier[] modules = {ModuleIdentifier.PERSPECTIVE};
        try (
                IgnitionContainer ignition = new IgnitionContainer()
                        .withModules(modules)
                        .withEdition(GatewayEdition.MAKER)
                        .withHttpPort(9080)
                        .withHttpsPort(9043)
                        .withAdminUsername("testcontainers")
                        .withAdminPassword("testcontainers-password")
                        .withQuickStart(false)
        ) {
            ignition.start();
        }
    }
}
