package com.coderGtm.yantra.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coderGtm.yantra.services.TermuxCommandService
import org.json.JSONObject

class TermuxCommandResultReceiver(private val callback: (JSONObject) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val executionId = intent?.getIntExtra(TermuxCommandService.EXTRA_EXECUTION_ID, 0)
        val stdout = intent?.getStringExtra("stdout")
        val stderr = intent?.getStringExtra("stderr")
        val exitCode = intent?.getIntExtra("exitCode", -1)
        val errCode = intent?.getIntExtra("errCode", -1)
        val errmsg = intent?.getStringExtra("errmsg")

        val result = JSONObject().apply {
            put("executionId", executionId)
            put("stdout", stdout)
            put("stderr", stderr)
            put("exitCode", exitCode)
            put("errCode", errCode)
            put("errmsg", errmsg)
        }

        // Call the provided callback with the result
        callback(result)
    }
}