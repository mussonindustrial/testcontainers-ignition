package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public class ThirdPartyModuleTest {

    @Test
    public void useThirdPartyModules() throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                .withThirdPartyModule("./src/test/resources/Embr-EventStream-0.4.0.modl")
                .withThirdPartyModule("./src/test/resources/Embr-Thermodynamics-0.1.2.modl")) {
            ignition.start();
        }
    }

    @Test
    public void failWhenModuleNotPresent() {
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                    .withThirdPartyModule("./src/test/resources/not-a-valid-module.modl")) {
                ignition.start();
            }
        });
        assertEquals(
                "module '.\\src\\test\\resources\\not-a-valid-module.modl' does not exist", exception.getMessage());
    }
}
