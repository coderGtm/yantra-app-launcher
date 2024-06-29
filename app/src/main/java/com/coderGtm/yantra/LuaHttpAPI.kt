import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONArray
import org.json.JSONObject
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import java.util.concurrent.CountDownLatch

class LuaHttpAPI(context: Context) : LuaTable() {

    init {
        AndroidNetworking.initialize(context)
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

            val requestBuilder = AndroidNetworking.get(url.checkjstring())
            headers.keys().forEach { key ->
                requestBuilder.addHeaders(key.checkjstring(), headers.get(key).checkjstring())
            }

            requestBuilder.build().getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(jsonResponse: JSONObject?) {
                    if (jsonResponse != null) {
                        responseTable.set("body", toLuaTable(jsonResponse))
                    }
                    latch.release()
                }

                override fun onError(error: ANError) {
                    responseTable.set("error", LuaValue.valueOf(error.errorDetail))
                    latch.release()
                }
            })

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

            val requestBuilder = AndroidNetworking.post(url.checkjstring())
            headers.keys().forEach { key ->
                requestBuilder.addHeaders(key.checkjstring(), headers.get(key).checkjstring())
            }

            requestBuilder.addJSONObjectBody(JSONObject(body))

            requestBuilder.build().getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(jsonResponse: JSONObject?) {
                    if (jsonResponse != null) {
                        responseTable.set("body", toLuaTable(jsonResponse))
                    }
                    latch.release()
                }

                override fun onError(error: ANError) {
                    responseTable.set("error", LuaValue.valueOf(error.errorDetail))
                    latch.release()
                }
            })

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

            val requestBuilder = AndroidNetworking.put(url.checkjstring())
            headers.keys().forEach { key ->
                requestBuilder.addHeaders(key.checkjstring(), headers.get(key).checkjstring())
            }

            requestBuilder.addJSONObjectBody(JSONObject(body))

            requestBuilder.build().getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(jsonResponse: JSONObject?) {
                    if (jsonResponse != null) {
                        responseTable.set("body", toLuaTable(jsonResponse))
                    }
                    latch.release()
                }

                override fun onError(error: ANError) {
                    responseTable.set("error", LuaValue.valueOf(error.errorDetail))
                    latch.release()
                }
            })

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

            val requestBuilder = AndroidNetworking.delete(url.checkjstring())
            headers.keys().forEach { key ->
                requestBuilder.addHeaders(key.checkjstring(), headers.get(key).checkjstring())
            }

            requestBuilder.addJSONObjectBody(JSONObject(body))

            requestBuilder.build().getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(jsonResponse: JSONObject?) {
                    if (jsonResponse != null) {
                        responseTable.set("body", toLuaTable(jsonResponse))
                    }
                    latch.release()
                }

                override fun onError(error: ANError) {
                    responseTable.set("error", LuaValue.valueOf(error.errorDetail))
                    latch.release()
                }
            })

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

            val requestBuilder = AndroidNetworking.patch(url.checkjstring())
            headers.keys().forEach { key ->
                requestBuilder.addHeaders(key.checkjstring(), headers.get(key).checkjstring())
            }

            requestBuilder.addJSONObjectBody(JSONObject(body))

            requestBuilder.build().getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(jsonResponse: JSONObject?) {
                    if (jsonResponse != null) {
                        responseTable.set("body", toLuaTable(jsonResponse))
                    }
                    latch.release()
                }

                override fun onError(error: ANError) {
                    responseTable.set("error", LuaValue.valueOf(error.errorDetail))
                    latch.release()
                }
            })

            latch.acquire()  // Block until the response is received
            return responseTable
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
            is String -> LuaValue.valueOf(value)
            is Number -> LuaValue.valueOf(value.toDouble())
            is Boolean -> LuaValue.valueOf(value)
            else -> LuaValue.NIL
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