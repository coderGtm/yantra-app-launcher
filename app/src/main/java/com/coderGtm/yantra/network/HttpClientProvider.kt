package com.coderGtm.yantra.network

import android.util.Log
import com.coderGtm.yantra.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Provides a singleton HttpClient instance for the application.
 * This ensures efficient resource usage and connection pool reuse across all network requests.
 */
object HttpClientProvider {

    val client: HttpClient by lazy {
        HttpClient(Android) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            Log.d("HTTP call", message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }
        }
    }
}