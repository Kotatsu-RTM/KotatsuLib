rootProject.name = "KotatsuLib"

pluginManagement {
    repositories {
        maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
    }

    plugins {
        kotlin("jvm") version "1.8.21"
    }
}

include("api")
