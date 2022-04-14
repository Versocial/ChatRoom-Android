package vsper.webConnect

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


/*what*/class HttpConnectTest {

    private val USER_AGENT = WebRegistry.USER_AGENT
    private val postUrl: URL = URL("http://${WebRegistry.serverIP}:${WebRegistry.serverPort}/login")


    fun main() {
        val http = HttpConnectTest()
//        println("Testing 1 - Send Http GET request")
//        http.sendGet()
        println("\nTesting 2 - Send Http POST request")
        http.sendPost()
    }

    // HTTP POST请求
    @Throws(Exception::class)
    private fun sendPost() {
        val con: HttpURLConnection = postUrl.openConnection() as HttpURLConnection

        //添加请求头
        con.setRequestMethod("POST")
        con.setRequestProperty("User-Agent", USER_AGENT)
        val urlParameters = "{\"username\":\"username\",\"password\":\"password\"}"

        //发送Post请求
        con.setDoOutput(true)
        val wr = DataOutputStream(con.getOutputStream())
        wr.writeBytes(urlParameters)
        wr.flush()
        wr.close()
        println("wr:" + wr)
        println("con:" + con.responseMessage)
        val responseCode: Int = con.getResponseCode()
        println("\nSending 'POST' request to URL : $postUrl")
        println("Post parameters : $urlParameters")
        println("Response Code : $responseCode")

        val cookieList: List<String>?
        cookieList = con.getHeaderFields().get(WebRegistry.HEADER_SET_COOKIE)
        var cookie: String = ""
        if (cookieList != null) {
            for (c in cookieList) {
                println("cookie$c")
                cookie += c
            }
        }
        println(cookie)

        val `in` = BufferedReader(
            InputStreamReader(con.getInputStream())
        )
        var inputLine: String?
        val response = StringBuffer()
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }

        `in`.close()

        //打印结果
        println(response.toString())

        var header: HashMap<String, String> = HashMap()
        header[WebRegistry.HEADER_COOKIE] = cookie
        header["User-Agent"] = WebRegistry.USER_AGENT
        val connection =
            WsConnect(URI("ws://${WebRegistry.serverIP}:${WebRegistry.serverPort}/ws"), header)
        println("success:" + connection.connectBlocking())
    }

}