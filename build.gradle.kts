import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.libsDirectory
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
}

group = "com.github.kotatsu-rtm.kotatsulib"
version = "0.0.1-SNAPSHOT"

tasks.jar {
    enabled = false
}

allprojects {
    buildscript {
        repositories {
            maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
        }
    }

    apply(plugin = "kotlin")

    version = rootProject.version

    repositories {
        maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }

    kotlin {
        jvmToolchain(8)
    }

    tasks {
        create<Copy>("includeReadmeAndLicense") {
            if (project == rootProject) this.enabled = false

            destinationDir = File(project.buildDir, "resources/main")

            from(rootProject.file("LICENSE")) {
                rename { "LICENSE_${rootProject.name}" }
            }

            from(rootProject.file("README.md")) {
                rename { "README_${rootProject.name}.md" }
            }

            processResources.get().finalizedBy(this)
        }

        withType<KotlinCompile> {
            kotlinOptions.apply {
                freeCompilerArgs =
                    mutableListOf(*kotlinOptions.freeCompilerArgs.toTypedArray())
                        .apply { add("-opt-in=kotlin.RequiresOptIn") }
                allWarningsAsErrors = true
            }
        }

        @Suppress("UnstableApiUsage")
        withType<ProcessResources> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            destinationDirectory.set(rootProject.libsDirectory)

            archiveBaseName.set(rootProject.name)
            archiveAppendix.set(project.name)
            archiveVersion.set(rootProject.version.toString())
            archiveClassifier.set("")
            archiveExtension.set("jar")

            dependsOn("includeReadmeAndLicense")
        }
    }
}
