package com.mussonindustrial.testcontainers;

import org.testcontainers.utility.DockerImageName;

public enum IgnitionTestImage {
    IGNITION_8_3_8("inductiveautomation/ignition:8.3.8");

    private final DockerImageName dockerImageName;

    public DockerImageName getDockerImageName() {
        return dockerImageName;
    }

    IgnitionTestImage(String fullImageName) {
        this.dockerImageName = DockerImageName.parse(fullImageName);
    }
}
