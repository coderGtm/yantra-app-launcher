package com.coderGtm.yantra.commands.weather

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "weather",
        helpTitle = terminal.activity.getString(R.string.cmd_weather_title),
        description = terminal.activity.getString(R.string.cmd_weather_help)
    )


    override fun execute(command: String) {
        when (val parseResult = parseWeatherCommand(command, this.terminal.activity)) {
            is ParseResult.MissingLocation -> handleMissingLocation(this)
            is ParseResult.ValidationError -> handleValidationError(
                parseResult.formatErrors,
                parseResult.invalidFields,
                this
            )

            is ParseResult.ListCommand -> showAvailableFields(this)
            is ParseResult.Success -> fetchWeatherData(parseResult.args, this)
        }
    }
}