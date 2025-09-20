package com.coderGtm.yantra.commands.weather

import android.util.Log
import com.coderGtm.yantra.BuildConfig
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.logging.Logger
import kotlinx.serialization.json.Json

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "weather",
        helpTitle = terminal.activity.getString(R.string.cmd_weather_title),
        description = terminal.activity.getString(R.string.cmd_weather_help)
    )

    // todo: ideally this would be a singleton that is re-used across the app
    private val httpClient = HttpClient(Android) {
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

    override fun execute(command: String) {
        when (val parseResult = parseWeatherCommand(command, this.terminal.activity)) {
            is ParseResult.MissingLocation -> handleMissingLocation(this)
            is ParseResult.ValidationError -> handleValidationError(
                parseResult.formatErrors,
                parseResult.invalidFields,
                this
            )

            is ParseResult.ListCommand -> showAvailableFields(this)
            is ParseResult.Success -> fetchWeatherData(parseResult.args, this, httpClient)
        }
    }
}