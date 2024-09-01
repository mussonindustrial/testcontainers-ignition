package com.mussonindustrial.testcontainers.ignition;

import com.mussonindustrial.testcontainers.IgnitionTestImages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

@Execution(ExecutionMode.CONCURRENT)
public class ArgsTest {

    @Test
    public void useModuleList() {
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
}
