package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

@Execution(ExecutionMode.CONCURRENT)
public class ThirdPartyModuleTest {

    @Test
    public void useThirdPartyModules() throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                .withThirdPartyModules(
                        "./src/test/resources/Embr-EventStream-0.4.0.modl",
                        "./src/test/resources/Embr-Thermodynamics-0.1.2.modl")
                .acceptLicense()) {

            WaitAllStrategy waitStrategy = new WaitAllStrategy();
            waitStrategy = waitStrategy
                    .withStrategy(Wait.forLogMessage(".*Embr Event Stream.*\\n", 1))
                    .withStrategy(Wait.forLogMessage(".*Embr Thermodynamics.*\\n", 1));

            ignition.waitingFor(waitStrategy);
            ignition.start();
        }
    }

    @Test
    public void failWhenModuleNotPresent() {

        Path module = Path.of("./src/test/resources/not-a-valid-module.modl");

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                    .withThirdPartyModules(module)
                    .acceptLicense()) {
                ignition.start();
            }
        });
        assertEquals(String.format("module '%s' does not exist", module), exception.getMessage());
    }
}
