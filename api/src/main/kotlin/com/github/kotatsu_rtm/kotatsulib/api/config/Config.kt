package com.github.kotatsu_rtm.kotatsulib.api.config

interface Config<T : Any> {
    val relativePathFromGameDirectory: String
    val nativeInstance: T
}
