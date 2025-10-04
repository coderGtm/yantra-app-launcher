package com.coderGtm.yantra.commands.weather

import android.content.Context
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand

sealed class ParseResult {
    data class Success(val args: WeatherCommandArgs) : ParseResult()
    object MissingLocation : ParseResult()
    data class ValidationError(val formatErrors: List<String>, val invalidFields: List<String>) :
        ParseResult()

    object ListCommand : ParseResult()
}

data class LocationAndFields(
    val location: String,
    val fields: List<String>,
)

data class WeatherCommandArgs(
    val location: String,
    val requestedFields: List<String>,
    val showDefaultFields: Boolean,
)

data class ValidationResult(
    val validFields: List<String>,
    val invalidFields: List<String>,
    val formatErrors: List<String>,
) {
    fun isValid(): Boolean {
        return formatErrors.isEmpty() && invalidFields.isEmpty()
    }
}


/**
 * This function takes a raw command string (e.g., "weather denver -temp -humidity") and attempts
 * to extract the location and requested weather fields.
 *
 * @param command The raw weather command string.
 * @return A [ParseResult] object representing the outcome of the parsing.
 */
fun parseWeatherCommand(command: String, context: Context): ParseResult {
    val args = command
        .trim()
        .split("\\s+".toRegex())

    if (args.size < 2) return ParseResult.MissingLocation

    if (args.size == 2 && args[1] == "list") return ParseResult.ListCommand

    val commandArgs = args.drop(1)

    // validate for field-like words in raw arguments before location extraction
    val earlyValidation = validateRawArgsForFieldLikeWords(commandArgs, context)
    if (earlyValidation.isNotEmpty()) {
        return ParseResult.ValidationError(earlyValidation, emptyList())
    }

    // extract location first to determine what needs validation
    val locationAndFields = extractLocationAndFields(commandArgs)

    // validate the extracted location for field-like words
    val locationValidation = validateLocationForFieldLikeWords(locationAndFields.location, context)
    if (locationValidation.isNotEmpty()) {
        return ParseResult.ValidationError(locationValidation, emptyList())
    }

    val firstFieldIndex = commandArgs.indexOfFirst { it.startsWith("-") }
    // validate arguments after location to catch misplaced non-hyphenated words
    val argsAfterLocation = if (firstFieldIndex == -1) emptyList() else commandArgs.drop(firstFieldIndex)
    val validationResult = validateWeatherFields(argsAfterLocation, context)

    if (!(validationResult.isValid())) {
        return ParseResult.ValidationError(
            validationResult.formatErrors,
            validationResult.invalidFields
        )
    }

    return when {
        locationAndFields.location.isEmpty() -> ParseResult.MissingLocation
        else -> ParseResult.Success(
            WeatherCommandArgs(
                locationAndFields.location,
                locationAndFields.fields,
                locationAndFields.fields.isEmpty()
            )
        )
    }
}

/**
 * This function checks each argument in the provided list to ensure it conforms to the expected
 * format for weather fields. It also verifies if the field name is a recognized weather field.
 *
 * @param args A list of strings, where each string is a valid or invalid weather field argument
 * (e.g., "-temp", "--humidity", "-").
 * @param context The [Context] instance for accessing string resources.
 * @return A [ValidationResult].
 */
fun validateWeatherFields(args: List<String>, context: Context): ValidationResult {
    val validFields = mutableListOf<String>()
    val invalidFields = mutableListOf<String>()
    val formatErrors = mutableListOf<String>()

    args.forEach { arg ->
        when {
            arg == "-" -> {
                formatErrors.add(
                    context.getString(R.string.weather_error_single_hyphen)
                )
            }

            arg.contains("--") -> {
                val correctFormat = "-${arg.substringAfterLast("-")}"
                formatErrors.add(
                    context.getString(
                        R.string.weather_error_multiple_hyphens,
                        arg,
                        correctFormat
                    )
                )
            }

            arg.startsWith("-") -> {
                val field = arg.removePrefix("-")
                if (field in VALID_WEATHER_FIELDS) {
                    validFields.add(field)
                } else {
                    // check if this might be combined fields separated by hyphens
                    val combinedFieldsResult = checkForCombinedFields(field, context)
                    if (combinedFieldsResult.isNotEmpty()) {
                        formatErrors.addAll(combinedFieldsResult)
                    } else {
                        invalidFields.add(field)
                    }
                }
            }

            else -> {
                formatErrors.add(
                    context.getString(
                        R.string.weather_error_misplaced_argument,
                        arg
                    )
                )
            }
        }
    }
    return ValidationResult(validFields, invalidFields, formatErrors)
}


/**
 * Extracts the location and requested weather fields from a list of command arguments.
 *
 * Note: This function assumes the arguments have already been validated by [validateWeatherFields].
 * It performs minimal validation and focuses on parsing/extraction.
 *
 * @param args The list of command arguments (pre-validated).
 * @return A [LocationAndFields] containing the extracted location string and a list of requested
 * weather field names (without the leading hyphen). If no fields are requested, the field list
 * will be empty.
 */
internal fun extractLocationAndFields(args: List<String>): LocationAndFields {
    val firstParamIndex = args.indexOfFirst { it.startsWith("-") }

    val locationParts = if (firstParamIndex == -1) args else args.take(firstParamIndex)
    val location = locationParts.joinToString(" ")

    val fields = if (firstParamIndex == -1) {
        emptyList()
    } else {
        args.drop(firstParamIndex)
            .filter { it.startsWith("-") }
            .map { it.removePrefix("-") }
            .filter { it.isNotEmpty() }
    }

    return LocationAndFields(location, fields)
}

/**
 * Validates raw command arguments for field-like words before location extraction.
 * This catches cases like "weather Paris temp -humidity" where "temp" appears to be a field.
 *
 * @param args The raw command arguments to validate
 * @param context The [Context] instance for accessing string resources
 * @return List of error messages, empty if arguments are valid
 */
fun validateRawArgsForFieldLikeWords(args: List<String>, context: Context): List<String> {
    val errors = mutableListOf<String>()
    val firstFieldIndex = args.indexOfFirst { it.startsWith("-") }

    // check words before first field for field-like names (or all words if no fields)
    val wordsBeforeFields = if (firstFieldIndex == -1) args else args.take(firstFieldIndex)

    // check words within the location part (excluding the very first word) for field-like patterns
    wordsBeforeFields.drop(1).forEach { word ->
        val normalizedWord = word.lowercase()
        if (normalizedWord in VALID_WEATHER_FIELDS) {
            if (firstFieldIndex == -1) {
                // no hyphenated fields present, suggest adding hyphens
                errors.add(
                    context.getString(
                        R.string.weather_error_field_in_location_no_hyphens,
                        word,
                        "-$word"
                    )
                )
            } else {
                // hyphenated fields present, this word should be part of location
                errors.add(
                    context.getString(
                        R.string.weather_error_field_in_location,
                        word,
                        "-$word"
                    )
                )
            }
        }
    }

    return errors
}

/**
 * Validates location string for field-like words that might indicate missing hyphens.
 *
 * @param location The extracted location string to validate
 * @param context The [Context] instance for accessing string resources
 * @return List of error messages, empty if location is valid
 */
fun validateLocationForFieldLikeWords(location: String, context: Context): List<String> {
    val locationWords = location.trim().split("\\s+".toRegex())
    val errors = mutableListOf<String>()

    // check each word in location against known field names. todo: is this brittle?
    locationWords.forEach { word ->
        val normalizedWord = word.lowercase()
        if (normalizedWord in VALID_WEATHER_FIELDS) {
            errors.add(
                context.getString(
                    R.string.weather_error_field_in_location,
                    word,
                    "-$word"
                )
            )
        }
    }

    return errors
}

/**
 * Checks if an invalid field might be multiple valid fields combined with hyphens.
 * For example, "uv-temp" should be "-uv -temp".
 *
 * @param field The field name to check
 * @param context The [Context] instance for accessing string resources
 * @return List of error messages suggesting correct format, empty if not combined fields
 */
private fun checkForCombinedFields(field: String, context: Context): List<String> {
    val errors = mutableListOf<String>()

    // split on hyphens and check if all parts are valid fields
    val parts = field.split("-")
    if (parts.size > 1) {
        val allPartsValid = parts.all { it.lowercase() in VALID_WEATHER_FIELDS }

        if (allPartsValid) {
            val suggestedFormat = parts.joinToString(" ") { "-$it" }
            errors.add(
                context.getString(
                    R.string.weather_error_combined_fields,
                    "-$field",
                    suggestedFormat
                )
            )
        }
    }

    return errors
}