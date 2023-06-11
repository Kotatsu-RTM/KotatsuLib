import net.minecraftforge.gradle.userdev.UserDevExtension

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
    api(project(":core"))
    api(project(":api"))

    add("minecraft", "net.minecraftforge:forge:1.12.2-14.23.5.2860")
}

configure<UserDevExtension> {
    mappings("snapshot", "20171003-1.12")
}

tasks {
    jar {
        from(
            configurations.api.get().apply { isCanBeResolved = true }.map { if (it.isDirectory) it else zipTree(it) }
        )
    }
}
