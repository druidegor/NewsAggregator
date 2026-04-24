package org.newsagg.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform