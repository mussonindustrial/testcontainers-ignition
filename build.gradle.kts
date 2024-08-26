import org.jreleaser.model.Active

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.spotless)
}

group = "com.mussonindustrial"
version = "0.1.0"
description = "An implementation of Testcontainers for Ignition by Inductive Automation."

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

spotless {
    java {
        importOrder()
        removeUnusedImports()
        palantirJavaFormat()
    }
}

tasks.build {
    dependsOn(tasks.spotlessCheck)
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

val stagingDir: Provider<Directory> = layout.buildDirectory.dir("staging-deploy")

publishing {
    repositories {
        maven {
            name = "MavenCentral"
            url = stagingDir.get().asFile.toURI()
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            description = project.description
            pom {
                name = project.name
                description = project.description
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
    project {
        authors.set(arrayListOf("Ben Musson"))
        license.set("MIT")
        inceptionYear = "2024"
    }
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
                    stagingRepository(stagingDir.get().toString())
                }
            }
        }

    }
}