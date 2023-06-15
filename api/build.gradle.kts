group = rootProject.group.toString() + ".api"

dependencies {
    api(kotlin("stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.5.1")
    api("io.arrow-kt:arrow-core:1.1.5")
    api("dev.siro256.forgelib:rtm-glsl:0.1.0-SNAPSHOT")
    api("org.joml:joml:1.10.5")

    compileOnly("org.lwjgl.lwjgl:lwjgl:2.9.3")
}

tasks {
    val temporarySourceDirectory = File(buildDir, "tmpSrc/main/kotlin/")

    create<Copy>("cloneSource") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(File(projectDir, "src/main/kotlin/"))
        into(temporarySourceDirectory)
    }

    compileKotlin {
        doFirst {
            sourceSets.main.get().kotlin.setSrcDirs(temporarySourceDirectory.toPath())
        }

        dependsOn("cloneSource")
    }

    jar {
        from(
            configurations.api.get().apply { isCanBeResolved = true }.map { if (it.isDirectory) it else zipTree(it) }
        )
    }
}
