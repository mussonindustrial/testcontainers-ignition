import com.diffplug.spotless.LineEnding
import org.jreleaser.model.Active

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.spotless)
}

group = "com.mussonindustrial"
version = "0.5.0"
description = "An implementation of Testcontainers for Ignition by Inductive Automation."

repositories {
    mavenCentral()
}

dependencies {
    api(libs.testcontainers)

    testRuntimeOnly(libs.junit.platform)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.params)
    testImplementation(libs.slf4j)
    testImplementation(libs.eclipse.milo)
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
        palantirJavaFormat("2.83.0")
        lineEndings = LineEnding.UNIX
    }
}

tasks.build {
    dependsOn(tasks.spotlessCheck)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    maxHeapSize = "1G"
    maxParallelForks = Runtime.getRuntime()
        .availableProcessors()
        .div(2)
        .coerceAtLeast(1)

    forkEvery = 1

    // Use the combined report instead.
    reports.html.required = false

    testLogging {
        showStandardStreams = true
    }
}

val unitTest = tasks.named<Test>("test") {
    description = "Runs unit and configuration tests"

    useJUnitPlatform {
        excludeTags("integration")
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs Ignition container integration tests"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("integration")
    }

    shouldRunAfter(unitTest)

    // Avoid starting multiple Ignition containers concurrently.
    maxParallelForks = 1
}

val allTestReport = tasks.register<TestReport>("allTestReport") {
    description = "Generates a combined report for all tests"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    dependsOn(unitTest, integrationTest)

    destinationDirectory =
        layout.buildDirectory.dir("reports/tests/all")

    testResults.from(
        unitTest.flatMap { it.binaryResultsDirectory },
        integrationTest.flatMap { it.binaryResultsDirectory },
    )
}

val allTests = tasks.register("allTests") {
    description = "Runs all unit and integration tests"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    dependsOn(allTestReport)
}

tasks.named("check") {
    dependsOn(allTests)
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
