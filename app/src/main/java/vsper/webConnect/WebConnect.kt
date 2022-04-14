package vsper.webConnect

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import vsper.app.global.GlobalRegistry
import vsper.eventChannel.event.ConSuccEvent
import vsper.eventChannel.event.SignInEvent
import java.net.URI


object WebConnect {
    private val TAG = "wsConnect"
    var wsConnect: WsConnect = WsConnect(URI(""), HashMap<String, String>())
    private const val username_ = "username"
    private const val password_ = "password"

    suspend fun logIn(
        serverIP: String,
        serverPort: String,
        account: String,
        passWord: String
    ): ConSuccEvent? = withContext(Dispatchers.IO) {

        var connectSuccess: ConSuccEvent? = null
        val urlParameters =
            JSONObject().apply {
                put(username_, account)
                put(password_, passWord)
            }.toString()
        val cookie = postToGetCookie("http://${serverIP}:${serverPort}/login", urlParameters)
        if (cookie != null) {
            val header: HashMap<String, String> = HashMap()
            header[WebRegistry.HEADER_COOKIE] = cookie
            header["User-Agent"] = WebRegistry.USER_AGENT
            val connection =
                WsConnect(URI("ws://${serverIP}:${serverPort}/ws"), header)
            try {
                if (connection.connectBlocking()) {
                    Log.d(TAG, "wsConnect success")
                    connectSuccess = ConSuccEvent(
                        cookie = cookie,
                        wsConnect = connection,
                        account = account
                    )
                }
            } catch (e: java.lang.Exception) {
                e.message?.let { Log.d(TAG + "connect blocking", it) }
            }
        }
        if (null == connectSuccess)
            Log.d(TAG, "wsConnect failure")

        return@withContext connectSuccess
    }


    private fun postToGetCookie(url: String, urlParameters: String): String? {
        var cookie: String? = null
        try {
            val response = postTo(url, urlParameters)
            if (response != null) {
                if (!response.isSuccessful) {
                    Log.d(TAG, "http post 请求失败")
                    // 一般会在这抛个异常
                }
                val result = response.body()!!.string()
                response.body()!!.close()

                println(result)
                cookie = response.header(WebRegistry.HEADER_SET_COOKIE)!!
                Log.d(TAG, "cookie:$cookie")
            }
        } catch (e: Exception) {
            Log.d(TAG, "http post exception:")
            e.printStackTrace()
        }
        return cookie
    }


    fun signIn(account: String, passWord: String) {
        val thread = Thread {
            val serverIP = WebRegistry.serverIP
            val serverPort = WebRegistry.serverPort

            val urlParameters =
                JSONObject().apply {
                    put(username_, account)
                    put(password_, passWord)
                }.toString()
            val url = "http://${serverIP}:${serverPort}/sign"
            if (postTo(url, urlParameters) == null) {
                Log.d(TAG, "signIn failure")
                GlobalRegistry.eventChannel.addEvent(
                    SignInEvent(
                        success = false,
                        account,
                        passWord
                    )
                )
            } else
                Log.d(TAG, "signIn $account success")
            GlobalRegistry.eventChannel.addEvent(SignInEvent(success = true, account, passWord))
        }
        thread.start()
        thread.join()
    }

    private fun postTo(url: String, urlParameters: String): Response? {

        val okHttpClient = OkHttpClient()
        val bodyHead = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(bodyHead, urlParameters)
        val request: Request = Request.Builder().url(url).post(
            body
        ).build()
        var response: Response? = null
        try {
            response = okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.d(TAG, "exception in Post to #$url# by #$urlParameters# :")
            response = null
        }
        return response
    }

}

