group = rootProject.group.toString() + ".api"

dependencies {
    api(kotlin("stdlib"))
    api("io.arrow-kt:arrow-core:1.1.5")
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
