package com.coderGtm.yantra.commands.weather

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand

fun showAvailableFields(command: BaseCommand) {
    command.output(
        command.terminal.activity.getString(
            R.string.weather_available_fields,
            VALID_WEATHER_FIELDS.size
        ), command.terminal.theme.successTextColor, Typeface.BOLD
    )
    command.output("")

    WEATHER_FIELD_CATEGORIES.forEach { it.displayFields(command) }

    command.output(command.terminal.activity.getString(R.string.examples))
    command.output("  weather london -temp -humidity")
    command.output("  weather paris -uv -wind -condition")
    command.output("  weather tokyo -sunrise -sunset -moonphase")
    command.output("  weather denver -co -pm25 -aqi")
}

fun handleMissingLocation(command: BaseCommand) {
    command.output(
        command.terminal.activity.getString(R.string.please_specify_a_location),
        command.terminal.theme.errorTextColor
    )
}

fun handleValidationError(
    formatErrors: List<String>,
    invalidFields: List<String>,
    command: BaseCommand,
) {
    command.output(
        command.terminal.activity.getString(R.string.weather_invalid_command_format),
        command.terminal.theme.errorTextColor,
        Typeface.BOLD
    )
    formatErrors.forEach { error ->
        command.output("• $error", command.terminal.theme.errorTextColor)
    }
    invalidFields.forEach { field ->
        command.output(
            command.terminal.activity.getString(
                R.string.weather_unknown_field_bullet,
                field
            ), command.terminal.theme.errorTextColor
        )
    }
    command.output(
        command.terminal.activity.getString(R.string.weather_use_list_command),
        command.terminal.theme.warningTextColor
    )
}