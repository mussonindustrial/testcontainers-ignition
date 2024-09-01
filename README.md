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
    testImplementation("com.mussonindustrial:testcontainers-ignition:0.3.0")
}
```

### Maven
```xml
<dependency>
    <groupId>com.mussonindustrial</groupId>
    <artifactId>testcontainers-ignition</artifactId>
    <version>0.3.0</version>
    <scope>test</scope>
</dependency>
```

## Usage
```java
void createIgnitionGateway() throws FileNotFoundException {
    try (IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
            .withCredentials("myUsername", "myPassword")
            .withEdition(GatewayEdition.STANDARD)
            .withModules(GatewayModule.PERSPECTIVE)
            .withGatewayBackup("./path/to/backup.gwbk")
            .withThirdPartyModules("./path/to/module.modl")
            .acceptLicense()) {
        ignition.start();
        String url = ignition.getGatewayUrl();
        // ... do something with your gateway!
    }
}
```

## Sponsors
Maintenance of this project is made possible by all our [contributors] and [sponsors].
If you'd like to sponsor this project and have your avatar or company logo appear below [click here](https://github.com/sponsors/mussonindustrial). 💖

## Links

-   [License (MIT)](LICENSE)
-   [Musson Industrial](https://mussonindustrial.com/)
-   [Inductive Automation](https://inductiveautomation.com/)

[testcontainers-ignition]: https://github.com/mussonindustrial/testcontainers-ignition/
[testcontainers]: https://java.testcontainers.org/
[contributors]: https://github.com/mussonindustrial/embr/graphs/contributors
[sponsors]: https://github.com/sponsors/mussonindustrial
