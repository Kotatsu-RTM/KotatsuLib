import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.libsDirectory
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    `maven-publish`
    signing
}

group = "com.github.kotatsu-rtm.kotatsulib"
version = "0.0.1-SNAPSHOT"

tasks {
    jar {
        enabled = false
    }

    withType<PublishToMavenRepository> {
        enabled = false
    }
}

allprojects {
    buildscript {
        repositories {
            maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
        }
    }

    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

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

        create<Jar>("sourcesJar") {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            destinationDirectory.set(rootProject.libsDirectory)

            archiveBaseName.set(rootProject.name)
            archiveAppendix.set(project.name)
            archiveVersion.set(rootProject.version.toString())
            archiveClassifier.set("sources")
            archiveExtension.set("jar")

            from(sourceSets.main.get().allSource)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("publication") {
                groupId = rootProject.group.toString()
                artifactId = "${rootProject.name}-${project.name}"
                version = rootProject.version.toString()

                from(components.getByName("java"))
                artifact(tasks.getByName("sourcesJar"))

                pom {
                    name.set(artifactId)
                    description.set(rootProject.description)
                    url.set("https://github.com/Kotatsu-RTM/KotatsuLib")

                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://opensource.org/licenses/mit-license.php")
                        }
                    }

                    developers {
                        developer {
                            id.set("Siro256")
                            name.set("Siro_256")
                            email.set("siro@siro256.dev")
                            url.set("https://github.com/Siro256")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/Kotatsu-RTM/KotatsuLib.git")
                        developerConnection.set("scm:git:ssh://github.com/Kotatsu-RTM/KotatsuLib.git")
                        url.set("https://github.com/Kotatsu-RTM/KotatsuLib.git")
                    }
                }
            }
        }

        repositories {
            maven {
                url =
                    if (version.toString().endsWith("SNAPSHOT")) {
                        uri("https://repo.siro256.dev/repository/maven-snapshots")
                    } else {
                        uri("https://repo.siro256.dev/repository/maven-public")
                    }

                credentials {
                    username = System.getenv("RepositoryUsername")
                    password = System.getenv("RepositoryPassword")
                }
            }
        }
    }

    signing {
        useInMemoryPgpKeys(
            System.getenv("SigningKeyId"),
            System.getenv("SigningKey"),
            System.getenv("SigningKeyPassword")
        )
        sign(publishing.publications.getByName("publication"))
    }
}
