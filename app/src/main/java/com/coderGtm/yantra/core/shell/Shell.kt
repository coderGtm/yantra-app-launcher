package com.coderGtm.yantra.core.shell

interface Shell {

    fun initialize()

    fun resume()

    fun pause()

    fun destroy()

    /**
     * Called when the Android back button is pressed.
     * @return true if the shell consumed the event, false otherwise.
     */
    fun onBackPressed(): Boolean

}