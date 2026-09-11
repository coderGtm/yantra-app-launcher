package com.coderGtm.yantra.commands.list

import android.content.pm.ApplicationInfo
import android.os.Build

object AppCategories {
    const val OTHER = "Other"
    val ALL: List<String> = listOf(
        "Accessibility",
        "Audio",
        "Games",
        "Images",
        "Maps",
        "News",
        "Productivity",
        "Social",
        "Video",
        OTHER
    )

    fun fromApplicationInfoCategory(category: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return OTHER
        return when (category) {
            ApplicationInfo.CATEGORY_GAME -> "Games"
            ApplicationInfo.CATEGORY_AUDIO -> "Audio"
            ApplicationInfo.CATEGORY_VIDEO -> "Video"
            ApplicationInfo.CATEGORY_IMAGE -> "Images"
            ApplicationInfo.CATEGORY_SOCIAL -> "Social"
            ApplicationInfo.CATEGORY_NEWS -> "News"
            ApplicationInfo.CATEGORY_MAPS -> "Maps"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
            ApplicationInfo.CATEGORY_ACCESSIBILITY -> "Accessibility"
            else -> OTHER
        }
    }
}

data class CategorizedApp(
    val appName: String,
    val packageName: String,
    val category: String
)

data class CategoryGroup(
    val category: String,
    val apps: List<CategorizedApp>
)

fun groupAppsByCategory(apps: List<CategorizedApp>): List<CategoryGroup> {
    return apps.groupBy { it.category }
        .map { (category, list) ->
            CategoryGroup(category, list.sortedBy { it.appName.lowercase() })
        }
        .sortedWith(compareBy({ it.category == AppCategories.OTHER }, { it.category }))
}

fun normalizeCategoryFilter(input: String): String? {
    val trimmed = input.trim()
    return AppCategories.ALL.firstOrNull { it.equals(trimmed, ignoreCase = true) }
}

sealed interface ListAppsArgs {
    data class Valid(val filter: String?, val showPackages: Boolean) : ListAppsArgs
    data object Invalid : ListAppsArgs
}

fun parseListAppsArgs(args: List<String>): ListAppsArgs {
    if (args.isEmpty()) return ListAppsArgs.Valid(null, false)
    if (args.size == 1) {
        if (args[0].equals("-p", ignoreCase = true)) return ListAppsArgs.Valid(null, true)
        val category = normalizeCategoryFilter(args[0])
        if (category != null) return ListAppsArgs.Valid(category, false)
        return ListAppsArgs.Invalid
    }
    if (args.size == 2) {
        val category = normalizeCategoryFilter(args[0])
        if (category != null && args[1].equals("-p", ignoreCase = true)) {
            return ListAppsArgs.Valid(category, true)
        }
        return ListAppsArgs.Invalid
    }
    return ListAppsArgs.Invalid
}

fun formatAppLine(app: CategorizedApp, showPackages: Boolean): String {
    return if (showPackages) "- ${app.appName} (${app.packageName})" else "- ${app.appName}"
}

fun formatCategoryTitle(group: CategoryGroup, count: Int = group.apps.size): String {
    return "${group.category} ($count)"
}
