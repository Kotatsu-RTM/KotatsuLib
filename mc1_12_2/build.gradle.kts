group = rootProject.group.toString() + ".core"

dependencies {
    api(project(":api"))
}

tasks {
    jar {
        from(
            configurations.api.get().apply { isCanBeResolved = true }.map { if (it.isDirectory) it else zipTree(it) }
        )
    }
}
