/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.3/userguide/building_java_projects.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`
}
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/K0tBegemot/DIS")
            credentials {
                username = System.getenv("K0TBEGEM0T_USERNAME")
                password = System.getenv("K0TBEGEM0T_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            groupId  = "com.hashapi.hashapi"
            artifactId  = "api"
            version  = "1.1"
        }
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.9.3")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.register("printEnvironment") {
    doFirst {
        System.getenv().forEach { t, u ->
            project.logger.lifecycle("$t -> $u")
        }
        project.logger.lifecycle(System.getenv("SYSTEMX_USERNAME"))
        project.logger.lifecycle(System.getenv("SYSTEMX_PASSWORD"))
    }
}