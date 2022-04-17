package ru.mephi.shared.data.network

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import kotlinx.serialization.json.Json
import ru.mephi.shared.data.network.exception.ForbiddenException
import ru.mephi.shared.data.network.exception.NetworkException
import ru.mephi.shared.data.network.exception.ServerNotRespondException
import ru.mephi.shared.data.network.exception.UndefinedException

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
        val timeout = 3000L
        return HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
            HttpResponseValidator {
                handleResponseException { exception ->
                    if (exception !is ClientRequestException) {
                        if (exception.message?.startsWith("Unable to resolve host") == true ||
                            exception.message?.startsWith("Request timeout") == true
                        )
                            throw NetworkException()
                        print("UndefinedErrorException from request: $exception")
                        throw UndefinedException()
                    }
                    val exceptionResponse = exception.response
                    println("handleResponseException: $exceptionResponse with status: ${exceptionResponse.status}")
                    when (exceptionResponse.status) {
                        HttpStatusCode.Forbidden -> throw ForbiddenException()
                        HttpStatusCode.BadGateway, HttpStatusCode.InternalServerError,
                        HttpStatusCode.NotImplemented, HttpStatusCode.ServiceUnavailable,
                        HttpStatusCode.GatewayTimeout -> {
                            throw ServerNotRespondException()
                        }
                    }
                    if (exceptionResponse.status != HttpStatusCode.OK)
                        throw NetworkException()
                }
            }
            expectSuccess = false
            defaultRequest {
                url.takeFrom(URLBuilder().takeFrom(Url(BASE_URL)).apply {
                    encodedPath += url.encodedPath
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = timeout
                connectTimeoutMillis = timeout
                socketTimeoutMillis = timeout
            }
            defaultRequest {
                parameter("api_key", API_KEY)
                if (this.method != HttpMethod.Get) contentType(Json)
                accept(Json)
            }
        }
    }
}