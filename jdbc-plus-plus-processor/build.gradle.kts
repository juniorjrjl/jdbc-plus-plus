plugins {
    java
    jacoco
}

group = "br.com.jdbcpp"
version = "1.0.0"

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    annotationProcessor(libs.autoservice)

    compileOnly(libs.autoservice)

    implementation(project(":jdbc-plus-plus-api"))

    implementation(libs.javapoet)
    implementation(libs.jspecify)

    mockitoAgent(libs.mockito.core) { isTransitive = false }

    testImplementation(libs.assertj.core)
    testImplementation(libs.compileTesting)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockito.junit.jupiter)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

tasks.withType<Test>().configureEach {
    jvmArgs(
        "-javaagent:${configurations.getByName("mockitoAgent").asPath}"
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    finalizedBy(tasks.jacocoTestReport)
    //systemProperty("test.seed", System.getProperty("test.seed") ?: "")
}