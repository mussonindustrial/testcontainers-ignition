# testcontainers-ignition [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][testcontainers-ignition]

[![Release](https://img.shields.io/maven-central/v/com.mussonindustrial/testcontainers-ignition)](https://central.sonatype.com/artifact/com.mussonindustrial/testcontainers-ignition)
[![javadoc](https://javadoc.io/badge2/com.mussonindustrial/testcontainers-ignition/javadoc.svg)](https://javadoc.io/doc/com.mussonindustrial/testcontainers-ignition)
[![Build](https://github.com/mussonindustrial/testcontainers-ignition/actions/workflows/build.yml/badge.svg)](https://github.com/mussonindustrial/testcontainers-ignition/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/mussonindustrial/embr/blob/main/LICENSE)

[Testcontainers] is a Java library that supports JUnit tests, providing lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container.

This project is a Testcontainers implementation for [Ignition by Inductive Automation](https://inductiveautomation.com/).



## Include

### Gradle
```kotlin
// build.gradle.kts
dependencies {
    testImplementation("com.mussonindustrial:testcontainers-ignition:0.5.0-SNAPSHOT")
}
```

### Maven
```xml
<dependency>
    <groupId>com.mussonindustrial</groupId>
    <artifactId>testcontainers-ignition</artifactId>
    <version>0.5.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Usage
```java
void createIgnitionGateway() throws FileNotFoundException {
    try (IgnitionContainer ignition = new IgnitionContainer()
            .withCredentials("myUsername", "myPassword")
            .withEdition(GatewayEdition.STANDARD)
            .withModules(GatewayModule.PERSPECTIVE)
            .withGatewayBackup("./path/to/backup.gwbk")
            .acceptLicense()) {
        ignition.start();
        String url = ignition.getGatewayUrl();
        // ... do something with your gateway!
    }
}
```

The no-argument constructor uses Ignition 8.3.8. A fresh gateway enables OPC UA
when no explicit module selection is supplied so automated commissioning can
finish. To run a custom unsigned module, enable developer mode and add its module
archive:

```java
void createDeveloperGateway() throws FileNotFoundException {
    try (IgnitionContainer ignition = new IgnitionContainer()
            .withCredentials("admin", "password")
            .withDeveloperMode()
            .withThirdPartyModule("./path/to/unsigned-module.modl")
            .acceptLicense()) {
        ignition.start();
        // The gateway and unsigned module are running when start() returns.
    }
}
```

The module identifier and gateway-scoped dependencies are read from `module.xml`
and included in the Ignition 8.3 automated commissioning configuration. String
and `Path` overloads are available for singular and plural third-party module APIs.

## Sponsors
Maintenance of this project is made possible by all our [contributors] and [sponsors].
If you'd like to sponsor this project and have your avatar or company logo appear below [click here](https://github.com/sponsors/mussonindustrial). 💖

## Links

-   [License (MIT)](LICENSE)
-   [Musson Industrial](https://mussonindustrial.com/)
-   [Inductive Automation](https://inductiveautomation.com/)
-   [Ignition 8.3 Docker Image](https://www.docs.inductiveautomation.com/docs/8.3/platform/docker-image)

[testcontainers-ignition]: https://github.com/mussonindustrial/testcontainers-ignition/
[testcontainers]: https://java.testcontainers.org/
[contributors]: https://github.com/mussonindustrial/embr/graphs/contributors
[sponsors]: https://github.com/sponsors/mussonindustrial
