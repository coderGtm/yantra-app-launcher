package com.coderGtm.yantra

import android.content.Context
import com.coderGtm.yantra.network.HttpClientProvider
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import java.util.concurrent.CountDownLatch

class LuaHttpAPI(context: Context) : LuaTable() {

    init {
        set("get", GetFunction())
        set("post", PostFunction())
        set("put", PutFunction())
        set("delete", DeleteFunction())
        set("patch", PatchFunction())
    }

    private inner class GetFunction : TwoArgFunction() {
        override fun call(url: LuaValue, options: LuaValue): LuaValue {
            val headers = if (options.istable()) options.get("headers").checktable() else LuaTable()
            val latch = OneShotLatch()
            val responseTable = LuaTable()

            requestJsonObject(HttpMethod.Get, url.checkjstring(), headers, null, responseTable, latch)

            latch.acquire()  // Block until the response is received
            return responseTable
        }
    }

    private inner class PostFunction : TwoArgFunction() {
        override fun call(url: LuaValue, options: LuaValue): LuaValue {
            val headers = if (options.istable()) options.get("headers").checktable() else LuaTable()
            val body = options.get("body").optjstring("")
            val latch = OneShotLatch()
            val responseTable = LuaTable()

            requestJsonObject(HttpMethod.Post, url.checkjstring(), headers, body, responseTable, latch)

            latch.acquire()  // Block until the response is received
            return responseTable
        }
    }

    private inner class PutFunction : TwoArgFunction() {
        override fun call(url: LuaValue, options: LuaValue): LuaValue {
            val headers = if (options.istable()) options.get("headers").checktable() else LuaTable()
            val body = options.get("body").optjstring("")
            val latch = OneShotLatch()
            val responseTable = LuaTable()

            requestJsonObject(HttpMethod.Put, url.checkjstring(), headers, body, responseTable, latch)

            latch.acquire()  // Block until the response is received
            return responseTable
        }
    }

    private inner class DeleteFunction : TwoArgFunction() {
        override fun call(url: LuaValue, options: LuaValue): LuaValue {
            val headers = if (options.istable()) options.get("headers").checktable() else LuaTable()
            val body = options.get("body").optjstring("")
            val latch = OneShotLatch()
            val responseTable = LuaTable()

            requestJsonObject(HttpMethod.Delete, url.checkjstring(), headers, body, responseTable, latch)

            latch.acquire()  // Block until the response is received
            return responseTable
        }
    }

    private inner class PatchFunction : TwoArgFunction() {
        override fun call(url: LuaValue, options: LuaValue): LuaValue {
            val headers = if (options.istable()) options.get("headers").checktable() else LuaTable()
            val body = options.get("body").optjstring("")
            val latch = OneShotLatch()
            val responseTable = LuaTable()

            requestJsonObject(HttpMethod.Patch, url.checkjstring(), headers, body, responseTable, latch)

            latch.acquire()  // Block until the response is received
            return responseTable
        }
    }

    private fun requestJsonObject(
        method: HttpMethod,
        url: String,
        headers: LuaTable,
        body: String?,
        responseTable: LuaTable,
        latch: OneShotLatch
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonBody = body?.takeIf { it.isNotBlank() }?.let { JSONObject(it).toString() }
                val responseText = HttpClientProvider.client.request(url) {
                    this.method = method
                    headers.keys().forEach { key ->
                        header(key.checkjstring(), headers.get(key).checkjstring())
                    }
                    if (jsonBody != null) {
                        contentType(ContentType.Application.Json)
                        setBody(jsonBody)
                    }
                }.bodyAsText()
                responseTable.set("body", toLuaTable(JSONObject(responseText)))
            } catch (e: Exception) {
                responseTable.set("error", valueOf(errorMessage(e)))
            } finally {
                latch.release()
            }
        }
    }

    private fun errorMessage(exception: Exception): String {
        val status = (exception as? ResponseException)?.response?.status
        val detail = exception.message?.takeIf { it.isNotBlank() }
            ?: exception::class.simpleName.orEmpty()
        return if (status != null) {
            "$status: $detail"
        } else {
            detail.ifBlank { "Unknown error" }
        }
    }

    private fun toLuaTable(json: JSONObject): LuaTable {
        val table = LuaTable()
        json.keys().forEach { key ->
            val value = json.get(key)
            table.set(key, convertToLua(value))
        }
        return table
    }

    private fun toLuaTable(array: JSONArray): LuaTable {
        val table = LuaTable()
        for (i in 0 until array.length()) {
            val value = array.get(i)
            table.set(i + 1, convertToLua(value))
        }
        return table
    }

    private fun convertToLua(value: Any): LuaValue {
        return when (value) {
            is JSONObject -> toLuaTable(value)
            is JSONArray -> toLuaTable(value)
            is String -> valueOf(value)
            is Number -> valueOf(value.toDouble())
            is Boolean -> valueOf(value)
            else -> NIL
        }
    }
}

private class OneShotLatch {
    private val latch = CountDownLatch(1)

    fun release() {
        latch.countDown()
    }

    fun acquire() {
        latch.await()
    }
}