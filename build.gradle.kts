plugins {
    java
}

group = "br.com.jdbcpp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
}