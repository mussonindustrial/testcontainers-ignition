# testcontainers-ignition [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][testcontainers-ignition]

[![Release](https://img.shields.io/maven-central/v/com.mussonindustrial/testcontainers-ignition)](https://central.sonatype.com/artifact/com.mussonindustrial/testcontainers-ignition)
[![javadoc](https://javadoc.io/badge2/com.mussonindustrial/testcontainers-ignition/javadoc.svg)](https://javadoc.io/doc/com.mussonindustrial/testcontainers-ignition)
[![Build](https://github.com/mussonindustrial/testcontainers-ignition/actions/workflows/build.yml/badge.svg)](https://github.com/mussonindustrial/testcontainers-ignition/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/mussonindustrial/embr/blob/main/LICENSE)

[Testcontainers] is a Java library that supports JUnit tests, providing lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container.

This project provides a Testcontainers implementation for [Ignition by Inductive Automation](https://inductiveautomation.com/).

## Include

### Gradle
```kotlin
// build.gradle.kts
dependencies {
    testImplementation("com.mussonindustrial:testcontainers-ignition:0.5.0")
}
```

### Maven
```xml
<dependency>
    <groupId>com.mussonindustrial</groupId>
    <artifactId>testcontainers-ignition</artifactId>
    <version>0.5.0</version>
    <scope>test</scope>
</dependency>
```

## Usage
Create an `IgnitionContainer` using a versioned Ignition Docker image. 
Untagged images and the `latest` tag are rejected. 
Ignition 8.1 and 8.3 images are currently supported.

### Start a Gateway

```java
import com.mussonindustrial.testcontainers.ignition.IgnitionContainer;
import com.mussonindustrial.testcontainers.ignition.IgnitionGatewayEdition;
import com.mussonindustrial.testcontainers.ignition.IgnitionModule;
import java.nio.file.Path;

void createIgnitionGateway() {
    try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.3.8")
            .withCredentials("admin", "password")
            .withEdition(IgnitionGatewayEdition.STANDARD)
            .withModules(IgnitionModule.PERSPECTIVE)
            .withGatewayBackup("path/to/backup.gwbk")
            .acceptLicense()) {

        ignition.start();

        String gatewayUrl = ignition.getGatewayUrl();

        // Use the Gateway in your test.
    }
}
```

The container waits for the Gateway to finish starting before `start()` returns.

### Install a Third-Party Module

```java
import com.mussonindustrial.testcontainers.ignition.IgnitionContainer;
import java.io.FileNotFoundException;
import java.nio.file.Path;

void createGatewayWithThirdPartyModule() throws FileNotFoundException {
    try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.3.8")
            .withCredentials("admin", "password")
            .withThirdPartyModule("path/to/module.modl")
            .withAllowUnsignedModules()
            .acceptLicense()) {

        ignition.start();

        // The Gateway is running with the module installed.
    }
}
```

Third-party module archives are inspected when they are added to the container.
The module identifier and Gateway-scoped dependencies are read from the archive’s `module.xml` descriptor and included in the Gateway configuration automatically.

Built-in modules remain explicitly controlled through `withModules(...)`.

For Ignition 8.3 images, third-party module identifiers are also supplied to the Docker image’s automatic module-certificate acceptance mechanism.
Ignition 8.1 images do not provide the equivalent Docker configuration capability.


## Sponsors
Maintenance of this project is made possible by all our [contributors] and [sponsors].
If you'd like to sponsor this project and have your avatar or company logo appear below [click here](https://github.com/sponsors/mussonindustrial).

## Links

- [License (MIT)](LICENSE)
- [Musson Industrial](https://mussonindustrial.com/)
- [Inductive Automation](https://inductiveautomation.com/)
- [Ignition 8.3 Docker Image](https://www.docs.inductiveautomation.com/docs/8.3/platform/docker-image)

[testcontainers-ignition]: https://github.com/mussonindustrial/testcontainers-ignition/
[testcontainers]: https://java.testcontainers.org/
[contributors]: https://github.com/mussonindustrial/embr/graphs/contributors
[sponsors]: https://github.com/sponsors/mussonindustrial
