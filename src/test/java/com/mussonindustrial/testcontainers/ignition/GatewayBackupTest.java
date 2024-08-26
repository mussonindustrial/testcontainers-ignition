package com.mussonindustrial.testcontainers.ignition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public class GatewayBackupTest {

    @Test
    public void useGatewayBackup() throws FileNotFoundException {
        try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                .withGatewayBackup("./src/test/resources/backup.gwbk", false)) {
            ignition.start();
        }
    }

    @Test
    public void failWhenGatewayBackupNotPresent() {

        Path backup = Path.of("./src/test/resources/not-a-valid-backup.gwbk");

        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            try (IgnitionContainer ignition =
                    new IgnitionContainer("inductiveautomation/ignition:8.1.33").withGatewayBackup(backup, false)) {
                ignition.start();
            }
        });
        assertEquals(String.format("gateway backup '%s' does not exist", backup), exception.getMessage());
    }
}
