package com.mussonindustrial.testcontainers;

import org.testcontainers.utility.DockerImageName;

public interface IgnitionTestImages {
    DockerImageName IGNITION_TEST_IMAGE = DockerImageName.parse("inductiveautomation/ignition:8.1.33");
}
