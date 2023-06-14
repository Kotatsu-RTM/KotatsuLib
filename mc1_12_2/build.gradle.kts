import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

group = rootProject.group.toString() + ".core"

buildscript {
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1.69") {
            isChanging = true
        }
    }
}

apply(plugin = "net.minecraftforge.gradle")

dependencies {
    val forgeDependencyManager =
        project.extensions[DependencyManagementExtension.EXTENSION_NAME] as DependencyManagementExtension

    api(project(":core"))
    api(project(":api"))

    add("minecraft", "net.minecraftforge:forge:1.12.2-14.23.5.2860")

    compileOnly(forgeDependencyManager.deobf("curse.maven:ngtlib-288989:3873392"))
    compileOnly(forgeDependencyManager.deobf("curse.maven:realtrainmod-288988:3873403"))
}

configure<UserDevExtension> {
    mappings("snapshot", "20171003-1.12")
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

    processResources {
        filesMatching(listOf("mcmod.info")) {
            filter<ReplaceTokens>("tokens" to replaceMap)
        }
    }

    jar {
        from(
            configurations.api.get().apply { isCanBeResolved = true }.map { if (it.isDirectory) it else zipTree(it) }
        )
    }
}
