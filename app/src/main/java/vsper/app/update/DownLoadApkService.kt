package vsper.app.update

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import vsper.app.R
import vsper.app.global.VsperApplication
import vsper.app.main.MainActivity
import vsper.app.utils.AppUtils
import vsper.app.utils.IDRegistry
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DownLoadApkService : Service() {
    companion object {
        private const val BUFFER_SIZE = 20 * 1024 //缓存大小
        private const val TAG = "vsperUpdateApkDownloadService"
    }

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var NOTIFICATION_ID = IDRegistry.DOWNLOAD_NOTIFICATION_ID
    private lateinit var urlStr: String
    private val channelId = "vsperUpdateService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate executed")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId, "下载apk前台Service通知",
            NotificationManager.IMPORTANCE_MIN
        )
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder
            .setContentTitle("正在准备下载：0%")
            .setContentText("from server")
            .setSmallIcon(R.drawable.ic_arrow1)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow2))
            .setContentIntent(pi)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        urlStr = intent?.getStringExtra("url")!!
        val dir = StorageUtils.getCacheDirectory(this)
        if (dir == null) {
            Log.d(TAG, "no cache to download")
            AppUtils.toast("no cache to download")
            return super.onStartCommand(intent, flags, startId)
        }
        updateText("from: $urlStr")
        //download and install
        GlobalScope.launch {
            val apkFile =
                downloadApk(
                    urlStr = urlStr,
                    dir = dir,
                    apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length)
                )
            // 下载完成,调用installAPK开始安装文件
            if (apkFile != null) {
                updateTitle("安装中")
                updateText("下载已完成")
                InstallUtil.installAPk(VsperApplication.context, apkFile)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initUrlConnect(url: URL): HttpURLConnection {
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        urlConnection.doOutput = false
        urlConnection.connectTimeout = 10 * 1000
        urlConnection.readTimeout = 10 * 1000
        urlConnection.setRequestProperty("Connection", "Keep-Alive")
        urlConnection.setRequestProperty("Charset", "UTF-8")
        urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate")
        return urlConnection
    }


    /**
     * 实时更新下载进度条显示
     * @param progress
     */
    private fun updateTitle(title: String) {
        notificationBuilder.setContentTitle(title);
        val notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, notification);

    }

    /**
     * 实时更新下载进度条显示
     * @param progress
     */
    private fun updateText(text: String) {
        notificationBuilder.setContentText(text);
        val notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, notification);

    }


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private suspend fun downloadApk(urlStr: String, dir: File, apkName: String): File? =
        withContext(Dispatchers.IO) {
            var `in`: InputStream? = null
            var out: FileOutputStream? = null
            var apkFile: File? = null
            try {
                //建立下载连接
                val url = URL(urlStr)
                val urlConnection = initUrlConnect(url)
                urlConnection.connect()

                //以文件流读取数据
                val bytetotal = urlConnection.contentLength.toLong() //取得文件长度
                var bytesum: Long = 0
                var byteread = 0
                `in` = urlConnection.inputStream
                apkFile = File(dir, apkName)
                out = FileOutputStream(apkFile)
                val buffer = ByteArray(BUFFER_SIZE)
                val limit = 0
                var oldProgress = 0

                while (`in`.read(buffer).also { byteread = it } != -1) {
                    bytesum += byteread.toLong()
                    out.write(buffer, 0, byteread)
                    val progress = (bytesum * 100L / bytetotal).toInt()
                    // 如果进度与之前进度相等，则不更新，如果更新太频繁，则会造成界面卡顿
                    if (progress != oldProgress) {
                        updateTitle("正在下载： $progress%")
                        Log.d(TAG, "update on $progress")
                    }
                    oldProgress = progress
                }
                updateTitle("正在下载： 100%")
                Log.d(TAG, "download apk finish")
            } catch (e: Exception) {
                Log.e(TAG, "download apk file error")
                e.printStackTrace()
                apkFile = null
            } finally {
                if (out != null) {
                    try {
                        out.close()
                    } catch (ignored: IOException) {
                    }
                }
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (ignored: IOException) {
                    }
                }
            }
            return@withContext apkFile
        }
}