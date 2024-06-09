package com.coderGtm.yantra.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class PluginResultsService : IntentService(PLUGIN_SERVICE_LABEL) {
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        Log.d(LOG_TAG, PLUGIN_SERVICE_LABEL + " received execution result 1")

        val resultBundle = intent.getBundleExtra("result")
        if (resultBundle == null) {
            Log.e(
                LOG_TAG,
                ("The intent does not contain the result bundle at the \"" + "result").toString() + "\" key."
            )
            return
        }

        val executionId = intent.getIntExtra(EXTRA_EXECUTION_ID, 0)

        val stdout = resultBundle.getString("stdout", "")
        val stderr = resultBundle.getString("stderr", "")
        val exitCode = resultBundle.getInt("exitCode")
        val errCode = resultBundle.getInt("err")
        val errmsg = resultBundle.getString("errmsg", "")

        Log.d(LOG_TAG, "Broadcasting command result for execution id $executionId")

        val resultIntent = Intent(ACTION_COMMAND_RESULT).apply {
            putExtra(EXTRA_EXECUTION_ID, executionId)
            putExtra("stdout", stdout)
            putExtra("stderr", stderr)
            putExtra("exitCode", exitCode)
            putExtra("errCode", errCode)
            putExtra("errmsg", errmsg)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent)
    }

    companion object {
        const val EXTRA_EXECUTION_ID: String = "execution_id"
        const val ACTION_COMMAND_RESULT: String = "TERMUX.ACTION_COMMAND_RESULT"
        private var EXECUTION_ID = 1000
        const val PLUGIN_SERVICE_LABEL: String = "PluginResultsService"
        private const val LOG_TAG = "PluginResultsService"

        @Synchronized
        fun getNextExecutionId(): Int = EXECUTION_ID++
    }
}
