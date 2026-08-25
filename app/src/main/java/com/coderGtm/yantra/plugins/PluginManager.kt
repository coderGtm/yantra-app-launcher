package com.coderGtm.yantra.plugins

import android.content.SharedPreferences
import android.util.Log
import com.coderGtm.yantra.terminal.Terminal

class PluginManager(private val preferenceObject: SharedPreferences) {
    private var plugins: List<YantraPlugin> = builtInPlugins()

    private val suggestionPlugins: List<SuggestionPlugin>
        get() = plugins.filterIsInstance<SuggestionPlugin>()

    fun reload() {
        plugins = builtInPlugins()
    }

    fun clearSuggestionPlugins(terminal: Terminal) {
        suggestionPlugins.forEach { plugin ->
            runCatching {
                plugin.clear(terminal)
            }.onFailure { error ->
                Log.w(TAG, "Plugin ${plugin.id} failed while clearing suggestions.", error)
            }
        }
    }

    fun onSuggestionsUpdated(terminal: Terminal, context: SuggestionPluginContext) {
        suggestionPlugins
            .filter { it.isEnabled(preferenceObject) }
            .forEach { plugin ->
                runCatching {
                    plugin.onSuggestionsUpdated(terminal, context)
                }.onFailure { error ->
                    Log.w(TAG, "Plugin ${plugin.id} failed while updating suggestions.", error)
                }
            }
    }

    companion object {
        private const val TAG = "PluginManager"
        const val HOST_PLUGIN_API_VERSION = 1

        fun builtInPlugins(): List<YantraPlugin> {
            return PluginCatalog.sourceDistributedPlugins()
        }

        fun compatibilityFor(plugin: YantraPlugin): PluginCompatibility {
            return when {
                plugin.minHostPluginApiVersion > HOST_PLUGIN_API_VERSION -> {
                    PluginCompatibility(
                        label = "Needs app update",
                        detail = "Requires plugin API ${plugin.minHostPluginApiVersion}; app has API $HOST_PLUGIN_API_VERSION.",
                        canEnable = false
                    )
                }

                plugin.targetHostPluginApiVersion < HOST_PLUGIN_API_VERSION -> {
                    PluginCompatibility(
                        label = "Legacy",
                        detail = "Built for plugin API ${plugin.targetHostPluginApiVersion}; app has API $HOST_PLUGIN_API_VERSION.",
                        canEnable = true
                    )
                }

                else -> {
                    PluginCompatibility(
                        label = "Compatible",
                        detail = "Built for plugin API ${plugin.targetHostPluginApiVersion}.",
                        canEnable = true
                    )
                }
            }
        }
    }
}
