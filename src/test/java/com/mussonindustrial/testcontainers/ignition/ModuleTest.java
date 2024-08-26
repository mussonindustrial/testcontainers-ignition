package com.mussonindustrial.testcontainers.ignition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testcontainers.containers.wait.strategy.Wait;

@Execution(ExecutionMode.CONCURRENT)
public class ModuleTest {

    @Test
    public void useModuleList() {
        try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                .withModule(Module.OPC_UA)) {
            ignition.waitingFor(Wait.forLogMessage(".*Processing GATEWAY_MODULES_ENABLED=opc-ua.*\\n", 1));
            ignition.start();
        }
    }
}
