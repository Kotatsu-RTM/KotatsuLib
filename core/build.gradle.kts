import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

group = rootProject.group.toString() + ".core"

dependencies {
    api(project(":api"))

    compileOnly("org.lwjgl.lwjgl:lwjgl:2.9.3")
    compileOnly("org.apache.logging.log4j:log4j-core:2.20.0")
}

tasks {
    val temporarySourceDirectory = File(buildDir, "tmpSrc/main/kotlin/")
    val replaceMap =
        mapOf(
            "modId" to rootProject.name.toLowerCaseAsciiOnly(),
            "modName" to rootProject.name,
            "modVersion" to rootProject.version
        )

    create<Copy>("cloneSource") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(File(projectDir, "src/main/kotlin/"))
        into(temporarySourceDirectory)
        filter<ReplaceTokens>("tokens" to replaceMap)
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
