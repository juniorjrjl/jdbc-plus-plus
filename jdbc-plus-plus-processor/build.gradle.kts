plugins {
    java
}

group = "br.com.jdbcpp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.autoservice)

    compileOnly(libs.autoservice)

    implementation(project(":jdbc-plus-plus-api"))

    implementation(libs.javapoet)
    implementation(libs.jspecify)
}

tasks.test {
    useJUnitPlatform()
}