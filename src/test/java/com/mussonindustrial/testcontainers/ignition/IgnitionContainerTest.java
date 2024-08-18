package com.mussonindustrial.testcontainers.ignition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class IgnitionContainerTest {

    @Test
    @DisplayName("Start a container.")
    public void start() {
        try (
            IgnitionContainer ignition = new IgnitionContainer()
        ) {
            ignition.start();
        }
    }
}
