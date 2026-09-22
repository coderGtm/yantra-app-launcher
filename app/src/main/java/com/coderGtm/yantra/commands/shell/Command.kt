package com.coderGtm.yantra.commands.shell

import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.core.runtime.LauncherRuntime
import com.coderGtm.yantra.gui.package_manager.GuiPackage
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(
    terminal: Terminal
) : BaseCommand(terminal) {

    override val metadata = CommandMetadata(
        name = "shell",
        helpTitle = "shell",
        description = "Manage launcher shells and GUI containers."
    )

    override fun execute(command: String) {
        val args = command.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (args.isEmpty()) return

        when (args.size) {
            1 -> showShellManager()
            2 -> handleSingleArgument(args[1])
            3 -> handleDoubleArgument(args[1], args[2])
            else -> output("Too many arguments.", terminal.theme.errorTextColor)
        }
    }

    private fun handleSingleArgument(argument: String) {
        when (argument.lowercase()) {
            "cli" -> switchTo("cli")
            "gui" -> switchTo("gui")
            "list" -> showShellList()
            "reload" -> reloadCurrentShell()
            "import" -> startImport()
            "-config" -> showConfig()
            "-info" -> showShellInfo(null)
            else -> switchTo(argument)
        }
    }

    private fun handleDoubleArgument(option: String, value: String) {
        when (option.lowercase()) {
            "-info" -> showShellInfo(value)
            "-remove" -> removeShell(value)
            else -> output(
                "Unknown shell option: $option\n\nUsage\n\nshell <name>\nshell import\nshell -info\nshell -info <name>\nshell -remove <name>\nshell -config",
                terminal.theme.errorTextColor
            )
        }
    }

    private fun showShellManager() {
        val current = LauncherRuntime.shellManager.getCurrentShellName()
        val allRegistered = LauncherRuntime.listShells()
        val imported = guiRegistry()?.all().orEmpty()
        val importedIds = imported.map { it.id.lowercase() }.toSet()

        val builder = StringBuilder()
        builder.append("YANTRA SHELL MANAGER\n\n")
        builder.append("Current Shell\n")
        builder.append("─────────────\n")
        builder.append(current).append("\n\n")

        builder.append("Available Shells\n")
        builder.append("────────────────\n")
        
        // List built-ins (those not in guiRegistry)
        allRegistered.filter { it.lowercase() !in importedIds }.forEach {
            appendShellLine(builder, it, it == current)
        }
        
        // List imported
        imported.forEach { appendShellLine(builder, it.id, it.id == current) }

        builder.append("\nCommands\n")
        builder.append("────────\n")
        builder.append("shell <name>\n")
        builder.append("shell import\n")
        builder.append("shell -info [name]\n")
        builder.append("shell -remove <name>\n")
        builder.append("shell -config\n")

        output(builder.toString())
    }

    private fun appendShellLine(builder: StringBuilder, name: String, current: Boolean) {
        builder.append(if (current) "✓ " else "• ").append(name).append('\n')
    }

    private fun showShellList() {
        val allRegistered = LauncherRuntime.listShells()
        val imported = guiRegistry()?.all().orEmpty()
        val importedIds = imported.map { it.id.lowercase() }.toSet()

        val builder = StringBuilder("Available Shells\n────────────────\n")
        
        // Built-ins
        allRegistered.filter { it.lowercase() !in importedIds }.forEach {
            builder.append("• ").append(it).append('\n')
        }
        
        // Imported
        imported.forEach {
            builder.append("• ").append(it.id).append("  ").append(it.name).append('\n')
        }
        output(builder.toString())
    }

    private fun switchTo(name: String) {
        val success = LauncherRuntime.switchShell(name)
        if (success) {
            output("✓ Switched to shell: $name")
            return
        }

        val imported = guiRegistry()?.get(name)
        if (imported != null) {
            output(
                "GUI '${imported.id}' is installed, but its WebView shell runtime is not enabled yet.\nImport completed successfully; runtime hookup is the next step.",
                terminal.theme.resultTextColor
            )
            return
        }

        output("✗ Shell not found: $name", terminal.theme.errorTextColor)
    }

    private fun startImport() {
        val activity = terminal.activity as? MainActivity
        if (activity == null) {
            output("✗ GUI import is unavailable in this activity.", terminal.theme.errorTextColor)
            return
        }

        output("Select a GUI folder containing manifest.json ...")
        activity.importGuiPackage { result ->
            if (result.success) {
                val pkg = result.packageInfo!!

                // Register the newly imported GUI immediately.
                // It stays in CLI until the user explicitly switches to it.
                activity.registerImportedGuiShell(pkg)

                output(
                    "✓ GUI imported successfully.\n\n" +
                            "Name    : ${pkg.name}\n" +
                            "ID      : ${pkg.id}\n" +
                            "Version : ${pkg.version}\n" +
                            "Author  : ${pkg.author}\n\n" +
                            "The imported GUI remains in CLI. Use:\n" +
                            "shell ${pkg.id}",
                    terminal.theme.successTextColor
                )
            } else {
                output(
                    "✗ GUI import rejected.\n\n" + result.errors.joinToString("\n• ", prefix = "• "),
                    terminal.theme.errorTextColor
                )
            }
        }
    }

    private fun showShellInfo(name: String?) {
        val target = name ?: LauncherRuntime.shellManager.getCurrentShellName()
        val imported = guiRegistry()?.get(target)

        if (imported != null) {
            output(formatGuiInfo(imported))
            return
        }

        if (LauncherRuntime.hasShell(target)) {
            output(
                "Shell Information\n─────────────────\n\n" +
                        "ID      : $target\n" +
                        "Type    : built-in\n" +
                        "Status  : available\n" +
                        "Protected: yes"
            )
            return
        }

        output("✗ Shell not found: $target", terminal.theme.errorTextColor)
    }

    private fun formatGuiInfo(pkg: GuiPackage): String = buildString {
        append("GUI Information\n")
        append("────────────────\n\n")
        append("Name      : ").append(pkg.name).append('\n')
        append("ID        : ").append(pkg.id).append('\n')
        append("Version   : ").append(pkg.version).append('\n')
        append("Author    : ").append(pkg.author).append('\n')
        append("API       : ").append(pkg.api).append('\n')
        append("Entry     : ").append(pkg.entry).append('\n')
        append("Stylesheet: ").append(pkg.stylesheet).append('\n')
        append("Script    : ").append(pkg.script).append('\n')
        append("Wallpaper : ").append(pkg.wallpaper)
    }

    private fun removeShell(name: String) {
        if (name.equals("cli", true) || name.equals("gui", true)) {
            output("✗ Cannot remove protected built-in shell: $name", terminal.theme.errorTextColor)
            return
        }

        val registry = guiRegistry()
        if (registry?.get(name) == null) {
            output("✗ User GUI not found: $name", terminal.theme.errorTextColor)
            return
        }

        if (LauncherRuntime.shellManager.getCurrentShellName().equals(name, true)) {
            LauncherRuntime.switchShell("cli")
        }

        if (registry.remove(name)) {
            (terminal.activity as? MainActivity)?.unregisterImportedGuiShell(name)
            output("✓ Removed GUI shell: $name", terminal.theme.successTextColor)
        } else {
            output("✗ Failed to remove GUI shell: $name", terminal.theme.errorTextColor)
        }
    }

    private fun reloadCurrentShell() {
        val current = LauncherRuntime.shellManager.getCurrentShellName()
        LauncherRuntime.switchShell("cli")
        LauncherRuntime.switchShell(current)
        output("✓ Reloaded shell: $current")
    }

    private fun showConfig() {
        output(
            "SHELL CONFIG\n\n" +
                    "1. Shell Import\n" +
                    "2. Wallpaper\n" +
                    "3. Package Attributes List\n" +
                    "4. Documentation Link\n" +
                    "5. Sample GUI.zip Link\n\n" +
                    "Interactive config UI will be connected here next."
        )
    }

    private fun guiRegistry() = (terminal.activity as? MainActivity)?.getGuiRegistry()
}
