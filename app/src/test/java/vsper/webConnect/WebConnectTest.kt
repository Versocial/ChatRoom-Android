package vsper.webConnect

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import vsper.Config
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


/*what*/ class WebConnectTest {
    companion object {
        private val TAG = "sdf"
    }

    fun runTest() {

        test2()
//        test("http://${Config.defaultServerIP}:${Config.defaultServerPort}/login",urlParameters)
    }

    fun test(url: String, urlParameters: String) {
        val json = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(json, urlParameters)
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val call: Call = okHttpClient.newCall(request)
        println("sfdsfsdf")
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("WebConnect.TAG" + "onFailure: ")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    println("WebConnect.TAG" + "onResponse: " + response.body()!!.string())
                }
            })
        call.execute()
    }


    fun test2() {

        val urlParameters =
            JSONObject().apply {
                put("password", "password")
                put("username", "username")
            }.toString()
        val okHttpClient = OkHttpClient()
        val bodyHead = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(bodyHead, urlParameters)
        val request: Request = Request.Builder()
            .url("http://${Config.defaultServerIP}:${Config.defaultServerPort}/login").post(
            body
        ).build()
        var response: Response
        try {
            response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                println("请求失败")
                // 一般会在这抛个异常
            } else {
                println("请求完成")
            }
            val result = response.body()!!.string()
            response.body()!!.close()

            println(result)

            val cookie = response.header(WebRegistry.HEADER_SET_COOKIE)!!
            println("cookie:$cookie")

            val header: HashMap<String, String> = HashMap()
            header[WebRegistry.HEADER_COOKIE] = cookie
            header["User-Agent"] = WebRegistry.USER_AGENT
            val connection =
                WsConnect(URI("ws://${WebRegistry.serverIP}:${WebRegistry.serverPort}/ws"), header)
            println("success:" + connection.connectBlocking())
        } catch (e: Exception) {
            println("sdfsdfsdfsdf")
            e.printStackTrace()
            println("sdfsdfsdfsd22f")
        }

    }

    private fun postTo(url: String, urlParameters: String): HttpURLConnection {

        val postUrl = URL(url)
        val con: HttpURLConnection = postUrl.openConnection() as HttpURLConnection
        con.connectTimeout = 3000; //连接主机超时时间ms
        con.readTimeout = 3000; //从主机读取数据超时时间ms
        Log.d(TAG, "response Code${con.responseCode == HttpURLConnection.HTTP_OK}")

        Log.d(TAG, "\nSending 'POST' request to URL : $postUrl by $con")
        //添加请求头
        con.requestMethod = "POST"
        con.setRequestProperty("User-Agent", WebRegistry.USER_AGENT)

        //发送Post请求
        try {
            con.doOutput = true
            val wr = DataOutputStream(con.outputStream)
            wr.writeBytes(urlParameters)
            wr.flush()
            wr.close()
            Log.d(TAG, "response:" + con.responseMessage)
            Log.d(TAG, "Response Code : ${con.responseCode}")
        } catch (e: Exception) {
            Log.d(TAG, "error")
            e.printStackTrace()
        }

        return con

    }

    private fun getCookie(con: HttpURLConnection): String {
        val cookieList: List<String>? = con.headerFields[WebRegistry.HEADER_SET_COOKIE]
        var cookie: String = ""
        if (cookieList != null) {
            for (c in cookieList) {
                println("cookie$c")
                cookie += c
            }
        }
        return cookie
    }
}