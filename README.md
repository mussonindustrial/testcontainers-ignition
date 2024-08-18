# Testcontainers-Ignition [<img src="https://cdn.mussonindustrial.com/files/public/images/emblem.svg" alt="Musson Industrial Logo" width="90" height="40" align="right">][embr]

[![Build](https://github.com/mussonindustrial/testcontainers-ignition/actions/workflows/build.yml/badge.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/mussonindustrial/embr/blob/main/LICENSE)

A testcontainers implementation for Ignition by Inductive Automation.

## Install
```kotlin
// TODO: Publish to maven central.
```

## Usage
```java
void createIgnitionGateway() {
    try (
            IgnitionContainer ignition = new IgnitionContainer("inductiveautomation/ignition:8.1.33")
                    .withCredentials("myUsername", "myPassword")
                    .withEdition(GatewayEdition.STANDARD)
                    .withGatewayBackup("./path/to/backup.gwbk", false)
                    .withModule(Module.PERSPECTIVE)
                    .withThirdPartyModule("./path/to/module.modl")
    ) {
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

[embr]: https://github.com/mussonindustrial/embr
[contributors]: https://github.com/mussonindustrial/embr/graphs/contributors
[sponsors]: https://github.com/sponsors/mussonindustrial
