package org.newsagg.project.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest

import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.newsagg.project.BuildKonfig
import org.newsagg.project.data.network.NewsApiImpl
import org.newsagg.project.data.network.api.NewsApi


val networkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            defaultRequest {
                url("https://newsapi.org/v2/")
                header("X-Api-Key", BuildKonfig.NEWS_API_KEY)
            }
        }
    }

    single<NewsApi> { NewsApiImpl(get()) }

}