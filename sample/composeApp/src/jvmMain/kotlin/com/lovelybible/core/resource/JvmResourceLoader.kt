package com.lovelybible.core.resource

/**
 * JVM 플랫폼용 리소스 로더
 */
class JvmResourceLoader : ResourceLoader {
    override fun loadBibleJson(): String {
        return Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("bible.json")
            ?.bufferedReader()
            ?.readText()
            ?: error("Bible JSON file not found in resources")
    }
}
