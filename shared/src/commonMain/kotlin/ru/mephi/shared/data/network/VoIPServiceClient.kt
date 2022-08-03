package ru.mephi.shared.data.network

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
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
    coerceInputValues = true
    encodeDefaults = false
    useAlternativeNames = false
}

internal val VoIPServiceClient = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(json)
    }
    install(Logging) {
        logger = object: Logger {
            override fun log(message: String) {
                println(message)
            }
        }
        level = LogLevel.ALL
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
        url.takeFrom(URLBuilder().takeFrom(Url(VOIP_MEPHI_URL)).apply {
            encodedPath += url.encodedPath
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT
        connectTimeoutMillis = REQUEST_TIMEOUT
        socketTimeoutMillis = REQUEST_TIMEOUT
    }
    defaultRequest {
        parameter("api_key", API_KEY)
        if (this.method != HttpMethod.Get) contentType(Json)
        accept(Json)
    }
}
