package com.mussonindustrial.testcontainers.ignition;

import com.mussonindustrial.testcontainers.IgnitionTestImages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testcontainers.containers.wait.strategy.Wait;

@Execution(ExecutionMode.CONCURRENT)
public class ModuleTest {

    @Test
    public void useModuleList() {
        try (IgnitionContainer ignition =
                new IgnitionContainer(IgnitionTestImages.IGNITION_TEST_IMAGE).withModules(Module.OPC_UA)) {
            ignition.waitingFor(Wait.forLogMessage(".*Processing GATEWAY_MODULES_ENABLED=opc-ua.*\\n", 1));
            ignition.start();
        }
    }
}
