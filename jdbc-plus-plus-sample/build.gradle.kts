plugins {
    id("java")
}

group = "br.com.jdbcpp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jdbc-plus-plus-api"))

    annotationProcessor(project(":jdbc-plus-plus-processor"))

    testAnnotationProcessor(project(":jdbc-plus-plus-processor"))

    testImplementation(project(":jdbc-plus-plus-api"))
    testImplementation(libs.assertj.core)
    testImplementation(libs.datafaker)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.mysql)
    testImplementation(libs.flyway.sqlserver)
    testImplementation(libs.flyway.postgresql)
    testImplementation(libs.flyway.oracle)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mysql)
    testImplementation(libs.sqlserver)
    testImplementation(libs.postgresql)
    testImplementation(libs.oracle)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.oracle)
    testImplementation(libs.testcontainers.sqlserver)
    testImplementation(libs.testcontainers.postgresql)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

}

tasks.test {
    useJUnitPlatform()
    systemProperty("test.seed", System.getProperty("test.seed") ?: "")
}
