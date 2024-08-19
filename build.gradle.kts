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
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mussonindustrial/testcontainers-ignition")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
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