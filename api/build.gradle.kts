group = rootProject.name + ".core"

dependencies {
    api(kotlin("stdlib"))
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
        configurations.api.get().copy().apply { isCanBeResolved = true }.map { if (it.isDirectory) it else zipTree(it) }
    }
}
