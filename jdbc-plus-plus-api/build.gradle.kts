plugins {
    java
}

group = "br.com.jdbcpp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.jspecify)

}

tasks.test {
    useJUnitPlatform()
}