plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.hash-manager"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/K0tBegemot/DIS")
        credentials {
            username = System.getenv("K0TBEGEM0T_USERNAME")
            password = System.getenv("K0TBEGEM0T_TOKEN")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.testcontainers:postgresql")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    implementation("com.github.dpaukov:combinatoricslib3:3.4.0")
    annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen:6.4.4.Final")
    implementation("org.hibernate:hibernate-core:6.4.4.Final")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.atomikos:transactions-jta:6.0.0")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springframework.retry:spring-retry:2.0.5")
    implementation("com.hashapi.hashapi:api:1.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
