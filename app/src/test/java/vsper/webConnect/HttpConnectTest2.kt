package vsper.webConnect

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


/*what*/class HttpConnectTest2 {

    private val USER_AGENT = WebRegistry.USER_AGENT
    private val postUrl: URL = URL("http://${WebRegistry.serverIP}:${WebRegistry.serverPort}/login")
    private val tag = "wsConnect"
    lateinit var connect: WsConnect
    var cookie: String = "no-cookie"

    @Throws(Exception::class)
    public fun sendHttpPost(): WsConnect {
        val postUrl = URL("http://${WebRegistry.serverIP}:${WebRegistry.serverPort}/login")
        val con: HttpURLConnection = postUrl.openConnection() as HttpURLConnection

        println("\nSending 'POST' request to URL : $postUrl by $con")
        //添加请求头
        con.setRequestMethod("POST")
        con.setRequestProperty("User-Agent", WebRegistry.USER_AGENT)
        val urlParameters = "{\"username\":\"username\",\"password\":\"password\"}"

        //发送Post请求
        con.setDoOutput(true)
        val wr = DataOutputStream(con.getOutputStream())
        wr.writeBytes(urlParameters)
        wr.flush()
        wr.close()
        val responseCode: Int = con.responseCode
        println("Post parameters : $urlParameters")
        println("Response Code : $responseCode")

        cookie = getCookie(con)
        println("cookie:$cookie")

        val header: HashMap<String, String> = HashMap()
        header[WebRegistry.HEADER_COOKIE] = cookie
        header["User-Agent"] = WebRegistry.USER_AGENT
        val connection =
            WsConnect(URI("ws://${WebRegistry.serverIP}:${WebRegistry.serverPort}/ws"), header)
        println("success:" + connection.connectBlocking())
        return connection
    }


    fun main() {
        val http = HttpConnectTest2()
        println("\nTesting 2 - Send Http POST request")
        http.sendHttpPost()
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

    private fun getResponse(con: HttpURLConnection): String {
        val response = StringBuffer()
        val `in` = BufferedReader(InputStreamReader(con.inputStream))
        var inputLine: String?
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        `in`.close()
        return response.toString()
    }
}
