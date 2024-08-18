package com.mussonindustrial.testcontainers.ignition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public class ThirdPartyModuleTest {

    @Test
    public void useThirdPartyModules() {
        try (
                IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                        .withThirdPartyModule("./src/test/resources/Embr-EventStream-0.4.0.modl")
                        .withThirdPartyModule("./src/test/resources/Embr-Thermodynamics-0.1.2.modl")
        ) {
            ignition.start();
        }
    }
}
