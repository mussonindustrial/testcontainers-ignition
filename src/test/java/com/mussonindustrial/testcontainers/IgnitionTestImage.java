package com.mussonindustrial.testcontainers;

import com.mussonindustrial.testcontainers.ignition.IgnitionVersion;
import java.util.Arrays;
import java.util.stream.Stream;
import org.testcontainers.utility.DockerImageName;

public enum IgnitionTestImage {
    IGNITION_8_1_33("inductiveautomation/ignition:8.1.33", false),
    IGNITION_8_1_49("inductiveautomation/ignition:8.1.49", true),
    IGNITION_8_3_8("inductiveautomation/ignition:8.3.8", true);

    private final DockerImageName dockerImageName;
    private final IgnitionVersion version;
    private final boolean representative;

    IgnitionTestImage(String fullImageName, boolean representative) {
        dockerImageName = DockerImageName.parse(fullImageName);

        version = IgnitionVersion.from(dockerImageName);

        this.representative = representative;
    }

    public DockerImageName getDockerImageName() {
        return dockerImageName;
    }

    public IgnitionVersion getVersion() {
        return version;
    }

    public boolean isRepresentative() {
        return representative;
    }

    public static Stream<IgnitionTestImage> representativeImages() {
        return Arrays.stream(values()).filter(IgnitionTestImage::isRepresentative);
    }

    @Override
    public String toString() {
        return dockerImageName.toString();
    }
}
