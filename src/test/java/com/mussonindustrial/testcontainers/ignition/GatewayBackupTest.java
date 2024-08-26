package com.mussonindustrial.testcontainers.ignition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
public class GatewayBackupTest {

    @Test
    public void useGatewayBackup() throws FileNotFoundException {
        try (
                IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                        .withGatewayBackup("./src/test/resources/backup.gwbk", false)
        ) {
            ignition.start();
        }
    }

    @Test
    public void failWhenGatewayBackupNotPresent() {
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () ->  {
                    try(IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                            .withGatewayBackup("./src/test/resources/not-a-valid-backup.gwbk", false)
                    ) {
                        ignition.start();
                    }
                });
        assertEquals("gateway backup '.\\src\\test\\resources\\not-a-valid-backup.gwbk' does not exist", exception.getMessage());
    }
}
