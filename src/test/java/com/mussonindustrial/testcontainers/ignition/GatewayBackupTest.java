package com.mussonindustrial.testcontainers.ignition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testcontainers.containers.ContainerLaunchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
public class GatewayBackupTest {

    @Test
    public void useGatewayBackup() {
        try (
                IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                        .withGatewayBackup("./src/test/resources/backup.gwbk", false)
        ) {
            ignition.start();
        }
    }

    @Test
    public void failWhenGatewayBackupNotPresent() {
        ContainerLaunchException exception = assertThrows(ContainerLaunchException.class, () ->  {
                    try(IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                            .withGatewayBackup("./src/test/resources/not-a-valid-backup.gwbk", false)
                    ) {
                        ignition.start();
                    }
                });
        assertEquals("Container startup failed for image inductiveautomation/ignition:8.1.33", exception.getMessage());
    }
}
