package com.coderGtm.yantra.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.coderGtm.yantra.services.PluginResultsService

class TermuxCommandResultReceiver(private val callback: (String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val executionId = intent?.getIntExtra(PluginResultsService.EXTRA_EXECUTION_ID, 0)
        val stdout = intent?.getStringExtra("stdout")
        val stderr = intent?.getStringExtra("stderr")
        val exitCode = intent?.getIntExtra("exitCode", -1)
        val errCode = intent?.getIntExtra("errCode", -1)
        val errmsg = intent?.getStringExtra("errmsg")

        val result = """
            Execution id $executionId result:
            stdout: $stdout
            stderr: $stderr
            exitCode: $exitCode
            errCode: $errCode
            errmsg: $errmsg
        """.trimIndent()

        Log.d("CommandResultReceiver", result)
        //Log.d("CommandResultReceiver", result)

        // Call the provided callback with the result
        callback(result)
    }
}