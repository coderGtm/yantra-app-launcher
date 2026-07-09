package com.coderGtm.yantra.plugins

import com.coderGtm.yantra.plugins.contrib.autocomplete_yyppsk_v0_1_0.InlineAutocompletePlugin

/**
 * Registry for source-distributed plugins that ship with the app.
 *
 * Contribution path:
 * 1. Add plugin source under:
 *    plugins/contrib/<feature>_<githubUsername>_<version>/
 *    Example: plugins/contrib/autocomplete_yyppsk_v0_1_0/
 * 2. Keep all plugin-specific files inside that folder whenever possible.
 * 3. Register the plugin constructor here so Yantra can discover it.
 *
 * This keeps plugin contributions isolated while avoiding dynamic code loading.
 */
object PluginCatalog {
    private val cachedSourceDistributedPlugins: List<YantraPlugin> by lazy {
        listOf(
            InlineAutocompletePlugin()
        )
    }

    fun sourceDistributedPlugins(): List<YantraPlugin> {
        return cachedSourceDistributedPlugins
    }
}
