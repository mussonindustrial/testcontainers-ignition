package com.mussonindustrial.testcontainers;

import org.testcontainers.utility.DockerImageName;

public enum IgnitionTestImage {
    IGNITION_8_1_33("inductiveautomation/ignition:8.1.33"),
    IGNITION_8_1_43("inductiveautomation/ignition:8.1.43");

    private final DockerImageName dockerImageName;

    public DockerImageName getDockerImageName() {
        return dockerImageName;
    }

    IgnitionTestImage(String fullImageName) {
        this.dockerImageName = DockerImageName.parse(fullImageName);
    }
}
