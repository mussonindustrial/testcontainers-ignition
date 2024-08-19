import org.jreleaser.model.Active

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.jreleaser)
}

group = "com.mussonindustrial"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.testcontainers)

    testRuntimeOnly(libs.junit.platform)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.slf4j)
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

    testLogging {
        showStandardStreams = true
    }
}

publishing {
    repositories {
        maven {
            name = "MavenCentral"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
    publications {
        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "com.mussonindustrial"
            artifactId = "testcontainers-ignition"
            description = "testcontainers-ignition - An implementation of Testcontainers for Ignition by Inductive Automation"
        }
        withType<MavenPublication>{
            pom {
                name = "testcontainers-ignition"
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
                    url = "https://github.com/mussonindustrial/testcontainers-ignition"
                    connection = "scm:git:git://github.com/mussonindustrial/testcontainers-ignition.git"
                    developerConnection = "scm:git:ssh://github.com/mussonindustrial/testcontainers-ignition.git"
                }
            }
        }
    }
}


jreleaser {
    signing {
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("target/staging-deploy")
                }
            }
        }

    }
}