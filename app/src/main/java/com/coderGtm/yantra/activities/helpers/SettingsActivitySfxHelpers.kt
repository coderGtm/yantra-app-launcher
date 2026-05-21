package com.coderGtm.yantra.activities.helpers

import android.content.Intent
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.SettingsActivity
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.misc.getSoundEffects

internal fun SettingsActivity.openSoundEffectsList() {
    val effects = getSoundEffects(this)
    YantraLauncherDialog(this).selectItem(
        title        = getString(R.string.manage_sound_effects),
        items        = effects.toTypedArray(),
        emptyMessage = getString(R.string.no_sound_effects_found),
        clickAction  = { which ->
            val sound = effects[which]
            YantraLauncherDialog(this).showInfo(
                title          = sound,
                message        = getString(R.string.delete_sound_effect),
                positiveButton = getString(R.string.delete),
                positiveAction = {
                    val files = listOf("$sound.mp3", "$sound.wav", "$sound.ogg")
                    filesDir.listFiles()?.find { it.name in files }?.delete()
                },
                negativeButton = getString(R.string.cancel)
            )
        },
        positiveButton = getString(R.string.add),
        positiveAction = {
            YantraLauncherDialog(this).showInfo(
                title          = getString(R.string.add_sound_effect),
                message        = getString(R.string.add_sfx_desc),
                positiveButton = getString(R.string.add),
                positiveAction = {
                    selectSfxLauncher.launch(
                        Intent.createChooser(
                            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "audio/*"
                            },
                            getString(R.string.select_sfx_file)
                        )
                    )
                },
            )
        },
        negativeButton = getString(R.string.cancel)
    )
}

