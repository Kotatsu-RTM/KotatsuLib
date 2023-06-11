import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

group = rootProject.group.toString() + ".core"

dependencies {
    api(project(":api"))
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
