package vsper.webConnect

import vsper.Config
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/*what*/class HttpConnectTest3 {

    fun main() {
        isOk("${Config.defaultServerIP}:${Config.defaultServerPort}/login")
    }

    fun isOk(url: String): Boolean {
        try {
            val netUrl = URL(url)
            val connection: HttpURLConnection = netUrl.openConnection() as HttpURLConnection
            connection.setConnectTimeout(3000) //连接主机超时时间ms
            connection.setReadTimeout(3000) //从主机读取数据超时时间ms
            if (HttpURLConnection.HTTP_OK === connection.getResponseCode()) {
                println("网络联通！")
                return true
            }
        } catch (e: IOException) {
            println("连接不通$e")
            return false
        }
        return false
    }
}