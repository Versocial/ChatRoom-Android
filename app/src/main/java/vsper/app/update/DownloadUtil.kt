package vsper.app.update

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vsper.Config
import java.net.URL

object DownloadUtil {
    private const val TAG = "update"
    private val LATEST_VERSION_URL = Config.updateUrl

    internal suspend fun getLatestVersionInfo(): String? = withContext(Dispatchers.IO) {
        return@withContext downloadLatestVersionInfo()
    }

    private fun downloadLatestVersionInfo(): String? {
        var ans: String? = null
        try {
            val url = URL(LATEST_VERSION_URL)
            //打开连接
            val conn = url.openConnection()
            //打开输入流
            val `is` = conn.getInputStream()
            //获得长度
            val contentLength = conn.contentLength
            Log.d(TAG, "contentLength = $contentLength")
            //创建字节流
            val bs = ByteArray(2048)
            //写数据
            val len: Int
            len = `is`.read(bs)
            if (len < 1) {
                Log.d(TAG, "error$len")
            }
            ans = String(bs)
            Log.d(TAG, ans)
            //完成后关闭流
            Log.d(TAG, "download-finish")
            `is`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ans
    }


}