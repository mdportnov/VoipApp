package ru.mephi.shared.data.network

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = false
    useAlternativeNames = false
}

object KtorClientBuilder {
    private const val BASE_URL = "https://sd.mephi.ru/api/6/"
    private const val API_KEY = "theiTh0Wohtho\$uquie)kooc"

    const val PHOTO_REQUEST_URL_BY_PHONE = // берет фотку из ru.mephi.voip
        BASE_URL + "get_photo_mobile.jpg?api_key=$API_KEY&phone="
    const val PHOTO_REQUEST_URL_BY_GUID = // берет фотку из cps
        BASE_URL + "get_photo_mobile.jpg?api_key=$API_KEY&EmpGUID="

    fun createHttpClient(): HttpClient {
        return HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
            expectSuccess = false
            defaultRequest {
                url.takeFrom(URLBuilder().takeFrom(Url(BASE_URL)).apply {
                    encodedPath += url.encodedPath
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 15000L
            }
            defaultRequest {
                parameter("api_key", API_KEY)
                if (this.method != HttpMethod.Get) contentType(Json)
                accept(Json)
            }
        }
    }
}