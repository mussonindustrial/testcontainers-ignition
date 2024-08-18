plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "com.mussonindustrial"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("org.testcontainers:testcontainers:1.20.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()

    maxHeapSize = "1G"
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    forkEvery = 1
    reports.html.required = true
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "testcontainers-ignition"
            from(components["java"])
            pom {
                name = "testcontainers-ignition"
                description = "testcontainers-ignition - An implementation of testcontainers for Ignition by Inductive Automation"
                url = "https://github.com/mussonindustrial/testcontainers-ignition"
                properties = mapOf()
                licenses {
                    license {
                        name = "The MIT License"
                        url = "https://opensource.org/license/mit"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        name = "Ben Musson"
                        email = "bmusson@mussonindustrial.com"
                        organization = "Musson Industrial"
                        organizationUrl = "https://www.mussonindustrial.com"
                    }
                }
                scm {
                    url = "https://github.com/mussonindustrial/testcontainers-ignition/tree/master"
                    connection = "scm:git:git://github.com/mussonindustrial/testcontainers-ignition.git"
                    developerConnection = "scm:git:ssh://github.com/mussonindustrial/testcontainers-ignition.git"
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
    // use the properties passed as command line args
    // -Psigning.keyId=${{secrets.SIGNING_KEY_ID}} -Psigning.password=${{secrets.SIGNING_PASSWORD}} -Psigning.secretKeyRingFile=$(echo ~/.gradle/secring.gpg)
    sign(publishing.publications["mavenJava"])
}