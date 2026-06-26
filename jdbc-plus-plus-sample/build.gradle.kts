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
}

tasks.test {
    useJUnitPlatform()
}